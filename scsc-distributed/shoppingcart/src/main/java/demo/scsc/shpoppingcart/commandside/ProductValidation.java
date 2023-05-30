package demo.scsc.shpoppingcart.commandside;

import demo.scsc.api.productcatalog.ProductUpdatedEvent;
import demo.scsc.config.JpaPersistenceUnit;
import jakarta.persistence.EntityManager;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;


@ProcessingGroup("product-validation")
public class ProductValidation {

    @EventHandler
    public void on(ProductUpdatedEvent productUpdatedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        em.merge(toEntity(productUpdatedEvent));
        em.getTransaction().commit();
    }

    public ProductValidationInfo forProduct(UUID id) {

        ProductValidationInfo productValidationInfo = null;

        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        ProductValidationEntity productValidationEntity = em.find(ProductValidationEntity.class, id);
        if (productValidationEntity != null) {
            productValidationInfo = new ProductValidationInfo(productValidationEntity);
        }

        return productValidationInfo;
    }

    @NotNull
    private ProductValidationEntity toEntity(@NotNull ProductUpdatedEvent productUpdatedEvent) {
        ProductValidationEntity productValidationEntity = new ProductValidationEntity();
        productValidationEntity.setId(productUpdatedEvent.id());
        productValidationEntity.setName(productUpdatedEvent.name());
        productValidationEntity.setPrice(productUpdatedEvent.price());
        productValidationEntity.setOnSale(productUpdatedEvent.onSale());
        return productValidationEntity;
    }

    public static class ProductValidationInfo {

        public final String name;
        public final BigDecimal price;
        public final boolean onSale;

        public ProductValidationInfo(ProductValidationEntity productValidationEntity) {
            this.name = productValidationEntity.getName();
            this.price = productValidationEntity.getPrice();
            this.onSale = productValidationEntity.isOnSale();
        }
    }

}
