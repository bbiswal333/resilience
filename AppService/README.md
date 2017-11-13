The resilience pattern for bounded queue is based on the assumption computing resources are not endless. It is not possible to consume infinite memory or CPU-capacity to handle the requests. If the requests are created, the system has to calculate, how many requests can be served at once. This defines the length of the queue.

Introduction of a queue brings the implementation closer to an asynchronous processing paradigm, because the queue holds the items. Only, if the execution can be done, it is triggered.

# Action

A queue is introduced to handle the requests. The behavior is changed from synchronous request processing to asynchronous.

![Bounded queue principle](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/BoundedQueue.png)

# Applicable

Bounded queue can only be done, if...

- ...the data for a dedicated task can be separated in a task object.

# Principles

Out of the four principles of resilience the following are applied:

- Loose Coupling: The request acceptance and the execution is decoupled.

Implementation

The processing of a request is directly done in the controller:

```Java
@GetMapping(path="/add")
public @ResponseBody String add(@RequestParam(value="productId") Long productId,
@RequestParam(value="customerId") Long customerId) { 
  Product product = productRepository.findOne(productId);
  Customer customer = customerRepository.findOne(customerId);

  Double discount = DiscountCalculator.discountCalculator(productId, customerId);

  Double price = product.getPrice();
  customer.setAmount(customer.getAmount() - price * discount);
  product.setQuantity(product.getQuantity() - 1);

  customerRepository.save(customer);
  productRepository.save(product);

  SuccessfulOrderItem successfulItem = new SuccessfulOrderItem();
  successfulItem.setProductId(productId);
  successfulItem.setCustomerId(customerId);
  successfulItem.setPrice(price);
  successfulItem.setDiscount(discount);

  orderRepository.save(successfulItem);

  return successfulItem.getId().toString();
}
```

Introducing a queue leads to two changes in the coding:

- Putting the processing request to a queue
- Taking tasks from the queue and process them

The introduction of a queue is done here:

```Java
@GetMapping(path="/add")
public @ResponseBody String add(@RequestParam(value="productId") Long productId,
@RequestParam(value="customerId") Long customerId) {
  OrderItem order = new OrderItem();
  order.setCustomerId(customerId);
  order.setProductId(productId);
  orderRepository.save(order);

  return order.getId().toString();
}
```

The table for orders simply contains the orders for later execution. Only in case it was successful, it will be removed.

```Java
@Override
public void run() {
  while(true) {
    if ( handler.orderRepository != null ) {
      Iterable<OrderItem> orders = handler.orderRepository.findAll();

      orders.forEach(order -> process(order));
    }
  }
}
```

The processing itself is similar, but there is a loop that takes elements from the queue to control the execution.

Side remark: A database table is usually not the first choice to implement a queue. There are multiple message brokers available that are suited for such use cases.
