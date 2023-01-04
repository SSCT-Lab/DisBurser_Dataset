# Redit-Kafka-12702

### Details

Title: kafka-configs.sh end with UnsupportedVersionException when describing TLS user with quotas

|         Label         | Value |      Label       |    Value     |
|:---------------------:|:-----:|:----------------:|:------------:|
|       **Type**        |  Bug  |   **Priority**   |    Blocker   |
|      **Status**       | RESOLVED|  **Resolution** |  Fixed  |
| **Affects Version/s** | 2.8.0 | **Component/s**  | None |

### Description

In kraft mode, if listeners and advertised.listeners are not configured with host addresses, the host parameter value of Listener in BrokerRegistrationRequestData will be null. When the broker is started, a null pointer exception will be thrown, causing startup failure.

A feasible solution is to replace the empty host of endPoint in advertisedListeners with InetAddress.getLocalHost.getCanonicalHostName in Broker Server when building networkListeners.

The following is the debug log:

```
[2021-04-21 14:15:20,032] DEBUG (broker-2-to-controller-send-thread org.apache.kafka.clients.NetworkClient 522) [broker-2-to-controller] Sending BROKER_REGISTRATION request with header RequestHeader(apiKey=BROKER_REGIS
TRATION, apiVersion=0, clientId=2, correlationId=6) and timeout 30000 to node 2: BrokerRegistrationRequestData(brokerId=2, clusterId='nCqve6D1TEef3NpQniA0Mg', incarnationId=X8w4_1DFT2yUjOm6asPjIQ, listeners=[Listener(n
ame='PLAINTEXT', host=null, port=9092, securityProtocol=0)], features=[], rack=null)
[2021-04-21 14:15:20,033] ERROR (broker-2-to-controller-send-thread kafka.server.BrokerToControllerRequestThread 76) [broker-2-to-controller-send-thread]: unhandled exception caught in InterBrokerSendThread
java.lang.NullPointerException
```

### Testcase

Start kafka in a three-node cluster using KRaft, omit the host address in the configuration file: 

```
listeners = PLAINTEXT://:9092,CONTROLLER://:19091
inter.broker.listener.name=PLAINTEXT
advertised.listeners=PLAINTEXT://:9092
```

In this way, the above exception will be thrown when the broker is started, causing the node to crash.