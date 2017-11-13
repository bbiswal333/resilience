package com.resilience;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.resilience.model.repository.CustomerRepository;
import com.resilience.model.repository.OrderItemRepository;
import com.resilience.model.repository.ProductRepository;
import com.resilience.model.repository.SuccessfulOrderItemRepository;
import com.resilience.process.OrderProcessor;

@SpringBootApplication
public class DemoApplication {
	
	private static RepositoryHandler handler;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		
		Thread processor = new Thread(new OrderProcessor(handler));
		processor.start();
	}
	
	@Bean
	public CommandLineRunner demo(	CustomerRepository customerRepository,
								  	OrderItemRepository orderRepository,
								  	ProductRepository productRepository,
								  	SuccessfulOrderItemRepository successfulOrderRepository) {
		return (args) -> {
			if ( handler == null )
				handler = new RepositoryHandler();
			handler.customerRepository = customerRepository;
			handler.orderRepository = orderRepository;
			handler.productRepository = productRepository;
			handler.successfulOrderRepository = successfulOrderRepository;
		};
	}
}
