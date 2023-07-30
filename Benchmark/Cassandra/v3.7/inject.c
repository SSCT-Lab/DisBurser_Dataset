#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200] = 		"            return isLive(before) ? UpdateAction.UPDATE_EXISTING : UpdateAction.NONE;\n";
    char str2[200] = 		"        assert isLive(cell) : \"We shouldn't have got there if the base row had no associated entry\";\n";

#ifdef CAS_12424
    system("cp ./buggy/ViewUpdateGenerator.java ./apache-cassandra-3.7-src/src/java/org/apache/cassandra/db/view/");
    printf("inject CAS_12424 ...\n");
#else
    fp1 = fopen("./buggy/ViewUpdateGenerator.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/ViewUpdateGenerator.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 193)
        {
            fputs(str1, fp2);
        }
        else if (i == 454)
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
    system("cp ./fixed/ViewUpdateGenerator.java ./apache-cassandra-3.7-src/src/java/org/apache/cassandra/db/view/");
    printf("don't inject CAS_12424 ...\n");
#endif
}
