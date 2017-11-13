package com.resilience.discount;

import java.util.Random;

public class DiscountCalculator {
	public static Double discountCalculator(Long productId, Long customerId) {
		Random r1 = new Random(productId);
		Random r2 = new Random(customerId);
		
		Double d1 = r1.nextDouble();
		Double d2 = r2.nextDouble();
		
		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return (d1+d2)/2.0;
	}
}
