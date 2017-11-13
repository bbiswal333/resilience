The pattern for shed load focuses on the handling of incoming requests to slow down the execution. Control the execution is required, because if too many requests are started, the process can consume too much resources and crash. Crashing the process would also mean that all other requests cannot be served any more.

The rate limitation goes to the overall problem, how many resources the system will consume to fulfill the tasks. In the past, the systems were designed in a way that they can handle the maximum load, i.e. having enough capacity to handle even the peaks. In cloud engineering, the paradigm has changed to tailor the system in a way that it can scale automatically. This requires that the load situation is known, i.e. there has to be an upfront calculation of the created load and consumed resources. The pattern for shed load does nothing more than rejecting or slowing down requests, if the calculation indicates that the incoming load will overload the system.

Defining a fixed rate limit is just the first step into a system that can tailor the load it can handle automatically. In cloud engineering, this is called elasticity. If the system has to provide elasticity or can survive with a fixed rate limit, depends on the business scenario and the effort that has to be put to the system. Usually, less automatism means that more manual interaction and operations is required.

# Action

As soon as possible the incoming requests are counted or analyzed to estimate the expected load of the runtime. Based on the available resources at runtime, a threshold is defined and requests are rejected or postponed for later execution.

![Ratelimiter principle](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/RateLimiter.png)

The application looks like the following after applying the pattern:

![Ratelimiter in example application](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/RateLimiterRefApp.png)

# Applicable

Shed load can only be done, if...

- ...execution time of a request can be calculated. If the execution time is not easy to calculate, it can become difficult to define a good threshold.

# Principles

Out of the four principles of resilience the following are applied:

- Isolation: The runtime is shielded from too many incoming requests.

# Implementation

The application itself is not changed. As said, the injection of the rate limiter has to be done as early as possible. For HTTP requests, it is possible to define a servlet filter:

```Java
@Configuration
public class FilterConfiguration {

  @Bean
  public FilterRegistrationBean DoSFilterBean() {
    final FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
    DoSFilter filter = new DoSFilter();
    filterRegBean.setFilter(filter);
    filterRegBean.addUrlPatterns("/*");
    filterRegBean.setEnabled(true);
    filterRegBean.setAsyncSupported(Boolean.TRUE);
    filterRegBean.addInitParameter("maxRequestsPerSec", "10");
    return filterRegBean;
  }
}
```

The filter implementation uses the way to inject the instance of the filter via a Spring configuration.
