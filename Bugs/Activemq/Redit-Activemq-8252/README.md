# Redit-ActiveMQ-8252

### Details

Title: Unnecessary stack trace in case of invalid credentials


|         Label         | Value  |      Label      |  Value   |
|:---------------------:|:------:|:---------------:|:--------:|
|       **Type**        |  Bug   |  **Priority**   |  Major   |
|      **Status**       | Fixed  | **Resolution**  | Resolved |
| **Affects Version/s** | 5.16.2 | **Component/s** |  STOMP   |

### Description

In case an invalid credential is used with STOMP, we get an unnecessary stack trace:

```

2021-04-28T09:17:07.020+0200 [ActiveMQ NIO Worker 0] WARN TransportConnection - Failed to add Connection id=ID:foo-29852-1619594148588-1:17, clientId=ID:foo-29852-1619594148588-1:17, clientIP=tcp://10.0.0.1:50940 due to User name [system] or password is invalid.
2021-04-28T09:17:07.020+0200 [ActiveMQ NIO Worker 0] WARN Service - Security Error occurred on connection to: tcp://10.0.0.1:50940, User name [system] or password is invalid.
2021-04-28T09:17:07.029+0200 [ActiveMQ NIO Worker 0] WARN Transport - Transport Connection to: tcp://10.0.0.1:50940 failed
java.io.IOException: User name [system] or password is invalid.
	at org.apache.activemq.util.IOExceptionSupport.create(IOExceptionSupport.java:40)
	at org.apache.activemq.transport.stomp.ProtocolConverter$3.onResponse(ProtocolConverter.java:785)
	at org.apache.activemq.transport.stomp.ProtocolConverter.onActiveMQCommand(ProtocolConverter.java:865)
	at org.apache.activemq.transport.stomp.StompTransportFilter.oneway(StompTransportFilter.java:72)
	at org.apache.activemq.transport.AbstractInactivityMonitor.doOnewaySend(AbstractInactivityMonitor.java:335)
	at org.apache.activemq.transport.AbstractInactivityMonitor.oneway(AbstractInactivityMonitor.java:317)
	at org.apache.activemq.transport.MutexTransport.oneway(MutexTransport.java:68)
	at org.apache.activemq.broker.TransportConnection.dispatch(TransportConnection.java:1480)
	at org.apache.activemq.broker.TransportConnection.processDispatch(TransportConnection.java:977)
	at org.apache.activemq.broker.TransportConnection.dispatchSync(TransportConnection.java:933)
	at org.apache.activemq.broker.TransportConnection$1.onCommand(TransportConnection.java:202)
	at org.apache.activemq.transport.MutexTransport.onCommand(MutexTransport.java:45)
	at org.apache.activemq.transport.AbstractInactivityMonitor.onCommand(AbstractInactivityMonitor.java:301)
	at org.apache.activemq.transport.stomp.StompTransportFilter.sendToActiveMQ(StompTransportFilter.java:97)
	at org.apache.activemq.transport.stomp.ProtocolConverter.sendToActiveMQ(ProtocolConverter.java:179)
	at org.apache.activemq.transport.stomp.ProtocolConverter.onStompConnect(ProtocolConverter.java:777)
	at org.apache.activemq.transport.stomp.ProtocolConverter.onStompCommand(ProtocolConverter.java:254)
	at org.apache.activemq.transport.stomp.StompTransportFilter.onCommand(StompTransportFilter.java:85)
	at org.apache.activemq.transport.TransportSupport.doConsume(TransportSupport.java:83)
	at org.apache.activemq.transport.stomp.StompCodec.processCommand(StompCodec.java:133)
	at org.apache.activemq.transport.stomp.StompCodec.parse(StompCodec.java:100)
	at org.apache.activemq.transport.stomp.StompNIOTransport.processBuffer(StompNIOTransport.java:136)
	at org.apache.activemq.transport.stomp.StompNIOTransport.serviceRead(StompNIOTransport.java:121)
	at org.apache.activemq.transport.stomp.StompNIOTransport.access$000(StompNIOTransport.java:44)
	at org.apache.activemq.transport.stomp.StompNIOTransport$1.onSelect(StompNIOTransport.java:73)
	at org.apache.activemq.transport.nio.SelectorSelection.onSelect(SelectorSelection.java:98)
	at org.apache.activemq.transport.nio.SelectorWorker$1.run(SelectorWorker.java:123)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: java.lang.SecurityException: User name [system] or password is invalid.
	at org.apache.activemq.security.JaasAuthenticationBroker.authenticate(JaasAuthenticationBroker.java:97)
	at org.apache.activemq.security.JaasAuthenticationBroker.addConnection(JaasAuthenticationBroker.java:68)
	at org.apache.activemq.security.JaasDualAuthenticationBroker.addConnection(JaasDualAuthenticationBroker.java:94)
	at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99)
	at org.apache.activemq.plugin.AbstractRuntimeConfigurationBroker.addConnection(AbstractRuntimeConfigurationBroker.java:118)
	at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99)
	at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99)
	at org.apache.activemq.broker.TransportConnection.processAddConnection(TransportConnection.java:848)
	at org.apache.activemq.broker.jmx.ManagedTransportConnection.processAddConnection(ManagedTransportConnection.java:77)
	at org.apache.activemq.command.ConnectionInfo.visit(ConnectionInfo.java:139)
	at org.apache.activemq.broker.TransportConnection.service(TransportConnection.java:331)
	at org.apache.activemq.broker.TransportConnection$1.onCommand(TransportConnection.java:200)
	... 19 more
Caused by: javax.security.auth.login.FailedLoginException: Password does not match
	at org.apache.activemq.jaas.PropertiesLoginModule.login(PropertiesLoginModule.java:95)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at javax.security.auth.login.LoginContext.invoke(LoginContext.java:755)
	at javax.security.auth.login.LoginContext.access$000(LoginContext.java:195)
	at javax.security.auth.login.LoginContext$4.run(LoginContext.java:682)
	at javax.security.auth.login.LoginContext$4.run(LoginContext.java:680)
	at java.security.AccessController.doPrivileged(Native Method)
	at javax.security.auth.login.LoginContext.invokePriv(LoginContext.java:680)
	at javax.security.auth.login.LoginContext.login(LoginContext.java:587)
	at org.apache.activemq.security.JaasAuthenticationBroker.authenticate(JaasAuthenticationBroker.java:92)
	... 30 more

```

