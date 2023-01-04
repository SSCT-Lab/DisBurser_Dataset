# Redit-ActiveMQ-6430

### Details
Title: noLocal=true in durable subscriptions is ignored after reconnect

|         Label         |       Value       |      Label      |        Value        |
|:---------------------:|:-----------------:|:---------------:|:-------------------:|
|       **Type**        |        Bug        |  **Priority**   |        Major        |
|      **Status**       |     RESOLVED      | **Resolution**  |        Fixed        |
| **Affects Version/s** |   5.13.4, 5.14.0  | **Component/s** |       Broker        |

### Description

I create a connection to my local ActiveMQ and open two sessions. In the first session I create a durable topic subscriber with noLocal=true. In the second session I send a message to the same topic. Then I close both sessions and the connection. The first time I do this, everything works well, that means I send but do not receive the message. The second time I run the same application I send AND receive the message.

After removing all files and directories in ActiveMQ's data directory, not receiving my own message works again, but only once.

### Testcase

Started the activemq cluster, started two sessions, and created a persistent topic subscriber in the first session with noLocal=true. In the second session, I sent a message to the same topic. The first time I did this, everything worked fine, meaning I sent but didn't receive the message. When the above operations are repeated, an abnormal situation occurs, and messages are sent and received.