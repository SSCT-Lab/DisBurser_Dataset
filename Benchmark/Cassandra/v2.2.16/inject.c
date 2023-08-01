#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[4][200] = {	"            if (bytes.remaining() == 0)\n",
    			  	"            {\n",
				"                return;\n",
				"            }\n"};
    char str2[200] = "            throw new MarshalException(\"Not enough bytes to read a map\");\n";
    char str3[4][200] = {	"            if (bytes.remaining() == 0)\n",
    				"            {\n",
                         	"                return;\n",
                         	"            }\n"};

#ifdef CAS_14365
    system("cp ./buggy/MapSerializer.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/serializers/");
    system("cp ./buggy/SetSerializer.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/serializers/");
    printf("inject CAS_14365 ...\n");
#else
    // File 1: MapSerializer.java
    fp1 = fopen("./buggy/MapSerializer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/MapSerializer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 74)
        {
            for (int j = 0; j < 4; j++)
            {
                fputs(str1[j], fp2);
            }
            fputs(str, fp2);
        }
        else if (i == 86)
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
    system("cp ./fixed/MapSerializer.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/serializers/");

    // File 2: SetSerializer.java
    fp1 = fopen("./buggy/SetSerializer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/SetSerializer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 64)
        {
            for (int j = 0; j < 4; j++)
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
    system("cp ./fixed/SetSerializer.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/serializers/");
    printf("don't inject CAS_14365 ...\n");
#endif


#ifdef CAS_13528
    system("cp ./buggy/NodeProbe.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/tools/");
    system("cp ./buggy/DescribeCluster.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/tools/nodetool/");
    printf("inject CAS_13528 ...\n");
#else
    system("cp ./fixed/NodeProbe.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/tools/");
    system("cp ./fixed/DescribeCluster.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/tools/nodetool/");
    printf("don't inject CAS_13528 ...\n");
#endif

#ifdef CAS_15814
    system("cp ./buggy/Lists.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./buggy/Maps.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./buggy/Sets.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/cql3/");
    printf("inject CAS_15814 ...\n");
#else
    system("cp ./fixed/Lists.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./fixed/Maps.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./fixed/Sets.java ./apache-cassandra-2.2.16-src/src/java/org/apache/cassandra/cql3/");
    printf("don't inject CAS_15814 ...\n");
#endif
}
