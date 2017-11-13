package com.resilience;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.resilience.model.repository.CustomerRepository;
import com.resilience.model.repository.OrderItemRepository;
import com.resilience.model.repository.ProductRepository;
import com.resilience.model.repository.SuccessfulOrderItemRepository;

@Controller
public class RepositoryHandler {
	@Autowired
	public OrderItemRepository orderRepository;
	
	@Autowired
	public SuccessfulOrderItemRepository successfulOrderRepository;
	
	@Autowired
	public ProductRepository productRepository;
	
	@Autowired
	public CustomerRepository customerRepository;
}
