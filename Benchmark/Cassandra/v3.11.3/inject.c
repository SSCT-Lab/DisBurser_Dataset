#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200] = 		"        IndexMode.validateAnalyzer(options, target.left);\n";
    char str2[200] = 		"    public abstract boolean isCompatibleWith(AbstractType<?> validator);\n";
    char str3[5][200] = {	"    @Override\n",
    			  	"    public boolean isCompatibleWith(AbstractType<?> validator)\n",
				"    {\n",
				"        return VALID_ANALYZABLE_TYPES.containsKey(validator);\n",
				"    }\n"};
    char str4[5][200] = {	"    @Override\n",
    				"    public boolean isCompatibleWith(AbstractType<?> validator)\n",
                         	"    {\n",
                         	"        return true;\n",
                         	"    }\n"};
    char str5[5][200] = {	"    @Override\n",
    				"    public boolean isCompatibleWith(AbstractType<?> validator)\n",
                         	"    {\n",
                         	"        return VALID_ANALYZABLE_TYPES.contains(validator);\n",
                         	"    }\n"};
    char str6[200] = 		"import java.util.HashSet;\n";
    char str7[200] = 		"import java.util.Set;\n";  
    char str8[200] = 		"import org.apache.cassandra.db.marshal.AsciiType;\n";
    char str9[7][200] = {	"    private static final Set<AbstractType<?>> VALID_ANALYZABLE_TYPES = new HashSet<AbstractType<?>>()\n",
    				"    {\n",
                         	"        {\n",
                         	"            add(UTF8Type.instance);\n",
                         	"            add(AsciiType.instance);\n",
                         	"        }\n",
                         	"    };\n"};
    char str10[5][200] = {	"    @Override\n",
    				"    public boolean isCompatibleWith(AbstractType<?> validator)\n",
                         	"    {\n",
                         	"        return VALID_ANALYZABLE_TYPES.contains(validator);\n",
                         	"    }\n"};
    char str11[200] = 		"    public static void validateAnalyzer(Map<String, String> indexOptions, ColumnDefinition cd) throws ConfigurationException\n";
    char str12[200] = 		"            Class<?> analyzerClass;\n";
    char str13[200] = 		"                analyzerClass = Class.forName(indexOptions.get(INDEX_ANALYZER_CLASS_OPTION));\n";
    char str14[14][200] = {	"            AbstractAnalyzer analyzer;\n",
    				"            try\n",
                         	"            {\n",
                         	"                analyzer = (AbstractAnalyzer) analyzerClass.newInstance();\n",
                         	"                if (!analyzer.isCompatibleWith(cd.type))\n",
                         	"                    throw new ConfigurationException(String.format(\"%s does not support type %s\",\n",
                         	"                                                                   analyzerClass.getSimpleName(),\n",
                         	"                                                                   cd.type.asCQL3Type()));\n",
                         	"            }\n",
                         	"            catch (InstantiationException | IllegalAccessException e)\n",
                         	"            {\n",
                         	"                throw new ConfigurationException(String.format(\"Unable to initialize analyzer class option specified [%s]\",\n",
                         	"                                                               analyzerClass.getSimpleName()));\n",
                         	"            }\n"};
    
                         	
                         	          
#ifdef CAS_13669
    system("cp ./buggy/SASIIndex.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/");
    system("cp ./buggy/AbstractAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");
    system("cp ./buggy/DelimiterAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");
    system("cp ./buggy/NoOpAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");
    system("cp ./buggy/NonTokenizingAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");
    system("cp ./buggy/StandardAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");
    system("cp ./buggy/IndexMode.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/conf/");
    printf("inject CAS_13669 ...\n");
