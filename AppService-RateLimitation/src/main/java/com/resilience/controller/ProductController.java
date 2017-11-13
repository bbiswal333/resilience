package com.resilience.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.resilience.model.Product;
import com.resilience.model.repository.ProductRepository;

@RestController
@RequestMapping(value="/product")
public class ProductController {
	@Autowired
	private ProductRepository productRepository;

	@GetMapping(path="/add")
	public @ResponseBody String add(@RequestParam(value="name") String name,
			                        @RequestParam(value="quantity") Long quantity,
			                        @RequestParam(value="price") Double price) {
		Product product = new Product(name);
		product.setPrice(price);
		product.setQuantity(quantity);
		productRepository.save(product);
		
		return product.getId().toString();
	}
	
	@GetMapping(path="/delete")
	public @ResponseBody String delete(@RequestParam(value="id") Long id) {
		productRepository.delete(id);
		
		return "OK";
	}
	
	@GetMapping(path="/all")
	public @ResponseBody Iterable<Product> getAllOrders() {
		return productRepository.findAll();
	}
}
