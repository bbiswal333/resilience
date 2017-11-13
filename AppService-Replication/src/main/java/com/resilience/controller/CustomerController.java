package com.resilience.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.resilience.model.Customer;
import com.resilience.model.repository.CustomerRepository;

@RestController
@RequestMapping(value="/customer")
public class CustomerController {
	@Autowired
	public CustomerRepository customerRepository;

	@GetMapping(path="/add")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public @ResponseBody String add(@RequestParam(value="name") String name,
			                        @RequestParam(value="amount") Double amount) {
		Customer customer = new Customer(name);
		customer.setAmount(amount);
		customerRepository.save(customer);
		customerRepository.flush();
		
		return customer.getId().toString();
	}
	
	@GetMapping(path="/delete")
	public @ResponseBody String delete(@RequestParam(value="id") Long id) {
		customerRepository.delete(id);
		
		return "OK";
	}
	
	@GetMapping(path="/all")
	public @ResponseBody Iterable<Customer> getAllOrders() {
		return customerRepository.findAll();
	}
}
