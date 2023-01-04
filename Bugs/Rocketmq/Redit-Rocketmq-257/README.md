# Redit-Rocket-257

### Details
Title: name server address and web server address should be specified at least one

|         Label         |    Value     | Label           |       Value        |
|:---------------------:|:------------:|:---------------:|:------------------:|
|       **Type**        |     Bug      | **Priority**    |       Minor        |
|      **Status**       |    CLOSED    | **Resolution**  |       Fixed        |
| **Affects Version/s** | 4.1.0-incubating | **Component/s** |      rocketmq-client       |

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

Start a rocket cluster, add rocket-client dependencies, create a DefaultMQProducer object, comment out producer.setNamesrvAddr(), and find that the producer can be started normally without error. However, the exception No name server address is thrown when sending the message. We want to verify when the producer starts: name server address and web server address should be specified at least one.