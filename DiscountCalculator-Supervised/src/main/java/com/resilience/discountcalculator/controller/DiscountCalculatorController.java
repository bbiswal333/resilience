package com.resilience.discountcalculator.controller;

import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/discount")
public class DiscountCalculatorController {
	
	private static long requests = 0;

	@GetMapping(path = "/value")
	public @ResponseBody String add(@RequestParam(value = "productId") Long productId,
									@RequestParam(value = "customerId") Long customerId) {
		requests++;
		
		Random r1 = new Random(productId);
		Random r2 = new Random(customerId);

		Double d1 = r1.nextDouble();
		Double d2 = r2.nextDouble();

		try {
			Thread.sleep(15+calculateOverhead());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return (new Double((d1 + d2) / 2.0)).toString();
	}
	
	@GetMapping(path = "/requests")
	public @ResponseBody Long requests() {
		return new Long(requests);
	}

	private int calculateOverhead() {
		Random timeout = new Random();
		float value = timeout.nextFloat();
		int overhead = 0;
		if ( value > 0.95 ) {
			overhead = 100;
		}
		return overhead;
	}
}
