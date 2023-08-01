#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200]    = 	"import java.net.InetAddress\n";
    char str2[200]    = 	"          setHost(if (ep.host == null || ep.host.trim.isEmpty) InetAddress.getLocalHost.getCanonicalHostName else ep.host).\n";

#ifdef KA_12702
    system("cp ./buggy/BrokerServer.scala ./kafka-2.8.0-src/core/src/main/scala/kafka/server/");
    printf("inject KA_12702 ...\n");
#else
    fp1 = fopen("./buggy/BrokerServer.scala", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/BrokerServer.scala", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 23)
        {
            fputs(str1, fp2);
            fputs(str, fp2);
        }
        else if (i == 274)
        {
            fputs(str2, fp2);
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
    system("cp ./fixed/BrokerServer.scala ./kafka-2.8.0-src/core/src/main/scala/kafka/server/");
    printf("don't inject KA_12702 ...\n");
#endif
}
