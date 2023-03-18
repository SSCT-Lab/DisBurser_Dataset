# Redit-Zookeeper-1576

### Details

Title: ***Zookeeper cluster - failed to connect to cluster if one of the provided IPs causes java.net.UnknownHostException***

JIRA link：[https://issues.apache.org/jira/browse/ZOOKEEPER-1576](https://issues.apache.org/jira/browse/ZOOKEEPER-1576)

|         Label         |  Value   |       Label       | Value |
|:---------------------:|:--------:|:-----------------:|:-----:|
|       **Type**        |   Bug    |   **Priority**    | Major |
|      **Status**       | RESOLVED |  **Resolution**   | Fixed |
| **Affects Version/s** |  3.5.0   | **Fix Version/s** | 3.5.0 |

### Description

Using a cluster of three 3.4.3 zookeeper servers.
All the servers are up, but on the client machine, the firewall is blocking one of the servers.
The following exception is happening, and the client is not connected to any of the other cluster members.

```
The exception:Nov 02, 2012 9:54:32 PM com.netflix.curator.framework.imps.CuratorFrameworkImpl logError
SEVERE: Background exception was not retry-able or retry gave up
java.net.UnknownHostException: scnrmq003.myworkday.com
at java.net.Inet4AddressImpl.lookupAllHostAddr(Native Method)
at java.net.InetAddress$1.lookupAllHostAddr(Unknown Source)
at java.net.InetAddress.getAddressesFromNameService(Unknown Source)
at java.net.InetAddress.getAllByName0(Unknown Source)
at java.net.InetAddress.getAllByName(Unknown Source)
at java.net.InetAddress.getAllByName(Unknown Source)
at org.apache.zookeeper.client.StaticHostProvider.<init>(StaticHostProvider.java:60)
at org.apache.zookeeper.ZooKeeper.<init>(ZooKeeper.java:440)
at org.apache.zookeeper.ZooKeeper.<init>(ZooKeeper.java:375)

The code at the org.apache.zookeeper.client.StaticHostProvider.<init>(StaticHostProvider.java:60) is :
public StaticHostProvider(Collection<InetSocketAddress> serverAddresses) throws UnknownHostException {
for (InetSocketAddress address : serverAddresses) {
InetAddress resolvedAddresses[] = InetAddress.getAllByName(address
.getHostName());
for (InetAddress resolvedAddress : resolvedAddresses)

{ this.serverAddresses.add(new InetSocketAddress(resolvedAddress .getHostAddress(), address.getPort())); }
}
......
```

The for-loop is not trying to resolve the rest of the servers on the list if there is an UnknownHostException at the InetAddress.getAllByName(address.getHostName());
and it fails the client connection creation.

I was expecting the connection will be created for the other members of the cluster.
Also, InetAddress is a blocking command, and if it takes very long time, (longer than the defined timeout) - that also should allow us to continue to try and connect to the other servers on the list.
Assuming this will be fixed, and we will get connection to the current available servers, I think the zookeeper should continue to retry to connect to the not-connected server of the cluster, so it will be able to use it later when it is back.
If one of the servers on the list is not available during the connection creation, then it should be retried every x time despite the fact that we

### Testcase

Reproduced version：3.4.3

Steps to reproduce：
1. Start a three-node zookeeper cluster and elect a leader.
2. Use firewall to disconnect the local ip connection to one of the nodes
3. Create client zk to connect to the zookeeper cluster.
4. Use zk to create a EPHEMERAL node "/test" and check its data.
5. Restore the local ip connection with one of the nodes.

This bug seems to have been resolved. I have not successfully reproduced it on version 3.4.3. The client will automatically find another node in the cluster after connecting to the node with the disconnected ip connection:

```
15:42:28.562 [main-SendThread(10.2.0.3:2181)] DEBUG o.a.zookeeper.SaslServerPrincipal - Canonicalized address to 10.2.0.3
15:42:28.563 [main-SendThread(10.2.0.3:2181)] INFO  org.apache.zookeeper.ClientCnxn - Opening socket connection to server 10.2.0.3/10.2.0.3:2181. Will not attempt to authenticate using SASL (unknown error)
15:42:29.898 [main-SendThread(10.2.0.3:2181)] WARN  org.apache.zookeeper.ClientCnxn - Client session timed out, have not heard from server in 1350ms for sessionid 0x0
15:42:29.898 [main-SendThread(10.2.0.3:2181)] INFO  org.apache.zookeeper.ClientCnxn - Client session timed out, have not heard from server in 1350ms for sessionid 0x0, closing socket connection and attempting reconnect
15:42:29.902 [main-SendThread(10.2.0.3:2181)] DEBUG o.a.zookeeper.ClientCnxnSocketNIO - Ignoring exception during shutdown input
java.net.SocketException: Socket is not connected
	at sun.nio.ch.Net.translateToSocketException(Net.java:126)
	at sun.nio.ch.Net.translateException(Net.java:160)
	at sun.nio.ch.Net.translateException(Net.java:166)
	at sun.nio.ch.SocketAdaptor.shutdownInput(SocketAdaptor.java:417)
	at org.apache.zookeeper.ClientCnxnSocketNIO.cleanup(ClientCnxnSocketNIO.java:198)
	at org.apache.zookeeper.ClientCnxn$SendThread.cleanup(ClientCnxn.java:1338)
	at org.apache.zookeeper.ClientCnxn$SendThread.cleanAndNotifyState(ClientCnxn.java:1276)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1254)
Caused by: java.nio.channels.NotYetConnectedException: null
	at sun.nio.ch.SocketChannelImpl.shutdownInput(SocketChannelImpl.java:781)
	at sun.nio.ch.SocketAdaptor.shutdownInput(SocketAdaptor.java:415)
	... 4 common frames omitted
15:42:29.902 [main-SendThread(10.2.0.3:2181)] DEBUG o.a.zookeeper.ClientCnxnSocketNIO - Ignoring exception during shutdown output
java.net.SocketException: Socket is not connected
	at sun.nio.ch.Net.translateToSocketException(Net.java:126)
	at sun.nio.ch.Net.translateException(Net.java:160)
	at sun.nio.ch.Net.translateException(Net.java:166)
	at sun.nio.ch.SocketAdaptor.shutdownOutput(SocketAdaptor.java:425)
	at org.apache.zookeeper.ClientCnxnSocketNIO.cleanup(ClientCnxnSocketNIO.java:205)
	at org.apache.zookeeper.ClientCnxn$SendThread.cleanup(ClientCnxn.java:1338)
	at org.apache.zookeeper.ClientCnxn$SendThread.cleanAndNotifyState(ClientCnxn.java:1276)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1254)
Caused by: java.nio.channels.NotYetConnectedException: null
	at sun.nio.ch.SocketChannelImpl.shutdownOutput(SocketChannelImpl.java:798)
	at sun.nio.ch.SocketAdaptor.shutdownOutput(SocketAdaptor.java:423)
	... 4 common frames omitted
15:42:30.019 [main-SendThread(10.2.0.4:2181)] DEBUG o.a.zookeeper.SaslServerPrincipal - Canonicalized address to 10.2.0.4
15:42:30.019 [main-SendThread(10.2.0.4:2181)] INFO  org.apache.zookeeper.ClientCnxn - Opening socket connection to server 10.2.0.4/10.2.0.4:2181. Will not attempt to authenticate using SASL (unknown error)
15:42:30.020 [main-SendThread(10.2.0.4:2181)] INFO  org.apache.zookeeper.ClientCnxn - Socket connection established, initiating session, client: /10.2.0.1:58860, server: 10.2.0.4/10.2.0.4:2181
15:42:30.024 [main-SendThread(10.2.0.4:2181)] DEBUG org.apache.zookeeper.ClientCnxn - Session establishment request sent on 10.2.0.4/10.2.0.4:2181
```