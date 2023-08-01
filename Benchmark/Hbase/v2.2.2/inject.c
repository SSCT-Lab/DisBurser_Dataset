#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200]        =    "import edu.umd.cs.findbugs.annotations.Nullable;\n";
    char str2[4][200]    = {	"  public void addChore(@Nullable ProcedureInMemoryChore<TEnvironment> chore) {\n",
    				"    if (chore == null) {\n",
    				"      return;\n",
    				"    }\n"};
    				
    char str3[4][200]    = {	"  public boolean removeChore(@Nullable ProcedureInMemoryChore<TEnvironment> chore) {\n",
    				"    if (chore == null) {\n",
    				"      return true;\n",
    				"    }\n"};

    char str4[2][200]    = {	"    // resets to default mob compaction thread number when the user sets this value incorrectly   \n",
    				"    if (maxThreads <= 0) {\n"};


#ifdef HB_23682
    system("cp ./buggy/ProcedureExecutor.java ./hbase-2.2.2-src/hbase-procedure/src/main/java/org/apache/hadoop/hbase/procedure2/");
    printf("inject HB_23682 ...\n");
#else
    fp1 = fopen("./buggy/ProcedureExecutor.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/ProcedureExecutor.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 19)
        {
            fputs(str1, fp2);
            fputs(str, fp2);
        }
        else if (i == 720)
        {
            for (int j = 0; j < 4; j++)
            {
                fputs(str2[j], fp2);
            }
        }
        else if (i == 730)
        {
            for (int j = 0; j < 4; j++)
            {
                fputs(str3[j], fp2);
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
    system("cp ./fixed/ProcedureExecutor.java ./hbase-2.2.2-src/hbase-procedure/src/main/java/org/apache/hadoop/hbase/procedure2/");
    printf("don't inject HB_23682 ...\n");
#endif


#ifdef HB_26114
    system("cp ./buggy/MobUtils.java ./hbase-2.2.2-src/hbase-server/src/main/java/org/apache/hadoop/hbase/mob/");
    printf("inject HB_26114 ...\n");
#else
    fp1 = fopen("./buggy/MobUtils.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/MobUtils.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 874)
        {
            for (int j = 0; j < 2; j++)
            {
                fputs(str4[j], fp2);
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
    system("cp ./fixed/MobUtils.java ./hbase-2.2.2-src/hbase-server/src/main/java/org/apache/hadoop/hbase/mob/");
    printf("don't inject HB_26114 ...\n");
#endif


#ifdef HB_24189
    system("cp ./buggy/CommonFSUtils.java ./hbase-2.2.2-src/hbase-common/src/main/java/org/apache/hadoop/hbase/util/");
    system("cp ./buggy/WALSplitter.java ./hbase-2.2.2-src/hbase-server/src/main/java/org/apache/hadoop/hbase/wal/");
    printf("inject HB_24189 ...\n");
#else
    system("cp ./fixed/CommonFSUtils.java ./hbase-2.2.2-src/hbase-common/src/main/java/org/apache/hadoop/hbase/util/");
    system("cp ./fixed/WALSplitter.java ./hbase-2.2.2-src/hbase-server/src/main/java/org/apache/hadoop/hbase/wal/");
    printf("don't inject HB_24189 ...\n");
#endif
}