This seems to be a regression because this problem was fixed as part of AMQ-7303.

### Testcase

Edit activemq.xml in ./conf folder, add authentication configuration:

```xml
    <plugins>
        <simpleAuthenticationPlugin>
            <users>
                <authenticationUser username="admin" password="12345678" groups="users,admins"/>
            </users>
        </simpleAuthenticationPlugin>
    </plugins>
```

Then, start a 2-node activemq cluster using Redit, try to connect the nodes using `StompConnection` with wrong password.

After that, `java.lang.Exception: Not connected` is shown in the console.

In activemq version 5.15.3, there is unnecessary stack trace:

```
java.lang.Exception: Not connected: java.lang.SecurityException: User name [admin] or password is invalid.
	at org.apache.activemq.security.SimpleAuthenticationBroker.authenticate(SimpleAuthenticationBroker.java:103)
	at org.apache.activemq.security.SimpleAuthenticationBroker.addConnection(SimpleAuthenticationBroker.java:71)
	at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99)
	at org.apache.activemq.broker.TransportConnection.processAddConnection(TransportConnection.java:849)
	at org.apache.activemq.broker.jmx.ManagedTransportConnection.processAddConnection(ManagedTransportConnection.java:77)
	at org.apache.activemq.command.ConnectionInfo.visit(ConnectionInfo.java:139)
	at org.apache.activemq.broker.TransportConnection.service(TransportConnection.java:336)
	at org.apache.activemq.broker.TransportConnection$1.onCommand(TransportConnection.java:200)
	at org.apache.activemq.transport.MutexTransport.onCommand(MutexTransport.java:45)
	at org.apache.activemq.transport.AbstractInactivityMonitor.onCommand(AbstractInactivityMonitor.java:301)
	at org.apache.activemq.transport.stomp.StompTransportFilter.sendToActiveMQ(StompTransportFilter.java:97)
	at org.apache.activemq.transport.stomp.ProtocolConverter.sendToActiveMQ(ProtocolConverter.java:203)
	at org.apache.activemq.transport.stomp.ProtocolConverter.onStompConnect(ProtocolConverter.java:776)
	at org.apache.activemq.transport.stomp.ProtocolConverter.onStompCommand(ProtocolConverter.java:266)
	at org.apache.activemq.transport.stomp.StompTransportFilter.onCommand(StompTransportFilter.java:85)
	at org.apache.activemq.transport.TransportSupport.doConsume(TransportSupport.java:83)
	at org.apache.activemq.transport.tcp.TcpTransport.doRun(TcpTransport.java:233)
	at org.apache.activemq.transport.tcp.TcpTransport.run(TcpTransport.java:215)
	at java.lang.Thread.run(Thread.java:748)
	at org.apache.activemq.transport.stomp.StompConnection.connect(StompConnection.java:141)
	at org.apache.activemq.transport.stomp.StompConnection.connect(StompConnection.java:132)
	at org.apache.activemq.transport.stomp.StompConnection.connect(StompConnection.java:122)
	at io.redit.samples.benchmark.activemq.SampleTest.testStompConsumer(AMQ8252.java:77)
	at io.redit.samples.benchmark.activemq.SampleTest.sampleTest(AMQ8252.java:49)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
	at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:27)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:69)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)
	at com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)
	at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:235)
	at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:54)
```

However, in version 5.16.5, there is no more unnecessary stack trace:

```

java.lang.Exception: Not connected: User name [admin] or password is invalid.
	at org.apache.activemq.transport.stomp.StompConnection.connect(StompConnection.java:141)
	at org.apache.activemq.transport.stomp.StompConnection.connect(StompConnection.java:132)
	at org.apache.activemq.transport.stomp.StompConnection.connect(StompConnection.java:122)
	at io.redit.samples.benchmark.activemq.SampleTest.testStompConsumer(AMQ8252.java:77)
	at io.redit.samples.benchmark.activemq.SampleTest.sampleTest(AMQ8252.java:49)


```