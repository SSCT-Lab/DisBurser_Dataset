#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef HB_21920
    system("cp ./buggy/HBaseFsck.java ./hbase-1.4.8-src/hbase-server/src/main/java/org/apache/hadoop/hbase/util/");
    printf("inject HB_21920 ...\n");
#else
    system("cp ./fixed/HBaseFsck.java ./hbase-1.4.8-src/hbase-server/src/main/java/org/apache/hadoop/hbase/util/");
    printf("don't inject HB_21920 ...\n");
#endif


#ifdef HB_23359
    system("cp ./buggy/CompactSplitThread.java ./hbase-1.4.8-src/hbase-server/src/main/java/org/apache/hadoop/hbase/regionserver/");
    printf("inject HB_23359 ...\n");
#else
    system("cp ./fixed/CompactSplitThread.java ./hbase-1.4.8-src/hbase-server/src/main/java/org/apache/hadoop/hbase/regionserver/");
    printf("don't inject HB_23359 ...\n");
#endif


#ifdef HB_23693
    system("cp ./buggy/MetaTableAccessor.java ./hbase-1.4.8-src/hbase-client/src/main/java/org/apache/hadoop/hbase/");
    system("cp ./buggy/RegionStates.java ./hbase-1.4.8-src/hbase-server/src/main/java/org/apache/hadoop/hbase/master/");
    printf("inject HB_23693 ...\n");
#else
    system("cp ./fixed/MetaTableAccessor.java ./hbase-1.4.8-src/hbase-client/src/main/java/org/apache/hadoop/hbase/");
    system("cp ./fixed/RegionStates.java ./hbase-1.4.8-src/hbase-server/src/main/java/org/apache/hadoop/hbase/master/");
    printf("don't inject HB_23693 ...\n");
#endif
}
