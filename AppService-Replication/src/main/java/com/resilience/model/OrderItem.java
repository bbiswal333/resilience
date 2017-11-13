package com.resilience.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class OrderItem {
	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof OrderItem ) {
			OrderItem order = (OrderItem)obj;
			return productId.equals(order.productId) && customerId.equals(order.customerId);
		}
		return false;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private Long productId;
	private Long customerId;
	
	public OrderItem() {
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
}
