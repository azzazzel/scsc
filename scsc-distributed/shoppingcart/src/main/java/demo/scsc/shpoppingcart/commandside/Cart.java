package demo.scsc.shpoppingcart.commandside;

import demo.scsc.api.shoppingcart.*;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.*;


public class Cart {

    private static final String DEADLINE_NAME_ABANDON_CART = "abandon-cart";
    private static final Logger LOG = LoggerFactory.getLogger(Cart.class);

    @AggregateIdentifier
    UUID id;
    private String owner;
    private final Set<UUID> products = new HashSet<>();

    // AddProductToCartCommand
    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public UUID handle(AddProductToCartCommand command, DeadlineManager deadlineManager) {

                /* -------------------------
                        validation
                ------------------------- */

        if (id == null) {
            if (command.owner() == null) {
                throw new CommandExecutionException("Can't create shopping cart for unknown owner! ", null);
            }

            apply(new CartCreatedEvent(UUID.randomUUID(), command.owner()));
        }

        if (products.contains(command.productId())) {
            throw new CommandExecutionException("Product already in the cart! ", null);
        }

                /* -------------------------
                        notification
                ------------------------- */

        apply(new ProductAddedToCartEvent(id, command.productId()));
        deadlineManager.schedule(Duration.ofMinutes(10), DEADLINE_NAME_ABANDON_CART);

        return id;
    }

    // RemoveProductFromCartCommand
    @CommandHandler
    public void handle(RemoveProductFromCartCommand command) {

                /* -------------------------
                        validation
                ------------------------- */

        if (!products.contains(command.productId())) {
            throw new CommandExecutionException("Product not in the cart! ", null);
        }

                /* -------------------------
                        notification
                ------------------------- */

        apply(new ProductRemovedFromCartEvent(id, command.productId()));
    }

    // AbandonCartCommand
    @CommandHandler
    public void handle(AbandonCartCommand command) {

                /* -------------------------
                        validation
                ------------------------- */

                /* -------------------------
                        notification
                ------------------------- */

        apply(new CartAbandonedEvent(command.cartId(), CartAbandonedEvent.Reason.MANUAL));
    }

    // CheckOutCartCommand
    @CommandHandler
    public void handle(CheckOutCartCommand command) {

                /* -------------------------
                        validation
                ------------------------- */

                /* -------------------------
                        notification
                ------------------------- */

        try {
            createNew(Order.class, () -> new Order(products.stream().toList(), owner));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandExecutionException(e.getMessage(), e);
        }

        apply(new CartCheckedOutEvent(command.cartId()));
    }


    // CartCreatedEvent
    @EventSourcingHandler
    public void on(CartCreatedEvent cartCreatedEvent) {
        this.id = cartCreatedEvent.id();
        this.owner = cartCreatedEvent.owner();
    }

    // ProductAddedToCartEvent
    @EventSourcingHandler
    public void on(ProductAddedToCartEvent productAddedToCartEvent) {
        this.products.add(productAddedToCartEvent.productId());
    }


    // ProductRemovedFromCartEvent
    @EventSourcingHandler
    public void on(ProductRemovedFromCartEvent productRemovedFromCartEvent) {
        this.products.remove(productRemovedFromCartEvent.productId());
    }

    // CartAbandonedEvent
    @EventSourcingHandler
    public void on(CartAbandonedEvent cartAbandonedEvent) {
        markDeleted();
    }

    // CartCheckedOutEvent
    @EventSourcingHandler
    public void on(CartCheckedOutEvent cartCheckedOutEvent) {
        markDeleted();
    }

    // DeadlineHandler
    @DeadlineHandler(deadlineName = DEADLINE_NAME_ABANDON_CART)
    public void onDeadline() {
        apply(new CartAbandonedEvent(id, CartAbandonedEvent.Reason.TIMEOUT));
    }

    // MessageHandlerInterceptors

    @MessageHandlerInterceptor(messageType = CommandMessage.class)
    public void intercept(CommandMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[  COMMAND ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        if (isLive()) {
            LOG.info("[    EVENT ] " + message.getPayload().toString());
        } else {
            LOG.info("[ SOURCING ] " + message.getPayload().toString());
        }
        interceptorChain.proceed();
    }
}

