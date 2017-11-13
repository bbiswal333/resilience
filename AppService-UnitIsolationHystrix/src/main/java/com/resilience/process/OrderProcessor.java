package com.resilience.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.resilience.RepositoryHandler;
import com.resilience.model.Customer;
import com.resilience.model.OrderItem;
import com.resilience.model.Product;
import com.resilience.model.SuccessfulOrderItem;

public class OrderProcessor implements Runnable {
	
	private RepositoryHandler handler;
	private long port = 8081;
	private final static int RETRY = 5;
	
	private static Logger logger = Logger.getLogger(OrderProcessor.class);
	
	public OrderProcessor(RepositoryHandler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		while(true) {
			if ( handler.orderRepository != null ) {
				Iterable<OrderItem> orders = handler.orderRepository.findAll();

				orders.forEach(order -> process(order));
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
		
		Double discount = getDiscount(productId, customerId);
		
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
	
	private Double getDiscount(Long productId, Long customerId) {
		Double ret = 1.0;
		int retry = RETRY;
		while(retry > 0) {
			try {
				ExecuteRequest request = new ExecuteRequest(customerId.toString(), productId.toString());
				Future<Double> future = request.queue();
				ret = future.get();
				if ( ret >= 0)
					retry = 0;
				else
					retry = triggerRetry(retry);
			} catch (InterruptedException e) {
				retry = triggerRetry(retry);
			} catch (ExecutionException e) {
				retry = triggerRetry(retry);
			}
		}
		return ret;
	}

	private int triggerRetry(int retry) {
		retry--;
		changePort();
		logger.info("Retry with "+(6-retry)+". attempt and port "+port);
		return retry;
	}
	
	private class ExecuteRequest extends HystrixCommand<Double> {
		private String customerId;
		private String productId;
		
		public ExecuteRequest(String customerId, String productId) {
	        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RequestGroup")).andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(30)));
			this.customerId = customerId;
			this.productId = productId;
		}

		@Override
		protected Double run() throws Exception {
			Double ret;
			StringBuilder result = new StringBuilder();
			URL url = new URL("http://localhost:"+port+"/discount/value?customerId="+customerId+"&productId="+productId);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			ret = Double.valueOf(result.toString());
			rd.close();
			return ret;
		}
		
		@Override
		protected Double getFallback() {
			return -1.0;
		}
	}

	private void changePort() {
		port = port == 8081 ? 8082 : 8081;
	}
}
