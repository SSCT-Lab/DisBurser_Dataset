#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200]    = 	"        Date dt = UtilAll.parseDate(this.defaultMQPushConsumer.getConsumeTimestamp(), UtilAll.YYYYMMDDHHMMSS);\n";
    char str2[3][200] = {	"                \"consumeTimestamp is invalid, the valid format is yyyyMMddHHmmss,but received \"\n",
    				"                    + this.defaultMQPushConsumer.getConsumeTimestamp()\n",
    				"                    + \" \" + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), null);\n"};
    char str3[200]    = 	"                                UtilAll.YYYYMMDDHHMMSS).getTime();\n";
    char str4[200]    = 	"    public static final String YYYYMMDDHHMMSS = \"yyyyMMddHHmmss\";\n";

#ifdef RMQ_189
    system("cp ./buggy/DefaultMQPushConsumerImpl.java ./rocketmq-all-4.0.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");
    system("cp ./buggy/RebalancePushImpl.java ./rocketmq-all-4.0.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");
    system("cp ./buggy/UtilAll.java ./rocketmq-all-4.0.0-incubating-src/common/src/main/java/org/apache/rocketmq/common/");
    printf("inject RMQ_189 ...\n");
#else
    // File 1: DefaultMQPushConsumerImpl.java
    fp1 = fopen("./buggy/DefaultMQPushConsumerImpl.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/DefaultMQPushConsumerImpl.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 656)
        {
            fputs(str1, fp2);
        }
        else if (i == 659)
        {
            for (int j = 0; j < 3; j++)
            {
                fputs(str2[j], fp2);
            }
        }
        else if (i == 660 || i == 661)
        {
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
    system("cp ./fixed/DefaultMQPushConsumerImpl.java ./rocketmq-all-4.0.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");

    // File 2: RebalancePushImpl.java
    fp1 = fopen("./buggy/RebalancePushImpl.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/RebalancePushImpl.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 162)
        {
            fputs(str3, fp2);
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
    system("cp ./fixed/RebalancePushImpl.java ./rocketmq-all-4.0.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");

    // File 3: UtilAll.java
    fp1 = fopen("./buggy/UtilAll.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/UtilAll.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 43)
        {
            fputs(str4, fp2);
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
    system("cp ./fixed/UtilAll.java ./rocketmq-all-4.0.0-incubating-src/common/src/main/java/org/apache/rocketmq/common/");
    printf("don't inject RMQ_189 ...\n");
#endif


#ifdef RMQ_153
    system("cp ./buggy/MQClientInstance.java ./rocketmq-all-4.0.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/factory/");
    printf("inject RMQ_153 ...\n");
#else
    system("cp ./fixed/MQClientInstance.java ./rocketmq-all-4.0.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/factory/");
    printf("don't inject RMQ_153 ...\n");
#endif


#ifdef RMQ_270
    system("cp ./buggy/MappedFileQueue.java ./rocketmq-all-4.0.0-incubating-src/store/src/main/java/org/apache/rocketmq/store/");
    printf("inject RMQ_270 ...\n");
#else
    system("cp ./fixed/MappedFileQueue.java ./rocketmq-all-4.0.0-incubating-src/store/src/main/java/org/apache/rocketmq/store/");
    printf("don't inject RMQ_270 ...\n");
#endif
}
