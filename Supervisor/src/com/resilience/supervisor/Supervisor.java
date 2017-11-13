package com.resilience.supervisor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

public class Supervisor {

	private static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(100);

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("DEFAULT");
	
	public static void main(String[] args) {
		start();
		while(true) {
			while(!queue.isEmpty()) {
				logger.info(queue.poll());
			}
			
			watchdog(8081);
			watchdog(8082);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void watchdog(long port) {
		try {
			StringBuilder result = new StringBuilder();
			URL url = new URL("http://localhost:"+port+"/discount/requests");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			logger.info("Requests for "+port+": " + result.toString());
			rd.close();
		} catch (MalformedURLException e1) {
		} catch (ProtocolException e1) {
		} catch (IOException e1) {
		}
	}

	private static void start() {
		Thread t1 = new Thread(new NodeObserver(8080, "/Users/d028547/git/ResilienceApp/AppService-UnitIsolationHystrix/target/demo-0.0.1-SNAPSHOT.jar", queue));
		t1.start();
		Thread t2 = new Thread(new NodeObserver(8081, "/Users/d028547/eclipse-workspaces/ResiliencePattern/DiscountCalculator-Supervised/target/DiscountCalculator-0.0.1-SNAPSHOT.jar", queue));
		t2.start();
		Thread t3 = new Thread(new NodeObserver(8082, "/Users/d028547/eclipse-workspaces/ResiliencePattern/DiscountCalculator-Supervised/target/DiscountCalculator-0.0.1-SNAPSHOT.jar", queue));
		t3.start();
	}
}
