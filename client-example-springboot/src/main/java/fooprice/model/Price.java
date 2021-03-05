package fooprice.model;

import java.util.Map;
import java.util.UUID;

public class Price {
    private UUID itemNumber;
    private Integer price;
    private String description;
    private Map<String, String> metadata;

    public Price(UUID itemNumber, Integer price, String description, Map<String, String> metadata) {
        this.itemNumber = itemNumber;
        this.price = price;
        this.description = description;
        this.metadata = metadata;
    }

    public void setItemNumber(UUID itemNumber) {
        this.itemNumber = itemNumber;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public UUID getItemNumber() {
        return itemNumber;
    }
}
