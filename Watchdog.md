# Pattern: Watchdog

The resilience pattern of a watch dog focuses on the behavior of a second component. The component that is called a watch dog, monitors the behavior of the other component that is under observation. If some predefined metrics get values that are beyond a specific threshold, the watch dog component can trigger appropriate actions.

The pattern is connected to the supervisor pattern, but the supervisor defines that the components under supervision are under control of the supervisor from lifecycle point of view, i.e. the supervisor can start or stop the supervised components. The watch dog is just getting some data from the other components.

## Action

Metrics for a component are defined and externally accessable. Another component, the watch dog, can get the values.

## Applicable

Watch dog can only be done, if...

- ...values for metrics are externalized.
- ...specific thresholds and actions are defined that has to be taken, once the thresholds are reached.

## Principles

Out of the four principles of resilience the following are applied:

- Isolation: The watch dog component is controlling the isolated component.

## Implementation

The supervisor component contains also some watch dog functionality:

```Java
public static void main(String[] args) {
  start();
  while(true) {
    while(!queue.isEmpty()) {
      logger.info(queue.poll());
    }

    watchdog(8081);
    watchdog(8082);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

private static void watchdog(long port) {
  try {
    StringBuilder result = new StringBuilder();
    URL url = new URL("http://localhost:"+port+"/discount/requests");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    logger.info("Requests for "+port+": " + result.toString());
    rd.close();
  } catch (MalformedURLException e1) {
  } catch (ProtocolException e1) {
  } catch (IOException e1) {
  }
}
```

The watch dog is doing just a repeated call, how many requests were done at the remote component. The functionality to return the number of requests is done via a REST-endpoint:

```Java
@GetMapping(path = "/requests")
public @ResponseBody Long requests() {
  return new Long(requests);
}
```

Sources are available at:

https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/Supervisor

https://github.wdf.sap.corp/cloud-native-dev/resilience/tree/master/DiscountCalculator-Supervised
