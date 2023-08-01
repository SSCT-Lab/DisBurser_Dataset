#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef AMQ_6010
    system("cp ./buggy/AmqpProtocolDiscriminator.java ./activemq-parent-5.12.0-src/activemq-amqp/src/main/java/org/apache/activemq/transport/amqp/");
    printf("inject AMQ_6010 ...\n");
#else
    system("cp ./fixed/AmqpProtocolDiscriminator.java ./activemq-parent-5.12.0-src/activemq-amqp/src/main/java/org/apache/activemq/transport/amqp/");
    printf("don't inject AMQ_6010 ...\n");
#endif


#ifdef AMQ_6059
    system("cp ./buggy/BrokerSupport.java ./activemq-parent-5.12.0-src/activemq-broker/src/main/java/org/apache/activemq/util/");
    printf("inject AMQ_6059 ...\n");
#else
    system("cp ./fixed/BrokerSupport.java ./activemq-parent-5.12.0-src/activemq-broker/src/main/java/org/apache/activemq/util/");
    printf("don't inject AMQ_6059 ...\n");
#endif


#ifdef AMQ_6062
    system("cp ./buggy/QueueBrowserSubscription.java ./activemq-parent-5.12.0-src/activemq-broker/src/main/java/org/apache/activemq/broker/region/");
    printf("inject AMQ_6062 ...\n");
#else
    system("cp ./fixed/QueueBrowserSubscription.java ./activemq-parent-5.12.0-src/activemq-broker/src/main/java/org/apache/activemq/broker/region/");
    printf("don't inject AMQ_6062 ...\n");
#endif
}
