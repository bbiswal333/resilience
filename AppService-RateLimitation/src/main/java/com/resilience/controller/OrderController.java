package com.resilience.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.resilience.model.OrderItem;
import com.resilience.model.repository.OrderItemRepository;

@RestController
@RequestMapping(value="/order")
public class OrderController {
	@Autowired
	private OrderItemRepository orderRepository;

	@GetMapping(path="/add")
	public @ResponseBody String add(@RequestParam(value="productId") Long productId,
			                        @RequestParam(value="customerId") Long customerId) {
		OrderItem order = new OrderItem();
		order.setCustomerId(customerId);
		order.setProductId(productId);
		orderRepository.save(order);
		
		return order.getId().toString();
	}
	
	@GetMapping(path="/delete")
	public @ResponseBody String delete(@RequestParam(value="id") Long id) {
		orderRepository.delete(id);
		
		return "OK";
	}
	
	@GetMapping(path="/all")
	public @ResponseBody Iterable<OrderItem> getAllOrders() {
		return orderRepository.findAll();
	}
}
