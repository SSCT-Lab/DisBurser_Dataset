#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[3][200]    = {	"          if (!zkClientForChrootCreation.pathExists(chroot)) {\n",
    				"            zkClientForChrootCreation.makeSurePersistentPathExists(chroot)\n",
    				"          }\n"};

#ifdef KA_12866
    system("cp ./buggy/KafkaZkClient.scala ./kafka-3.0.0-src/core/src/main/scala/kafka/zk/");
    printf("inject KA_12866 ...\n");
#else
    fp1 = fopen("./buggy/KafkaZkClient.scala", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/KafkaZkClient.scala", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 1971)
        {
            for (int j = 0; j < 3; j++)
            {
                fputs(str1[j], fp2);
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
    system("cp ./fixed/KafkaZkClient.scala ./kafka-3.0.0-src/core/src/main/scala/kafka/zk/");
    printf("don't inject KA_12866 ...\n");
#endif
}
