# Redit-Kafka-13964

### Details

Title: kafka-configs.sh end with UnsupportedVersionException when describing TLS user with quotas

|         Label         | Value |      Label       |    Value     |
|:---------------------:|:-----:|:----------------:|:------------:|
|       **Type**        |  Bug  |   **Priority**   |    Minor     |
|      **Status**       | OPEN  |  **Resolution**  |  Unresolved  |
| **Affects Version/s** | 3.2.0 | **Component/s**  | admin, kraft |

### Description

Usage of kafka-configs.sh end with org.apache.kafka.common.errors.UnsupportedVersionException: The broker does not support DESCRIBE_USER_SCRAM_CREDENTIALS when describing TLS user with quotas enabled.

```
bin/kafka-configs.sh --bootstrap-server localhost:9092 --describe --user CN=encrypted-arnost` got status code 1 and stderr: ------ Error while executing config command with args '--bootstrap-server localhost:9092 --describe --user CN=encrypted-arnost' java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.UnsupportedVersionException: The broker does not support DESCRIBE_USER_SCRAM_CREDENTIALS
```

STDOUT contains all necessary data, but the script itself ends with return code 1 and the error above. Scram-sha has not been configured anywhere in that case (not supported by KRaft). This might be fixed by adding support for scram-sha in the next version (not reproducible without KRaft enabled).

### Testcase

Start kafka in a three-node cluster using KRaft, then run the command describing TLS user with quotas enabled in one of the docker containers, and the same exception is thrown.