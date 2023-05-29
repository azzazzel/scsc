package demo.scsc.api.productcatalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdatedEvent(
     UUID id,
     String name,
     String desc,
     BigDecimal price,
     String image,
     boolean onSale
){
}
