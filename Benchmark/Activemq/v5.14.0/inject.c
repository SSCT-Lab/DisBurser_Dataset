#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[3][200]    = {	"                    if (info.isNoLocal()) {\n",
    				"                        sub.setSelector(sub.getSelector());\n",
    				"                    }\n"};
    char str2[3][200]    = {	"                if (sub.getContext() == context) {\n",
    				"                    sub.deactivate(keepDurableSubsActive, info.getLastDeliveredSequenceId());\n",
    				"                }\n"};
    char str3[5][200]    = {	"        if (broker.getBrokerService().getStoreOpenWireVersion() >= 11) {\n",
    				"            if (info1.isNoLocal() ^ info2.isNoLocal()) {\n",
    				"                return true;\n",
    				"            }\n",
    				"        }\n"};

#ifdef AMQ_6430
    system("cp ./buggy/TopicRegion.java ./activemq-parent-5.14.0-src/activemq-broker/src/main/java/org/apache/activemq/broker/region/");
    printf("inject AMQ_6430 ...\n");
#else
    fp1 = fopen("./buggy/TopicRegion.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/TopicRegion.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 164)
        {
            for (int j = 0; j < 3; j++)
            {
                fputs(str1[j], fp2);
            }
            fputs(str, fp2);
        }
        else if (i == 191)
        {
            for (int j = 0; j < 3; j++)
            {
                fputs(str2[j], fp2);
            }
        }
        else if (i == 192)
        {
        }
        else if (i == 375)
        {
            for (int j = 0; j < 5; j++)
            {
                fputs(str3[j], fp2);
            }
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
    system("cp ./fixed/TopicRegion.java ./activemq-parent-5.14.0-src/activemq-broker/src/main/java/org/apache/activemq/broker/region/");
    printf("don't inject AMQ_6430 ...\n");
#endif


#ifdef AMQ_6697
    system("cp ./buggy/StompSubscription.java ./activemq-parent-5.14.0-src/activemq-stomp/src/main/java/org/apache/activemq/transport/stomp/");
    printf("inject AMQ_6697 ...\n");
#else
    system("cp ./fixed/StompSubscription.java ./activemq-parent-5.14.0-src/activemq-stomp/src/main/java/org/apache/activemq/transport/stomp/");
    printf("don't inject AMQ_6697 ...\n");
#endif


#ifdef AMQ_6823
    system("cp ./buggy/MessagePull.java ./activemq-parent-5.14.0-src/activemq-client/src/main/java/org/apache/activemq/command/");
    printf("inject AMQ_6823 ...\n");
#else
    system("cp ./fixed/MessagePull.java ./activemq-parent-5.14.0-src/activemq-client/src/main/java/org/apache/activemq/command/");
    printf("don't inject AMQ_6823 ...\n");
#endif
}
