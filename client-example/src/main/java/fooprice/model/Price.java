package fooprice.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Price {
    private UUID itemNumber;
    private Integer price;
    private String description;
    private Map<String, String> metadata;
    private List<String> offers;
    private PriceData data;
    private String updatedBy;

    public Price(UUID itemNumber, Integer price, String description, Map<String, String> metadata, List<String> offers, PriceData data, String updatedBy) {
        this.itemNumber = itemNumber;
        this.price = price;
        this.description = description;
        this.metadata = metadata;
        this.offers = offers;
        this.data = data;
        this.updatedBy = updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public List<String> getOffers() {
        return offers;
    }

    public void setOffers(List<String> offers) {
        this.offers = offers;
    }

    public PriceData getData() {
        return data;
    }

    public void setData(PriceData data) {
        this.data = data;
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

    public static class PriceData {
        private String value;

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public PriceData(String value) {
            this.value = value;
        }
    }
}
