#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200]    = 	"      \"A value of '*' indicates ACL should apply to all topics.\")\n";
    char str2[200]    = 	"      \"A value of '*' indicates the ACLs should apply to all groups.\")\n";
    char str3[200]    = 	"      \"be added or removed. A value of '*' indicates the ACLs should apply to all transactionalIds.\")\n";
    char str4[200]    = 	"      \"A value of '*' indicates ACL should apply to all tokens.\")\n";
    char str5[200]    = 	"      \" For example, User:'*' is the wild card indicating all users.\")\n";
    char str6[200]    = 	"      \"allows access to User:'*' and specify --deny-principal=User:test@EXAMPLE.COM. \" +\n";
    char str7[200]    = 	"      \"If you have specified --allow-principal then the default for this option will be set to '*' which allows access from all hosts.\")\n";
    char str8[200]    = 	"      \"If you have specified --deny-principal then the default for this option will be set to '*' which denies access from all hosts.\")\n";

#ifdef KA_13852
    system("cp ./buggy/AclCommand.scala ./kafka-3.2.0-src/core/src/main/scala/kafka/admin/");
    printf("inject KA_13852 ...\n");
#else
    fp1 = fopen("./buggy/AclCommand.scala", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/AclCommand.scala", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 520)
        {
            fputs(str1, fp2);
        }
        else if (i == 527)
        {
            fputs(str2, fp2);
        }
        else if (i == 533)
        {
            fputs(str3, fp2);
        }
        else if (i == 543)
        {
            fputs(str4, fp2);
        }
        else if (i == 571)
        {
            fputs(str5, fp2);
        }
        else if (i == 581)
        {
            fputs(str6, fp2);
        }
        else if (i == 594)
        {
            fputs(str7, fp2);
        }
        else if (i == 600)
        {
            fputs(str8, fp2);
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
    system("cp ./fixed/AclCommand.scala ./kafka-3.2.0-src/core/src/main/scala/kafka/admin/");
    printf("don't inject KA_13852 ...\n");
#endif


#ifdef KA_14024
    system("cp ./buggy/AbstractCoordinator.java ./kafka-3.2.0-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/");
    system("cp ./buggy/ConsumerCoordinator.java ./kafka-3.2.0-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/");
    system("cp ./buggy/WorkerCoordinator.java ./kafka-3.2.0-src/connect/runtime/src/main/java/org/apache/kafka/connect/runtime/distributed/");
    printf("inject KA_14024 ...\n");
#else
    system("cp ./fixed/AbstractCoordinator.java ./kafka-3.2.0-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/");
    system("cp ./fixed/ConsumerCoordinator.java ./kafka-3.2.0-src/clients/src/main/java/org/apache/kafka/clients/consumer/internals/");
    system("cp ./fixed/WorkerCoordinator.java ./kafka-3.2.0-src/connect/runtime/src/main/java/org/apache/kafka/connect/runtime/distributed/");
    printf("don't inject KA_14024 ...\n");
#endif


#ifdef KA_14797
    system("cp ./buggy/MirrorSourceTask.java ./kafka-3.2.0-src/connect/mirror/src/main/java/org/apache/kafka/connect/mirror/");
    printf("inject KA_14797 ...\n");
#else
    system("cp ./fixed/MirrorSourceTask.java ./kafka-3.2.0-src/connect/mirror/src/main/java/org/apache/kafka/connect/mirror/");
    printf("don't inject KA_14797 ...\n");
#endif
}
