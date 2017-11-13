The pattern of supervisor is used to control the execution of failure units. Sometimes, it can happen that the unit of work cannot recover on its own, because the unit simply repeats the task again and again. This could potentially lead to endless cycles. Now, an external entity can get that task to control the behavior and execution of the failure unit. If something unexpected happens, the supervisor becomes active and recovers the runtime by restarting, rescheduling the work or any other appropriate action.
Supervisor is a concept that was developed even some time ago. The programming language Erlang, created 1987, made it part of the core key words of the language. The idea is to define a language that is exactly meant to provide an enviroment for high-available systems. Other languages, like Scala, have taken over the concepts, called actors, to provide exactly the same advantages.
Action

The application and all failure units are not impacted by the implementation of a supervisor. A supervisor is just an external component that is controlling the instances. The actions that are taken can include complete restart or fine-tuning of the instances.

![Supervisor principle](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/Supervisor.png)

The application looks like the following after applying the pattern:

![Supervisor in example application](https://github.wdf.sap.corp/cloud-native-dev/resilience/blob/master/Images/SupervisorRefApp.png)

# Applicable

Supervisor is useful, if...

- ...the control of the failure units cannot be done within the unit itself. Sometimes, it can be that the unit is working in a way that correct calculation of resource consumption is not possible. To interrupt such task, a supervisor is a good approach, because the supervisor is not impacted by any work inside the failure unit.
- ...the failure unit can be separated in a good way (clear interface).
- ...external control is feasible, because all mandatory metrics are available.

# Principles

Out of the four principles of resilience the following are applied:

- Isolation: The supervisor is an isolated component to control the other failure units.

# Used Patterns

The following patterns are used:

- Unit isolation: The failure unit implementation is isolated to allow partial recovery.
- Watch dog: A watch dog retrieves information about the failure unit under supervision.

# Implementation

The implementation of the supervisor can be rather simple:

```Java
public static void main(String[] args) {
  start();
  while(true) {
    while(!queue.isEmpty()) {
      logger.info(queue.poll());
    }

    watchdog(8081);
    watchdog(8082);
  }
}
```

First of all, the supervisor takes care for starting the failure units that are controlled. For simplicity reasons, the start of the observed process is done as a command:

```Java
java -Dserver.port=<port number> -jar <executable>
```

The functionality in Java to handle external processes is used and handled in a special implementation class:

```Java
public class NodeObserver implements Runnable {

public NodeObserver(int port, String executable, ArrayBlockingQueue<String> queue) {
  ...
}

@Override
public void run() {
  Process p;
  while(true) {
    try {
      p = Runtime.getRuntime().exec(new String[] {"java", "-Dserver.port="+port, "-jar", executable});
      try {
        p.waitFor();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      queue.put(port+" crashed -> restarting");
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
```

The process is started and running. Only in case, the process crashes, a restart is triggered and a message is putting into a queue.
