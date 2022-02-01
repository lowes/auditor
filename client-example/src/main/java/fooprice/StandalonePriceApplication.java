package fooprice;

import com.lowes.auditor.client.api.Auditor;
import com.lowes.auditor.client.entities.domain.AuditorEventConfig;
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig;
import fooprice.model.Price;
import java.util.Map;
import java.util.UUID;

public class StandalonePriceApplication implements Runnable {
    private final Auditor auditor;

    public StandalonePriceApplication(Auditor auditor) {
        this.auditor = auditor;
    }

    @Override
    public void run() {
        UUID itemNumber = UUID.randomUUID();
        Price oldPrice = new Price(itemNumber, 1234, "old_price", null);
        Price newPrice = new Price(itemNumber, 21212, "new_price", Map.of("new_price_id", "98767"));
        System.out.println("Running auditor! for newItemNumber $newItemNumber");
        auditor.audit(oldPrice, newPrice);
        auditor.log("bigObject");
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
