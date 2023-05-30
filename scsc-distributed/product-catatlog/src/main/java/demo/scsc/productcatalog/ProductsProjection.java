package demo.scsc.productcatalog;

import demo.scsc.api.productcatalog.ProductListQuery;
import demo.scsc.api.productcatalog.ProductListQueryResponse;
import demo.scsc.api.productcatalog.ProductUpdatedEvent;
import demo.scsc.config.JpaPersistenceUnit;
import jakarta.persistence.EntityManager;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@ProcessingGroup("products")
public class ProductsProjection {

    @EventHandler
    public void on(ProductUpdatedEvent productUpdatedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();

        if (productUpdatedEvent.onSale()) {
            em.merge(toEntity(productUpdatedEvent));
        } else {
            ProductEntity productEntity = em.find(ProductEntity.class, productUpdatedEvent.id());
            if (productEntity != null) em.remove(productEntity);
        }

        em.getTransaction().commit();
    }

    @QueryHandler
    public ProductListQueryResponse getProducts(ProductListQuery query) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();

        List<ProductEntity> products = em.createQuery(getProductsSql(query.sortBy()), ProductEntity.class).getResultList();
        ProductListQueryResponse response = new ProductListQueryResponse(
                products.stream()
                        .map(productEntity -> new ProductListQueryResponse.ProductInfo(
                                productEntity.getId(),
                                productEntity.getName(),
                                productEntity.getDesc(),
                                productEntity.getPrice(),
                                productEntity.getImage()
                        ))
                        .collect(Collectors.toList())
        );

        em.getTransaction().commit();
        return response;
    }


    @NotNull
    private ProductEntity toEntity(@NotNull ProductUpdatedEvent productUpdatedEvent) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(productUpdatedEvent.id());
        productEntity.setName(productUpdatedEvent.name());
        productEntity.setDesc(productUpdatedEvent.desc());
        productEntity.setPrice(productUpdatedEvent.price());
        productEntity.setImage(productUpdatedEvent.image());
        return productEntity;
    }

    private static String getProductsSql(String sortBy) {
        return "SELECT p FROM ProductEntity AS p ORDER BY " + sortBy;
    }
}
