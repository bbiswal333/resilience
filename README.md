The principles for resilience and high availability are defined at multiple places. Unfortunately, the principles are most of the times not described in more detail to allow developers to apply them in their coding.

In software development, the usual solution for this situation is to define patterns that can be used during development. This has happened in multiple areas, most prominently:

- Enterprise Integration Patterns: http://www.enterpriseintegrationpatterns.com/patterns/messaging/
- GoF Software Design Patterns: https://en.wikipedia.org/wiki/Design_Patterns

The way to develop software has changed after publishing of these patterns, because a developer is reading the patterns and understands, where they can be applied. The patterns are defined in a composable way, i.e. it is possible to take one pattern and combine it with another one. The consequence is that the developer does not have to solve all problems again and again. He can simply use well-defined patterns, check, if it is applicable, and can use it in development.

# Pattern approach

Now, the pattern approach can also be applied to the resilience area by defining a set of composable building blocks, called patterns.

One of the first challenges is that the patterns are different regarding their granularity. There are patterns that are well-covered in known libraries and only require one line of code to use them:

- Timeout: Most libraries provide a parameter in the call-method to define a timeframe, after that the execution is interrupted. After that, an information is provided, if the timeout has occurred or if the method was completed successful. It is up to consumer of the library to handle this properly.
- Retry: It is usually a wrapper around the call-method to define a retry strategy. This can be done linearly or with exponential backoff, but to call a method again and again is usually not such a big deal.

Such patterns are part of the usual toolbox of developers. For simplicity reasons, such patterns are called micropatterns, because it is quite simple to compose them with others and they are usable all over the place. This shows also the composable aspect of resilience patterns, because retry and timeout are definitely mandatory patterns for other patterns like unit isolation (to be explained in the resilience pattern for unit isolation).

# Definition of a pattern

The general structure of the pattern definition is done in the following way:

- Action: What has to be done to apply the pattern.
- Applicable: When is the pattern applicable.
- Principles: What resilience principles (isolation, loose coupling, fallback, redundancy) are used.
- Used patterns: What other patterns are used.

The patterns are explained based on an old-style classic application (database for datastorage, one process to handle requests). The application is very simple to focus on the resilience pattern currently in scope.

![Overview of example application](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/OverviewRefApp.png)

Sources are available at: https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/AppServiceSimple

# Available patterns

The patterns can be grouped according to the focus areas of resilience:

- Failure Unit:
  - Unit Isolation (https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/AppService-UnitIsolation)
- Data Handling:
  - Temporary Replication (https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/AppService-Replication)
  - Temporary Data
- Control Circuit:
  - Quarantine (https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/AppService-Quarantine)
  - Supervisor (https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/Supervisor)
- Rate Limitation:
  - Shed load (https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/AppService-RateLimitation)
  - Bounded Queues (https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/AppService)
- Dependency Management
  - Circuit Breaker (https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/CircuitBreaker.md)
  - Retry (https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Retry.md)
  - Timeout (https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Timeout.md)
  - Watchdog (https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Watchdog.md)
