#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200] = 	"                    this.defaultMQPullConsumer.setOffsetStore(this.offsetStore);\n";
    char str2[7][200] = {	"                    // name server address and web server address should be specified at least one\n",
    				"                    if (null == this.clientConfig.getNamesrvAddr() && MixAll.getWSAddr().equals(MixAll.WS_ADDR)) {\n",
    				"                        throw new MQClientException(\"name server address and web server address should be specified at least one.\", null);\n",
    				"                    } else if (null == this.clientConfig.getNamesrvAddr()) {\n",
    				"                        this.mQClientAPIImpl.fetchNameServerAddr();\n",
    				"                    }\n",
    				"\n"};
    char str3[200]    = 	"    public static final String WS_ADDR = \"http://\" + WS_DOMAIN_NAME + \":8080/rocketmq/\" + WS_DOMAIN_SUBGROUP;\n";
    char str4[200] = 		"            || this.defaultMQPushConsumer.getConsumeThreadMin() > 1000) {\n";
    char str5[8][200] = {	"        // consumeThreadMin can't be larger than consumeThreadMax\n",
    				"        if (this.defaultMQPushConsumer.getConsumeThreadMin() > this.defaultMQPushConsumer.getConsumeThreadMax()) {\n",
    				"            throw new MQClientException(\n",
    				"                \"consumeThreadMin (\" + this.defaultMQPushConsumer.getConsumeThreadMin() + \") \"\n",
    				"                    + \"is larger than consumeThreadMax (\" + this.defaultMQPushConsumer.getConsumeThreadMax() + \")\",\n",
    				"                null);\n",
    				"        }\n",
    				"\n"};


#ifdef RMQ_255
    system("cp ./buggy/DefaultMQPullConsumerImpl.java ./rocketmq-all-4.1.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");
    printf("inject RMQ_255 ...\n");
#else
    fp1 = fopen("./buggy/DefaultMQPullConsumerImpl.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/DefaultMQPullConsumerImpl.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 571)
        {
            fputs(str1, fp2);
            fputs(str, fp2);
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
    system("cp ./fixed/DefaultMQPullConsumerImpl.java ./rocketmq-all-4.1.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");
    printf("don't inject RMQ_255 ...\n");
#endif


#ifdef RMQ_257
    system("cp ./buggy/MQClientInstance.java ./rocketmq-all-4.1.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/factory/");
    system("cp ./buggy/MixAll.java ./rocketmq-all-4.1.0-incubating-src/common/src/main/java/org/apache/rocketmq/common/");
    printf("inject RMQ_257 ...\n");
#else
    // File 1: MQClientInstance.java
    fp1 = fopen("./buggy/MQClientInstance.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/MQClientInstance.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 227)
        {
            for (int j = 0; j < 7; j++)
            {
                fputs(str2[j], fp2);
            }
        }
        else if (i == 228 || i == 229 || i == 230)
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
    system("cp ./fixed/MQClientInstance.java ./rocketmq-all-4.1.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/factory/");

    // File 2: MixAll.java
    fp1 = fopen("./buggy/MixAll.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/MixAll.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 57)
        {
        }
        else if (i == 58)
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
    system("cp ./fixed/MixAll.java ./rocketmq-all-4.1.0-incubating-src/common/src/main/java/org/apache/rocketmq/common/");
    printf("don't inject RMQ_257 ...\n");
#endif


#ifdef RMQ_266
    system("cp ./buggy/DefaultMQPushConsumerImpl.java ./rocketmq-all-4.1.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");
    printf("inject RMQ_266 ...\n");
#else
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
        if (i == 700)
        {
            fputs(str1, fp2);
        }
        else if (i == 701)
        {
        }
        else if (i == 716)
        {
            for (int j = 0; j < 8; j++)
            {
                fputs(str2[j], fp2);
            }
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
    system("cp ./fixed/DefaultMQPushConsumerImpl.java ./rocketmq-all-4.1.0-incubating-src/client/src/main/java/org/apache/rocketmq/client/impl/consumer/");
    printf("don't inject RMQ_266 ...\n");
#endif
}
