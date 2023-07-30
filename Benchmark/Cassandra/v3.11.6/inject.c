#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200] = "import org.apache.cassandra.exceptions.InvalidRequestException;\n";
    char str2[10][200] = {	"    public boolean containsTokenRelations()\n",
    			  	"    {\n",
				"        for (Relation rel : relations)\n",
				"        {\n",
				"            if (rel.onToken())\n",
				"                return true;\n",
				"        }\n",
				"        return false;\n",
				"    }\n",
				"\n"};
    char str3[3][200] = {	"        if (whereClause.containsTokenRelations())\n",
                         	"            throw new InvalidRequestException(\"Cannot use token relation when defining a materialized view\");\n",
                         	"\n"};

    char str4[200] = "import java.io.File;\n";
    char str5[5][200] = {	"            if (!snapshotName.isEmpty() && snapshotName.contains(File.separator))\n",
    			  	"            {\n",
				"                throw new IOException(\"Snapshot name cannot contain \" + File.separator);\n",
				"            }\n",
				"\n"};

    char str6[200] = "            return name.toCQLString() + terms.stream().map(Term.Raw::getText).collect(Collectors.joining(\", \", \"(\", \")\"));\n";
    char str7[200] = "import org.apache.cassandra.cql3.ColumnIdentifier;\n";
    char str8[3][200] = {	"    // We special case the token function because that's the only function which name is a reserved keyword\n",
    			  	"    private static final FunctionName TOKEN_FUNCTION_NAME = FunctionName.nativeFunction(\"token\");\n",
				"\n"};
    char str9[10][200] = {	"\n",
    				"    public String toCQLString()\n",
                         	"    {\n",
                         	"        String maybeQuotedName = equalsNativeFunction(TOKEN_FUNCTION_NAME)\n",
                         	"               ? name\n",
                         	"               : ColumnIdentifier.maybeQuote(name);\n",
                         	"        return keyspace == null\n",
                         	"               ? maybeQuotedName\n",
                         	"               : ColumnIdentifier.maybeQuote(keyspace) + '.' + maybeQuotedName;\n",
                         	"    }\n"};

    char str10[5][200] = {	"            if (column.type.isMultiCell())\n",
    				"                throw new InvalidRequestException(String.format(\"Cannot use selection function %s on non-frozen %s %s\",\n",
                         	"                                                                isWritetime ? \"writeTime\" : \"ttl\",\n",
                         	"                                                                column.type.isCollection() ? \"collection\" : \"UDT\",\n",
                         	"                                                                column.name));\n"};


#ifdef CAS_13464
    system("cp ./buggy/WhereClause.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./buggy/CreateViewStatement.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/statements/");
    printf("inject CAS_13464 ...\n");
#else
    // File 1: WhereClause.java
    fp1 = fopen("./buggy/WhereClause.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/WhereClause.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 26)
        {
            fputs(str1, fp2);
            fputs(str, fp2);
        }
        else if (i == 59)
        {
            fputs(str, fp2);
            for (int j = 0; j < 10; j++)
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
    system("cp ./fixed/WhereClause.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/");

    // File 2: CreateViewStatement.java
    fp1 = fopen("./buggy/CreateViewStatement.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/CreateViewStatement.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 241)
        {
            for (int j = 0; j < 3; j++)
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
    system("cp ./fixed/CreateViewStatement.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/statements/");
    printf("don't inject CAS_13464 ...\n");
#endif


#ifdef CAS_15297
    system("cp ./buggy/Snapshot.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/tools/nodetool/");
    printf("inject CAS_15297 ...\n");
#else
    fp1 = fopen("./buggy/Snapshot.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/Snapshot.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 25)
        {
            fputs(str4, fp2);
            fputs(str, fp2);
        }
        else if (i == 63)
        {
            fputs(str, fp2);
            for (int j = 0; j < 5; j++)
            {
                fputs(str5[j], fp2);
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
    system("cp ./fixed/Snapshot.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/tools/nodetool/");
    printf("don't inject CAS_15297 ...\n");
#endif


#ifdef CAS_16836
    system("cp ./buggy/FunctionCall.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/functions/");
    system("cp ./buggy/FunctionName.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/functions/");
    printf("inject CAS_16836 ...\n");
#else
    // File 1: FunctionCall.java
    fp1 = fopen("./buggy/FunctionCall.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/FunctionCall.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 206)
        {
            fputs(str6, fp2);
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
    system("cp ./fixed/FunctionCall.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/functions/");

    // File 2: FunctionName.java
    fp1 = fopen("./buggy/FunctionName.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/FunctionName.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
    	if (i == 22)
    	{
            fputs(str7, fp2);
            fputs(str, fp2);
    	}
        else if (i == 25)
        {
            for (int j = 0; j < 3; j++)
            {
                fputs(str8[j], fp2);
            }
            fputs(str, fp2);
        }
        else if (i == 81)
        {
            for (int j = 0; j < 10; j++)
            {
                fputs(str9[j], fp2);
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
    system("cp ./fixed/FunctionName.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/functions/");
    printf("don't inject CAS_16836 ...\n");
#endif


#ifdef CAS_17628
    system("cp ./buggy/Selectable.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/selection/");
    printf("inject CAS_17628 ...\n");
#else
    fp1 = fopen("./buggy/Selectable.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/Selectable.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 202)
        {
            for (int j = 0; j < 5; j++)
            {
                fputs(str10[j], fp2);
            }
        }
        else if (i == 203 || i == 204)
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
    system("cp ./fixed/Selectable.java ./apache-cassandra-3.11.6-src/src/java/org/apache/cassandra/cql3/selection/");
    printf("don't inject CAS_17628 ...\n");
#endif
}
