package com.resilience.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;

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
				final ExecutorService executor = Executors.newSingleThreadExecutor();
				Future<Double> future = executor.submit(new ExecuteRequest(customerId.toString(), productId.toString()));
				ret = future.get(30, TimeUnit.MILLISECONDS);
				retry = 0;
			} catch (TimeoutException e) {
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
	
	private class ExecuteRequest implements Callable<Double> {
		private String customerId;
		private String productId;
		
		public ExecuteRequest(String customerId, String productId) {
			this.customerId = customerId;
			this.productId = productId;
		}

		public Double call() throws Exception {
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
	}

	private void changePort() {
		port = port == 8081 ? 8082 : 8081;
	}
}
