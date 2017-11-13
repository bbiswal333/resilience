package com.resilience.model.repository;

import org.springframework.data.repository.CrudRepository;

import com.resilience.model.OrderItem;

public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {

}
