package demo.scsc.external.inventory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.scsc.config.AxonFramework;
import demo.scsc.external.inventory.InventoryProduct;
import org.axonframework.config.Configuration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExternalInventory {

    private final Configuration axonFramework;
    private final List<InventoryProduct> inventory;

    public ExternalInventory() {
        inventory = initializeInventory();

        axonFramework = AxonFramework.configure("Inventory System")
                .withJsonSerializer()
                .connectedToInspectorAxon("1ca6fe24", "087cb5cb", "c31c8730b7544d82a8a6b7cd114d25f5")
                .start();

        SwingUtilities.invokeLater(() -> new InventoryUI(axonFramework, inventory).start());
    }

    public static void main(String[] args) {
        new ExternalInventory();
    }

    @NotNull
    private List<InventoryProduct> initializeInventory() {
        final List<InventoryProduct> inventoryProducts;
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream jsonInput = ExternalInventory.class.getClassLoader().getResourceAsStream("products.json");
        try {
            inventoryProducts = objectMapper.readValue(jsonInput, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Can't create External Product System", e);
        }
        inventoryProducts.forEach(inventoryProduct -> inventoryProduct.onSale = true);
        return inventoryProducts;
    }
}
