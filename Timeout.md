The pattern for timeout is solving the problem of blocking calls to external services. Usually, it is not really predictable, how long a call will take, and sometimes it can be that the call takes a very long time or even does not return. To provide a service for endusers that is useful, such a behavior is most of the time not acceptable. The timeout is most of the time a setting that defines, when the call shall be interrupted to handle the situation.

The timeout is slightly connected with other patterns like retry or circuit breaker, because all these patterns define the handling of calls to external systems.

# Action

The call to an external system is instrumented with a time window. If the call time is longer, the call operation will be interrupted.

# Applicable

Timeout can only be done, if...

- ...execution time of a request is predictable. If the response time is completely erratic, the calling side gets a problem to react appropriately (no definition of a time limit possible).

# Principles

Out of the four principles of resilience the following are applied:

- Loose Coupling: The caller is decoupled from the execution in the remote system. If something happens on the remote side, the caller is not blocked any more.

# Implementation

The execution of the call is wrapped by a thread that can be interrupted after a dedicated time period.

```Java
final ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Double> future = executor.submit(new ExecuteRequest(customerId.toString(), productId.toString()));
ret = future.get(30, TimeUnit.MILLISECONDS);
```
