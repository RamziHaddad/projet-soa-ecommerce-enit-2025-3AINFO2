package ecommerce.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication

@EnableScheduling
public class PricingService1Application {
	public static void main(String[] args) {
		SpringApplication.run(PricingService1Application.class, args);
	}
}