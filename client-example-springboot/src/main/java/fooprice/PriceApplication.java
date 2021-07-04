package fooprice;

import com.lowes.auditor.client.api.Auditor;
import fooprice.model.Price;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PriceApplication implements CommandLineRunner {
    private Auditor auditor;

    public PriceApplication(Auditor auditor) {
        this.auditor = auditor;
    }

    @Override
    public void run(String... args) throws Exception {
        Price oldPrice = new Price(UUID.randomUUID(), 1234, "old_item", null);

        UUID newItemNumber = UUID.randomUUID();
        Price newPrice =
                new Price(
                        UUID.randomUUID(),
                        21212,
                        "new_item",
                        new HashMap<String, String>() {
                            {
                                put("new_price_id", "98767");
                            }
                        });
        System.out.println("Running auditor! for newItemNumber $newItemNumber");
        auditor.audit(oldPrice, newPrice);

        auditor.log(new Price(UUID.randomUUID(), null, "logging description", new HashMap<>()));

        System.out.println("Done");
    }

    public static void main(String[] args) {
        SpringApplication.run(PriceApplication.class, args);
    }
}
