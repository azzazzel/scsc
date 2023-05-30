package demo.scsc.shpoppingcart;

import demo.scsc.config.AxonFramework;
import demo.scsc.shpoppingcart.commandside.Cart;
import demo.scsc.shpoppingcart.commandside.Order;
import demo.scsc.shpoppingcart.commandside.ProductValidation;
import demo.scsc.shpoppingcart.queryside.CartsProjection;
import demo.scsc.shpoppingcart.queryside.OrdersProjection;

public class ShoppingCart {
        public static void main(String[] args) {
                AxonFramework.configure("Shopping cart")
                        .withJsonSerializer()
                        .withJPATokenStoreIn("SCSC")
                        .withAggregates(
                                Cart.class,
                                Order.class
                        )
                        .withMessageHandlers(
                                new CartsProjection(),
                                new ProductValidation(),
                                new OrdersProjection()
                        )
                        .connectedToInspectorAxon("1ca6fe24", "087cb5cb", "c31c8730b7544d82a8a6b7cd114d25f5")
                        .startAndWait();
            }
}
