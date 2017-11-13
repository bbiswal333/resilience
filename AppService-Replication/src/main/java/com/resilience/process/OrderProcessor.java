package com.resilience.process;

import javax.transaction.Transactional;

import com.resilience.RepositoryHandler;
import com.resilience.discount.DiscountCalculator;
import com.resilience.model.Customer;
import com.resilience.model.OrderItem;
import com.resilience.model.Product;
import com.resilience.model.SuccessfulOrderItem;

public class OrderProcessor implements Runnable {
	
	private RepositoryHandler handler;
	
	public OrderProcessor(RepositoryHandler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		while(true) {
			if ( handler.orderRepository != null ) {
				try {
					Iterable<OrderItem> orders = handler.orderRepository.findAll();
					orders.forEach(order -> process(order));
				} catch (Exception e) {
				}
			}
				
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Transactional
	private void process(OrderItem order) {
		
		Long productId = order.getProductId();
		Long customerId = order.getCustomerId();
		Product product = handler.productRepository.findOne(productId);
		Customer customer = handler.customerRepository.findOne(customerId);
		
		Double discount = DiscountCalculator.discountCalculator(productId, customerId);
		
		Double price = product.getPrice();
		customer.setAmount(customer.getAmount() - price * discount);
		product.setQuantity(product.getQuantity() - 1);
		
		handler.customerRepository.save(customer);
		handler.productRepository.save(product);
		
		SuccessfulOrderItem successfulItem = new SuccessfulOrderItem();
		successfulItem.setProductId(order.getProductId());
		successfulItem.setCustomerId(order.getCustomerId());
		successfulItem.setPrice(price);
		successfulItem.setDiscount(discount);
		
		handler.successfulOrderRepository.save(successfulItem);
		handler.orderRepository.delete(order);
	}
}
