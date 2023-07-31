#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef HB_26742
    system("cp ./buggy/HRegion.java ./hbase-2.4.9-src/hbase-server/src/main/java/org/apache/hadoop/hbase/regionserver/");
    printf("inject HB_26742 ...\n");
#else
    system("cp ./fixed/HRegion.java ./hbase-2.4.9-src/hbase-server/src/main/java/org/apache/hadoop/hbase/regionserver/");
    printf("don't inject HB_26742 ...\n");
#endif
}
