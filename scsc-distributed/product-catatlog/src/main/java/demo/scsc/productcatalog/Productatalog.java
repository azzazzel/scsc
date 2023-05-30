package demo.scsc.productcatalog;

import demo.scsc.config.AxonFramework;

public class Productatalog {

    public static void main(String[] args) {
        AxonFramework.configure("ProductCatalog")
                .withJsonSerializer()
                .withJPATokenStoreIn("SCSC")
                .withMessageHandlers(new ProductsProjection())
                .connectedToInspectorAxon("1ca6fe24", "087cb5cb", "c31c8730b7544d82a8a6b7cd114d25f5")
                .startAndWait();
    }
}
