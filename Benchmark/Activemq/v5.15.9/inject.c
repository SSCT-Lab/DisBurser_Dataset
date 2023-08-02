#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200]    = 	"            throw new SecurityException(\"User name [\" + username + \"] or password is invalid.\");\n";
    char str2[200]    = 	"            LOG.warn(\"Failed to add Connection id={}, clientId={}, clientIP={} due to {}\", info.getConnectionId(), clientId, info.getClientIp(), e.getLocalizedMessage());\n";

#ifdef AMQ_8252
    system("cp ./buggy/JaasAuthenticationBroker.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/security/");
    system("cp ./buggy/TransportConnection.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/broker/");
    printf("inject AMQ_8252 ...\n");
#else
    // File 1: JaasAuthenticationBroker.java
    fp1 = fopen("./buggy/JaasAuthenticationBroker.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/JaasAuthenticationBroker.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 96)
        {
            fputs(str1, fp2);
        }
        else
        {
            fputs(str, fp2);
        }
        i++;
    }
    i = 0;
    fclose(fp1);
    fclose(fp2);
    system("cp ./fixed/JaasAuthenticationBroker.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/security/");

    // File 2: TransportConnection.java
    fp1 = fopen("./buggy/TransportConnection.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/TransportConnection.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 854)
        {
            fputs(str2, fp2);
        }
        else
        {
            fputs(str, fp2);
        }
        i++; // 行号控制
    }
    i = 0;
    fclose(fp1);
    fclose(fp2);
    system("cp ./fixed/TransportConnection.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/broker/");
    printf("don't inject AMQ_8252 ...\n");
#endif


#ifdef AMQ_8104
    system("cp ./buggy/AnnotatedMBean.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/broker/jmx/");
    printf("inject AMQ_8104 ...\n");
#else
    system("cp ./fixed/AnnotatedMBean.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/broker/jmx/");
    printf("don't inject AMQ_8104 ...\n");
#endif


#ifdef AMQ_7312
    system("cp ./buggy/SubQueueSelectorCacheBroker.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/plugin/");
    printf("inject AMQ_7312 ...\n");
#else
    system("cp ./fixed/SubQueueSelectorCacheBroker.java ./activemq-parent-5.15.9-src/activemq-broker/src/main/java/org/apache/activemq/plugin/");
    printf("don't inject AMQ_7312 ...\n");
#endif
}
