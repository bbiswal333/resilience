package com.resilience.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.resilience.model.SuccessfulOrderItem;
import com.resilience.model.repository.SuccessfulOrderItemRepository;

@RestController
@RequestMapping(value="/successfulorder")
public class SuccessfulOrderController {
	@Autowired
	private SuccessfulOrderItemRepository successfulOrderRepository;
	
	@GetMapping(path="/delete")
	public @ResponseBody String delete(@RequestParam(value="id") Long id) {
		successfulOrderRepository.delete(id);
		
		return "OK";
	}
	
	@GetMapping(path="/all")
	public @ResponseBody Iterable<SuccessfulOrderItem> getAllOrders() {
		return successfulOrderRepository.findAll();
	}
}
