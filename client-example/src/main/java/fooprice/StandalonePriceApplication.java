package fooprice;

import com.lowes.auditor.client.api.Auditor;
import com.lowes.auditor.client.entities.domain.*;
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig;
import com.lowes.auditor.core.entities.domain.EventSourceType;
import com.lowes.auditor.core.entities.domain.EventType;
import fooprice.model.Price;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StandalonePriceApplication implements Runnable {
    private final Auditor auditor;

    public StandalonePriceApplication(Auditor auditor) {
        this.auditor = auditor;
    }

    private void testCreateOldObjectNullAndNewObject(UUID itemNumber) {
        Price oldPrice = null;
        Price newPrice = getPrice(itemNumber);
        auditor.audit(oldPrice, newPrice);
    }

    private void testDeleteOldObjectNotNullAndNewObjectAsNull(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        Price newPrice = null;
        auditor.audit(oldPrice, newPrice);
    }

    private void testUpdateBothObject(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        Price newPrice = getPrice(itemNumber);
        newPrice.setDescription("new_price_description");
        newPrice.setPrice(50);
        newPrice.setMetadata(Map.of("MetaKey", "metaValue", "NewMetaKey", "NewMetaValue"));
        newPrice.setOffers(Collections.emptyList());
        newPrice.setData(new Price.PriceData(""));
        auditor.audit(oldPrice, newPrice);
    }

    private void testStaticDataSubstitution(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        Price newPrice = getPrice(itemNumber);
        newPrice.setDescription("new_price_description");
        AuditorEventConfig config = new AuditorEventConfig();
        config.setApplicationName("client-example-java");
        config.setEventSource(
                new EventSourceConfig(
                        EventSourceType.USER,
                        new EventSourceMetadataConfig("static-user-id", null, null)));
        config.setMetadata(Map.of("iteNumber", "Sadly i am only static-value"));
        auditor.audit(oldPrice, newPrice, config);
    }

    private void testDynamicDataSubstitution(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        Price newPrice = getPrice(itemNumber);
        newPrice.setDescription("new_price_description");
        Price.PriceData priceData = new Price.PriceData("NewPriceDataValue");
        newPrice.setData(priceData);
        AuditorEventConfig config = new AuditorEventConfig();
        config.setApplicationName("client-example-java");
        config.setEventSource(
                new EventSourceConfig(
                        EventSourceType.USER,
                        new EventSourceMetadataConfig("${updatedBy}", null, null)));
        config.setMetadata(
                Map.of(
                        "iteNumber",
                        "${itemNumber}",
                        "price.data",
                        "${data.value}",
                        "static-key",
                        "static-value"));
        auditor.audit(oldPrice, newPrice, config);
    }

    private void testEventFilters(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        oldPrice.setOffers(null);
        Price newPrice = getPrice(itemNumber);
        newPrice.setDescription("new_price_description");
        newPrice.setPrice(50);
        AuditorEventConfig config = new AuditorEventConfig();
        EventFilter eventFilter =
                new EventFilter(true, List.of(EventType.CREATED, EventType.UPDATED));
        Filters filters = new Filters();
        filters.setEvent(eventFilter);
        config.setFilters(filters);
        auditor.audit(oldPrice, newPrice, config);
    }

    private void testElementIncludesFilters(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        Price newPrice = getPrice(itemNumber);
        newPrice.setDescription("new_price_description");
        newPrice.setPrice(50);
        AuditorEventConfig config = new AuditorEventConfig();
        ElementFilterOptions options = new ElementFilterOptions();
        options.setIncludes(List.of("description"));
        ElementFilter elementFilter = new ElementFilter(true, List.of("InclusionFilter"), options);
        Filters filters = new Filters();
        filters.setElement(elementFilter);
        config.setFilters(filters);
        auditor.audit(oldPrice, newPrice, config);
    }

    private void testElementExcludesFilters(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        Price newPrice = getPrice(itemNumber);
        newPrice.setDescription("new_price_description");
        newPrice.setPrice(50);
        AuditorEventConfig config = new AuditorEventConfig();
        ElementFilterOptions options = new ElementFilterOptions();
        options.setExcludes(List.of("description"));
        ElementFilter elementFilter = new ElementFilter(true, List.of("ExclusionFilter"), options);
        Filters filters = new Filters();
        filters.setElement(elementFilter);
        config.setFilters(filters);
        auditor.audit(oldPrice, newPrice, config);
    }

    private void testLoggingFilters(UUID itemNumber) {
        Price oldPrice = getPrice(itemNumber);
        Price newPrice = getPrice(itemNumber);
        newPrice.setDescription("new_price_description");
        AuditorEventConfig config = new AuditorEventConfig();
        Filters filters = new Filters();
        filters.setLogging(new LoggingFilter(true));
        config.setFilters(filters);
        auditor.audit(oldPrice, newPrice, config);
    }

    private void testLog(UUID itemNumber) {
        auditor.log(getPrice(itemNumber));
    }

    private Price getPrice(UUID itemNumber) {
        Price.PriceData data = new Price.PriceData("PriceDataValue");
        return new Price(
                itemNumber,
                1234,
                "old_price_description",
                Map.of("MetaKey", "metaValue"),
                List.of("offers"),
                data,
                "DoctorStrange!");
    }

    @Override
    public void run() {
        UUID itemNumber = UUID.randomUUID();
        System.out.println("Running auditor! for" + itemNumber);
        testCreateOldObjectNullAndNewObject(itemNumber);
        testDeleteOldObjectNotNullAndNewObjectAsNull(itemNumber);
        testUpdateBothObject(itemNumber);
        testStaticDataSubstitution(itemNumber);
        testDynamicDataSubstitution(itemNumber);
        testEventFilters(itemNumber);
        testElementIncludesFilters(itemNumber);
        testElementExcludesFilters(itemNumber);
        testLoggingFilters(itemNumber);
        testLog(itemNumber);
        System.out.println("Done");
    }

    public static void main(String[] args) {
        AuditEventProducerConfig producerConfig = new AuditEventProducerConfig();
        producerConfig.setEnabled(true);
        producerConfig.setBootstrapServers("localhost:9092");
        producerConfig.setTopic("auditTopic");
        producerConfig.setConfigs(Map.of("client.id", "client-example"));
        AuditorEventConfig auditorEventConfig = new AuditorEventConfig();
        auditorEventConfig.setApplicationName("client-example");
        Auditor auditor = Auditor.getInstance(producerConfig, auditorEventConfig);
        new StandalonePriceApplication(auditor).run();
    }
}
