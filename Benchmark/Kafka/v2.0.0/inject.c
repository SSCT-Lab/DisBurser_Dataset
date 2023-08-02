#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef KA_7763
    system("cp ./buggy/KafkaProducer.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/producer/");
    system("cp ./buggy/TransactionManager.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/producer/internals/");
    system("cp ./buggy/TransactionalRequestResult.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/producer/internals/");
    printf("inject KA_7763 ...\n");
#else
    system("cp ./fixed/KafkaProducer.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/producer/");
    system("cp ./fixed/TransactionManager.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/producer/internals/");
    system("cp ./fixed/TransactionalRequestResult.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/producer/internals/");
    printf("don't inject KA_7763 ...\n");
#endif


#ifdef KA_7941
    system("cp ./buggy/MockConsumer.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/consumer/");
    system("cp ./buggy/KafkaBasedLog.java ./kafka-2.0.0-src/connect/runtime/src/main/java/org/apache/kafka/connect/util/");
    printf("inject KA_7941 ...\n");
#else
    system("cp ./fixed/MockConsumer.java ./kafka-2.0.0-src/clients/src/main/java/org/apache/kafka/clients/consumer/");
    system("cp ./fixed/KafkaBasedLog.java ./kafka-2.0.0-src/connect/runtime/src/main/java/org/apache/kafka/connect/util/");
    printf("don't inject KA_7941 ...\n");
#endif


#ifdef KA_9254
    system("cp ./buggy/DynamicBrokerConfig.scala ./kafka-2.0.0-src/core/src/main/scala/kafka/server/");
    printf("inject KA_9254 ...\n");
#else
    system("cp ./fixed/DynamicBrokerConfig.scala ./kafka-2.0.0-src/core/src/main/scala/kafka/server/");
    printf("don't inject KA_9254 ...\n");
#endif
}
