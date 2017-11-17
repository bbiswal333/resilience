# Pattern: Unit Isolation
The pattern for unit isolation is focusing on one of the core decisions of resilience: the design of the failure unit. A failure unit is the entity of an application that can fail without impacting the overall availability of the whole application. During the design process of the application, it is crucial to define a split of the functionality to avoid the monolithic architecture design.

The overall problem for unit isolation is to find a good balance of the isolated entities. Nowadays, different methodologies like domain-driven design are applied to define the cut of units, but in the moment it looks more like an art than a science, i.e. it depends on many circumstances and boundary conditions. Therefore, it is a clear recommendation to assess multiple options of isolation and not apply the pattern everywhere.

## Action

The compute units are split during the failure unit design, because each run of a compute unit can crash and bring down the whole application. If the application is implemented as one monolith one action can influence all others.
As a consequence, the communication to compute unit is remote, and if the synchronous call crashes, the request on the caller side gets an exception. So, the caller can handle the error situation.

![Principle of Unit Isolation](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/UnitIsolation.png)

It makes a lot of sense that the patterns for retry and timeout are applied here, because the remote call is a good isolation for the compute unit, but it has to be handled in an appropriate way.
Only the compute unit is a failure unit after applying the pattern, because nothing is changed for the overall application, i.e. if the application is only running with one instance, the application does not have a fallback instance.

The application looks like this after applying the pattern:

![Example application after isolation](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/UnitIsolationRefApp.png)

## Applicable

Separation of some functionality can only be done, if...

- ...task can be separated in an isolated way. This is usually the case for compute-intensive operations or non-mandatory information.
- ...the size of the data that is required for the computation is not too huge, i.e. overloading the network.

An anti-pattern in this context is to simply pass a reference to an external storage that contains the huge data set, because passing such references implies coupling on data level. It can be done, but to isolate the compute functionality, but couple on data level does only half of the job.

## Principles

Out of the four principles of resilience the following are applied:

- Isolation: The compute unit is isolated from the remaining parts of the application. There is a clear boundary between the units.
- Decoupling: The isolated compute unit defines an interface, and this interface is used for decoupling from the remaining parts.
- Redundancy: Usually unit isolation is applied to allow failover to a secondary runtime instance.

## Used Patterns

The following patterns are used:

- Retry: Each call to a compute unit has to be wrapped by a retry logic.
- Timeout: Each call has to define a timeout, otherwise the remaining parts of the application could be blocked forever.

## Implementation

The application contains business logic to handle orders:

```Java
@Transactional
private void process(OrderItem order) {
  Long productId = order.getProductId();
  Long customerId = order.getCustomerId();
  Product product = handler.productRepository.findOne(productId);
  Customer customer = handler.customerRepository.findOne(customerId);
 
  // compute-intensive call
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
```

All database operations are running in a transaction (see annotation @Transactional). The important statement is the call to the compute-intensive functionality (`getDiscount()`).

The isolation of the compute unit is now done in a way that the functionality is separated to another process. With modern development frameworks it is not such a big deal to extract the logic to a separate process, but the challenge is to handle the call in the appropriate way.

The remote call is isolated to method calls:

```Java
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
```

The call is separated to a Java-future to allow interruption of the call (see timeout parameter) and the whole call is wrapped into a retry-loop to allow calls. It is also important to notice what happens in an exceptional case: The URL is changed, because the call should go to another process. This implies that at least two instances of the isolated process for discount calculation are running.
