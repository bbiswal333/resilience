# Pattern: Temporary Replication

The resilience pattern for a temporary replication is solving the issue of inheriting the availability of a used storage technology. The underlying problem is that an application can never be better regarding availability, if the application is coupled to the storage, i.e. each call to the application results in calls to the storage and if the storage is not available, the application cannot work. For this purpose, another storage technology can make sense.

Temporary replication focuses on one of the difficult problems of resilient applications, because the data consistency has to be handled. In monolithic software architectures, the server was using a relational database and followed the ACID paradigm, but if the server load grows beyond a specific limit, it is not possible for the database to handle the load anymore. The load has to be scaled out, and the CAP theorem becomes important.

The CAP theorem defines that only two out of three qualities can be achieved:
- Consistency
- Availability
- Partition-tolerance

In a distributed system like it is required to handle the load, there is partitioning. The distributed system usually has to provide availability. This means that the consistency has to be compromised. Nowadays the term eventual consistency becomes very prominent, because it means that the different instances of the data storage can deliver different information according to the time, the data is requested. Only after a dedicated timeframe, consistency is achieved again.

Introducing a temporary storage to mitigate the downtime of the primary storage is one way to achieve availability of the overall service with the problem to make the data consistent afterwards.

As a general recommendation, it has to be assessed what qualities the system has to provide under the given boundary conditions. Again, it is an assessment that can only be done case by case.

## Action

There is a fallback storage to persist information and replicate later. For this purpose, a circuit breaker is used and the fallback implementation redirects to the temporary storage.

Once the primary storage is available again, the data from the temporary storage is transferred to the primary storage. This is done in an asynchronous loop, running periodically in the application.

![Replication principle](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/TemporaryReplication.png)

The application is changed in the following way.

![Replication in example application](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/TemporaryReplicationRefApp.png)

## Applicable

Using a secondary storage is an option, if...

- ...persisting data is not required for later business logic steps (storage can be deferred).
- ...temporary storage does not loss data (potentially it could be allowed to lose data, business decision).

## Principles

Out of the four principles of resilience the following are applied:

- Fallback: If the primary storage is not available, a secondary storage is used as fallback.
- Redundancy: The secondary storage is a redundant place to store data.

## Used Patterns

The following patterns are used:

- Circuit breaker: If the connection to the primary storage is not available, the circuit gets open and will only be closed, once it is available again.

## Implementation

The implementation of the resilience pattern involves two major pieces:

- Fallback to temporary storage
- Replication to primary storage

## Fallback

The call to the primary storage has to be wrapped to allow the fallback implementation. This is done via a dedicated instance of the call request.

```Java
@GetMapping(path="/add")
public @ResponseBody String add(@RequestParam(value="productId") Long productId,
@RequestParam(value="customerId") Long customerId) {
  OrderItem order = new OrderItem();
  order.setCustomerId(customerId);
  order.setProductId(productId);

  SaveRequest request = new SaveRequest(order);
  request.execute();

  if ( request.isResponseFromFallback() )
    return "";

  return order.getId().toString();
}

private class SaveRequest extends HystrixCommand<String> {
  private OrderItem order;

  public SaveRequest(OrderItem order) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RequestGroup")).andCommandPropertiesDefaults(
      HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1000)));
    this.order = order;
  }

  @Override
  protected String run() throws Exception {
    orderRepository.save(order);
    synchronized(list) {
      if ( list.contains(order))
        list.remove(order);
    }
    return order.getId().toString();
  }

  @Override
  protected String getFallback() {
    synchronized(list) {
      if ( !list.contains(order))
        list.add(order);
    }
    return "";
  }
}
```

The Hystrix framework provides a good foundation to allow a fallback implementation and timeout behavior. The fallback implementation is simply adding the order request to an in-memory list for later replication (this is not a reliable storage overall, because if the process crashes, all data is gone).

## Replication

The data in the secondary storage has also to be replicated to the primary storage. A separate thread is used for that.

```Java
private List<OrderItem> list = new ArrayList<OrderItem>();

public class OrderReplicator implements Runnable {

  @Override
  public void run() {
    while(true) {
      if ( list.size() > 0 ) {
        ArrayBlockingQueue<OrderItem> queue;
        synchronized(list) {
          queue = new ArrayBlockingQueue<OrderItem>(list.size(), true, list);
        }
        queue.forEach(order -> process(order));
      }

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void process(OrderItem order) {
    SaveRequest request = new SaveRequest(order);
    request.queue();
  }
}
```
