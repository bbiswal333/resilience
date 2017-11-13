package com.resilience.model.repository;

import org.springframework.data.repository.CrudRepository;

import com.resilience.model.SuccessfulOrderItem;

public interface SuccessfulOrderItemRepository extends CrudRepository<SuccessfulOrderItem, Long> {

}
