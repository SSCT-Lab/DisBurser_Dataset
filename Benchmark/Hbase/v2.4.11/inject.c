#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef HB_26901
    system("cp ./buggy/NewVersionBehaviorTracker.java ./hbase-2.4.11-src/hbase-server/src/main/java/org/apache/hadoop/hbase/regionserver/querymatcher/");
    printf("inject HB_26901 ...\n");
#else
    system("cp ./fixed/NewVersionBehaviorTracker.java ./hbase-2.4.11-src/hbase-server/src/main/java/org/apache/hadoop/hbase/regionserver/querymatcher/");
    printf("don't inject HB_26901 ...\n");
#endif
}
