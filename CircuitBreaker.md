The resilience pattern for Circuit Breaker is sometimes seen as the most important pattern. The reason for that is that the pattern tries to mitigate one of the shortcomings of a distributed systems. Nowadays, the software systems are growing and usually do not handle a request without contacting another system. The problem with contacting an external system is that it is rather difficult to get information about the status of the external system. It can be that the external system is overloaded and not responding. Or the response time is fluctuating.

The circuit breaker now tackles these problems by introducing a kind of circuit for each external dependency. The external request is tracked, the response time and for sure the answer. If there is a problem identified, the circuit on the caller side will control the behavior of the calls in future. To give an example: If the response time goes up, the external system could be under heavy load and each call can increase the load. Sooner or later the external system will crash. To avoid the crash, the circuit can be configured in a way that the calls will not be done, if the response time goes beyond a specific limit. The circuit gets open.

As the description already indicates, the configuration of all parameters of a circuit gets rather difficult. There is no ultimate set of configuration values that works always. It depends on the external system, the call itself, and even the network latency. So, the best recommendation would be to introduce a circuit breaker, measure the usual behavior, and get a good understanding to fine-tune the configuration settings.

# Action

The external call to the remote system shall be wrapped to allow the measurement of the external call including response time, error code, etc.

# Applicable

Circuit breakers are applicable, if...

- ...the call goes to a dedicated endpoint.

# Principles

Out of the four principles of resilience the following are applied:

- Isolation: The call can be isolated.

# Implementation

The implementation of a circuit breaker is best explained with the example of a Hystrix command. Hystrix is an open source framework, especially designed for such remote call dependencies.

```Java
private class ExecuteRequest extends HystrixCommand<Double> {
  private String customerId;
  private String productId;

  public ExecuteRequest(String customerId, String productId) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RequestGroup")).andCommandPropertiesDefaults(
            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(30)));
    this.customerId = customerId;
    this.productId = productId;
  }

  @Override
  protected Double run() throws Exception {
    ...
  }

  @Override
  protected Double getFallback() {
    ...
```

The request to the external system is isolated to a Hystrix command providing two methods. The method run handles the usual call, and the method getFallback is providing the response, if the circuit is open, i.e. the external system has a problem. To define the criteria to open the circuit, multiple configurations can be done in the constructor of the command. In the example, only the timeout is set.
