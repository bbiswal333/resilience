# Pattern: Retry

The resilience pattern for retry is not very complex, but highly recommended to allow a successful execution of a task. The underlying assumption is that the execution of a task can succeed, if another attempt is done. For attempts within the same process, it can be that the assumption is not valid, but for remote calls, the probability is given that the call will succeed, because of network problems or problems in the external systems.

There are multiple ways to handle a retry. The simplest way would be to trigger the execution simply a second time after a fixed period of time. This can be varied in a way that exponential backoff is applied, i.e. the time after that the execution is triggered again is increased in an exponential way.

Idempotency is a concept in this context, meaning that a call can be done multiple times without changing the status again and again.

## Action

The execution of a task is surrounded by a retry logic.

## Applicable

Retry can only be done, if...

- ...the first attempt to trigger a task does not have side effects. If there are side effects, the second and all other subsequent execution attempts are changing the status of the system. The behavior could be unpredictable.

## Principles

Out of the four principles of resilience the following are applied:

- Redundancy: The execution is triggered multiple times.

## Implementation

In the example, the call to an external system is wrapped with a loop that is triggering the retry of the call.

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

private int triggerRetry(int retry) {
  retry--;
  changePort();
  logger.info("Retry with "+(6-retry)+". attempt and port "+port);
  return retry;
}
```

The retry is done after a fixed limit of retries.
