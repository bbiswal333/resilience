package com.resilience.model.repository;

import org.springframework.data.repository.CrudRepository;

import com.resilience.model.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {

}
