# Redit-Rocket-257

### Details

Title: ***name server address and web server address should be specified at least one***

JIRA link：[https://issues.apache.org/jira/browse/ROCKETMQ-257](https://issues.apache.org/jira/browse/ROCKETMQ-257)

|         Label         |    Value     | Label           |       Value        |
|:---------------------:|:------------:|:---------------:|:------------------:|
|       **Type**        |     Bug      | **Priority**    |       Minor        |
|      **Status**       |    CLOSED    | **Resolution**  |       Fixed        |
| **Affects Version/s** | 4.1.0-incubating | **Fix Version/s** |   4.2.0      |

### Description

if name server address and web server address both are not specified , client will not fetch the
right name server and client will start fail, because the default wsAddr=http://jmenv.tbsite.net:8080/rocketmq/nsaddr is not reachable.

```
    if (null == this.clientConfig.getNamesrvAddr() && MixAll.getWSAddr().equals(MixAll.WS_ADDR)) {
        throw new MQClientException("name server address and web server address should be specified at least one.", null);
    } else if (null == this.clientConfig.getNamesrvAddr()) {
        this.mQClientAPIImpl.fetchNameServerAddr();
    }
```

### Testcase

Reproduced version：4.1.0-incubating

Steps to reproduce：
1. Create a DefaultMQProducer object, comment out producer.setNamesrvAddr().
2. Find that the producer can be started normally without error.
3. However, the exception No name server address is thrown when sending the message.
4. We want to verify when the producer starts: name server address and web server address should be specified at least one.

