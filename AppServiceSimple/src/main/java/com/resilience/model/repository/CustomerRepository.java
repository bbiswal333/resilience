package com.resilience.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resilience.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
