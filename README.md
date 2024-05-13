

## Build
./gradlew clean build

the agent needs to compile for java 8. to prevents hassle in Idea configure project jdk to 8 and gradle jdk to 21.





## debugging the ageng with remote debug
sometimes its necessary to debug the agent premain and transformers during development.<br>
to be able to remote debug the agent in an application in intellij the application needs the agent
jar in the class path.<br>
assuming your local digma agent project is in /home/shalom/workspace/digma/digma-agent/ and the jar builds to
/home/shalom/workspace/digma/digma-agent/build/libs/digma-agent-1.0.8-SNAPSHOT.jar<br>
add that to the application gradle dependencies:<br>
```
runtimeOnly(files('/home/shalom/workspace/digma/digma-agent/build/libs/digma-agent-1.0.8-SNAPSHOT.jar'))
```
refresh gradle. after its loaded you can open class DigmaAgent, intellij will suggest to attach source code, point it to the 
agent project main source folder:
```/home/shalom/workspace/digma/digma-agent/src/main/java```
<br>
to be able to stop at breakpoints in the agent's premain the debugger agent must be listed before the digma agent.
to do that add JAVA_TOOL_OPTIONS to the application's launcher:<br>
```JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005```
the plugin will keep that and merge it wil the JAVA_TOOL_OPTIONS for instrumentation.<br>
put a breakpoint in DigmaAgent premain<br>
create a new remote debug launcher and make sure the ports match.<br>
run the application, the debugger should immediately wait for remote debugger to attach.<br>
start the remote debug launcher.
the debugger will stop at the breakpoint in premain, and you can debug any part of the agent.<br>
don't forget to remove it all when finished developing.<br>


## Known error logs messages
When digma agent and otel agent are on the same application otel agent will emit some debug logs with stacktrace that look like errors but 
are actually just debug and can be ignored.<br>
errors like:<br>
```Cannot resolve type description for org.digma.net.bytebuddy.agent.builder.$Proxy31```
see https://github.com/open-telemetry/opentelemetry-java-instrumentation/discussions/11336#discussioncomment-9419936



