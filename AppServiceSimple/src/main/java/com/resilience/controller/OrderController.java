package com.resilience.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.resilience.discount.DiscountCalculator;
import com.resilience.model.Customer;
import com.resilience.model.Product;
import com.resilience.model.SuccessfulOrderItem;
import com.resilience.model.repository.CustomerRepository;
import com.resilience.model.repository.ProductRepository;
import com.resilience.model.repository.SuccessfulOrderItemRepository;

@RestController
@RequestMapping(value="/order")
public class OrderController {
	@Autowired
	private SuccessfulOrderItemRepository orderRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CustomerRepository customerRepository;

	@GetMapping(path="/add")
	public @ResponseBody String add(@RequestParam(value="productId") Long productId,
			                        @RequestParam(value="customerId") Long customerId) {		
		Product product = productRepository.findOne(productId);
		Customer customer = customerRepository.findOne(customerId);
		
		Double discount = DiscountCalculator.discountCalculator(productId, customerId);
		
		Double price = product.getPrice();
		customer.setAmount(customer.getAmount() - price * discount);
		product.setQuantity(product.getQuantity() - 1);
		
		customerRepository.save(customer);
		productRepository.save(product);
		
		SuccessfulOrderItem successfulItem = new SuccessfulOrderItem();
		successfulItem.setProductId(productId);
		successfulItem.setCustomerId(customerId);
		successfulItem.setPrice(price);
		successfulItem.setDiscount(discount);
		
		orderRepository.save(successfulItem);
		
		return successfulItem.getId().toString();
	}
	
	@GetMapping(path="/delete")
	public @ResponseBody String delete(@RequestParam(value="id") Long id) {
		orderRepository.delete(id);
		
		return "OK";
	}
	
	@GetMapping(path="/all")
	public @ResponseBody Iterable<SuccessfulOrderItem> getAllOrders() {
		return orderRepository.findAll();
	}
}
