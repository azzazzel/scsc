package demo.scsc.shpoppingcart.queryside;

import demo.scsc.api.shoppingcart.*;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.axonframework.modelling.command.AggregateLifecycle.isLive;

@ProcessingGroup("carts")
public class CartsProjection {

    private static final Logger LOG = LoggerFactory.getLogger(CartsProjection.class);

    // CartCreatedEvent
    @EventHandler
    public void on(CartCreatedEvent cartCreatedEvent) {
        CartStore cartStore = new CartStore();
        cartStore.saveCart(cartCreatedEvent.owner(), cartCreatedEvent.id());
    }

    // ProductAddedToCartEvent
    @EventHandler
    public void on(ProductAddedToCartEvent productAddedToCartEvent) {
        CartStore cartStore = new CartStore();
        cartStore.saveProduct(
                productAddedToCartEvent.cartId(),
                productAddedToCartEvent.productId()
        );
    }

    // ProductRemovedFromCartEvent
    @EventHandler
    public void on(ProductRemovedFromCartEvent productRemovedFromCartEvent) {
        CartStore cartStore = new CartStore();
        cartStore.removeProduct(productRemovedFromCartEvent.cartId(), productRemovedFromCartEvent.productId());
    }

    // CartAbandonedEvent
    @EventHandler
    public void on(CartAbandonedEvent cartAbandonedEvent) {
        CartStore cartStore = new CartStore();
        cartStore.removeCart(cartAbandonedEvent.cartId());
    }


    // CartCheckedOutEvent
    @EventHandler
    public void on(CartCheckedOutEvent cartCheckedOutEvent) {
        CartStore cartStore = new CartStore();
        cartStore.removeCart(cartCheckedOutEvent.cartId());
    }

    // GetCartQueryResponse
    @QueryHandler
    public Optional<GetCartQueryResponse> on(GetCartQuery getCartQuery) {

        CartStore cartStore = new CartStore();
        GetCartQueryResponse getCartQueryResponse = cartStore.getOwnersCarts(getCartQuery.owner());
        return Optional.ofNullable(getCartQueryResponse);
    }

    // onReset
    @ResetHandler
    public void onReset() {
        LOG.info("[    RESET ] ");
        CartStore cartStore = new CartStore();
        cartStore.reset();
    }

    // interceptors

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[    EVENT ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }


}