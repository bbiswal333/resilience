# Pattern: Quarantine 

The pattern of quarantine can be used to isolate the execute of a request to a more controlled environment. If the usual execution is creating problems, then it can be put to a dedicated execution.
Action

The compute unit that can create problems shall be isolated. Usual execution is done inside the usual boundary conditions, but if a dedicated instance is identified, the instance for computation is delegated to a fallback implementation with dedicated resources.

![Quarantine principle](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/Quarantine.png)

The application after applying the pattern looks like this:

![Quarantine in example application](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/QuarantineRefApp.png)

## Applicable

Quarantine can only be done, if...

- ...unit of quarantine can be isolated.
- ...usual execution is not crashing the whole application

If the unit to quarantine is crashing the whole runtime, other strategies (supervisor) have to be applied to handle the fallback.
Principles

Out of the four principles of resilience the following are applied:

- Isolation: The compute unit is running with own resources, nothing is shared.
- Fallback: The implementation of the compute unit has to be available in a fallback implementation to push execution to it.
- Loose coupling: The compute unit provides a clear interface to allow easy execution.

## Used Patterns

The following patterns are used:

- Unit isolation: The fallback implementation uses a separated unit to compute.

## Implementation

Only the implementation of the command to trigger the computation is changed. The command is using the in-process execution to get the result, only in case there is an exception, the fallback to call an external service is used.

```Java
@Override
protected Double run() throws Exception {
  return DiscountCalculator.discountCalculator(productId, customerId);
}

@Override
protected Double getFallback() {
  logger.info("remote call for "+productId);
  Double ret = -1.0;
  StringBuilder result = new StringBuilder();
  BufferedReader rd = null;
  try {
    URL url = new URL("http://localhost:"+port+"/discount/value?customerId="+customerId+"&productId="+productId);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    ret = Double.valueOf(result.toString());
  } catch (NumberFormatException e) {
  } catch (MalformedURLException e) {
  } catch (ProtocolException e) {
  } catch (IOException e) {
  } finally {
    if ( rd != null )
      try {
        rd.close();
      } catch (IOException e) {
      }
  }
  return ret;
}
```