#else
    // File 1: SASIIndex.java
    fp1 = fopen("./buggy/SASIIndex.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/SASIIndex.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 145)
        {
            fputs(str1, fp2);
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
    system("cp ./fixed/SASIIndex.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/");

    // File 2: AbstractAnalyzer.java
    fp1 = fopen("./buggy/AbstractAnalyzer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/AbstractAnalyzer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
    	if (i == 50)
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
    system("cp ./fixed/AbstractAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");

    // File 3: DelimiterAnalyzer.java
    fp1 = fopen("./buggy/DelimiterAnalyzer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/DelimiterAnalyzer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
    	if (i == 108)
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
    system("cp ./fixed/DelimiterAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");

    // File 4: NoOpAnalyzer.java
    fp1 = fopen("./buggy/NoOpAnalyzer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/NoOpAnalyzer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
    	if (i == 54)
        {
            for (int j = 0; j < 5; j++)
            {
                fputs(str4[j], fp2);
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
    system("cp ./fixed/NoOpAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");

    // File 5: NonTokenizingAnalyzer.java
    fp1 = fopen("./buggy/NonTokenizingAnalyzer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/NonTokenizingAnalyzer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
    	if (i == 126)
        {
            for (int j = 0; j < 5; j++)
            {
                fputs(str5[j], fp2);
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
    system("cp ./fixed/NonTokenizingAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");

    // File 6: StandardAnalyzer.java
    fp1 = fopen("./buggy/StandardAnalyzer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/StandardAnalyzer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
    	if (i == 25)
    	{
            fputs(str6, fp2);
            fputs(str, fp2);
    	}
    	else if (i == 26)
    	{
            fputs(str7, fp2);
            fputs(str, fp2);
    	}
    	else if (i == 29)
    	{
            fputs(str8, fp2);
            fputs(str, fp2);
    	}
	else if (i == 41)
	{
	    for (int j = 0; j < 7; j++)
	    {
		fputs(str9[j], fp2);
	    }
	    fputs(str, fp2);
	}
	else if (i == 204)
	{
	    for (int j = 0; j < 5; j++)
	    {
		fputs(str10[j], fp2);
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
    system("cp ./fixed/StandardAnalyzer.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/analyzer/");

    // File 7: IndexMode.java
    fp1 = fopen("./buggy/IndexMode.java", "r");
    if (fp1 == NULL)
    {
     perror("open file error");
     return -1;
    }
    fp2 = fopen("./fixed/IndexMode.java", "w+");
    if (fp2 == NULL)
    {
     perror("open file error");
     return -1;
    }

    while (fgets(str, 500, fp1) != NULL)
    {
    if (i == 95)
    {
         fputs(str11, fp2);
    }
     else if (i == 100)
     {
         fputs(str12, fp2);
         fputs(str, fp2);
     }
     else if (i == 102)
     {
         fputs(str13, fp2);
     }
     else if (i == 109)
     {
         for (int j = 0; j < 14; j++)
    {
    fputs(str14[j], fp2);
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
    system("cp ./fixed/IndexMode.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/index/sasi/conf/");
    printf("don't inject CAS_13669 ...\n");
#endif


#ifdef CAS_15663
    system("cp ./buggy/cqlhandling.py ./apache-cassandra-3.11.3-src/pylib/cqlshlib/");
    system("cp ./buggy/Lexer.g ./apache-cassandra-3.11.3-src/src/antlr/");
    system("cp ./buggy/ReservedKeywords.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./buggy/reserved_keywords.txt ./apache-cassandra-3.11.3-src/src/resources/org/apache/cassandra/cql3/");
    printf("inject CAS_15663 ...\n");
#else
    system("cp ./fixed/cqlhandling.py ./apache-cassandra-3.11.3-src/pylib/cqlshlib/");
    system("cp ./fixed/Lexer.g ./apache-cassandra-3.11.3-src/src/antlr/");
    system("cp ./fixed/ReservedKeywords.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./fixed/reserved_keywords.txt ./apache-cassandra-3.11.3-src/src/resources/org/apache/cassandra/cql3/");
    printf("don't inject CAS_15663 ...\n");
#endif


#ifdef CAS_16307
    system("cp ./buggy/DataLimits.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/db/filter/");
    system("cp ./buggy/DataResolver.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/service/");
    printf("inject CAS_16307 ...\n");
#else
    system("cp ./fixed/DataLimits.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/db/filter/");
    system("cp ./fixed/DataResolver.java ./apache-cassandra-3.11.3-src/src/java/org/apache/cassandra/service/");
    printf("don't inject CAS_16307 ...\n");
#endif
}
