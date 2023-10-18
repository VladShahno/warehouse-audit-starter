package warehouse.com.audit.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "warehouse.com")
public class WarehouseAuditStarterApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseAuditStarterApplication.class, args);
	}

}
