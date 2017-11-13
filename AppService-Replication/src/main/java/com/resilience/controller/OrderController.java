package com.resilience.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.resilience.model.OrderItem;
import com.resilience.model.repository.OrderItemRepository;

@RestController
@RequestMapping(value="/order")
public class OrderController {
	
	private OrderReplicator replicator;
	
	public OrderController() {
		replicator = new OrderReplicator();
		Thread processor = new Thread(replicator);
		processor.start();
	}
	
	@Autowired
	private OrderItemRepository orderRepository;

	@GetMapping(path="/add")
	public @ResponseBody String add(@RequestParam(value="productId") Long productId,
			                        @RequestParam(value="customerId") Long customerId) {
		OrderItem order = new OrderItem();
		order.setCustomerId(customerId);
		order.setProductId(productId);
		
		SaveRequest request = new SaveRequest(order);
		request.execute();
		
		if ( request.isResponseFromFallback() )
			return "";
		
		return order.getId().toString();
	}
	
	private List<OrderItem> list = new ArrayList<OrderItem>();
	
	public class OrderReplicator implements Runnable {
		public OrderReplicator() {
		}

		@Override
		public void run() {
			while(true) {
				if ( list.size() > 0 ) {
					ArrayBlockingQueue<OrderItem> queue;
					synchronized(list) {
						queue = new ArrayBlockingQueue<OrderItem>(list.size(), true, list);
					}
					queue.forEach(order -> process(order));
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void process(OrderItem order) {
			SaveRequest request = new SaveRequest(order);
			request.queue();
		}
	}
	
	private class SaveRequest extends HystrixCommand<String> {
		private OrderItem order;
		
		public SaveRequest(OrderItem order) {
	        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RequestGroup")).andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1000)));
			this.order = order;
		}

		@Override
		protected String run() throws Exception {
			orderRepository.save(order);
			synchronized(list) {
				if ( list.contains(order))
					list.remove(order);
			}
			return order.getId().toString();
		}
		
		@Override
		protected String getFallback() {
			synchronized(list) {
				if ( !list.contains(order))
					list.add(order);
			}
			return "";
		}
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
