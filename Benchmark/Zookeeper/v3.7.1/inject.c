#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200]    = 	"                        changeZkState(States.CLOSED);\n";
    char str2[6][200] =  {	"            if (closing) {\n",
    				"                eventThread.queueEvent(new WatchedEvent(Event.EventType.None, KeeperState.Closed, null));\n",
    				"            } else if (state == States.CLOSED) {\n",
    				"                eventThread.queueEvent(new WatchedEvent(Event.EventType.None, KeeperState.Expired, null));\n",
    				"            }\n",
    				"            eventThread.queueEventOfDeath();\n"};

#ifdef ZK_4508
    system("cp ./buggy/ClientCnxn.java ./apache-zookeeper-3.7.1/zookeeper-server/src/main/java/org/apache/zookeeper/");
    printf("inject ZK_4508 ...\n");
#else
    fp1 = fopen("./buggy/ClientCnxn.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/ClientCnxn.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 1203)
        {
        }
        else if (i == 1250)
        {
            fputs(str1, fp2);
            fputs(str, fp2);
        }
        else if (i == 1316)
        {
            for (int j = 0; j < 6; j++)
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
    system("cp ./fixed/ClientCnxn.java ./apache-zookeeper-3.7.1/zookeeper-server/src/main/java/org/apache/zookeeper/");
    printf("don't inject ZK_4508 ...\n");
#endif
}
