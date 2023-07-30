#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef ES_8321
    system("cp ./buggy/ClusterDynamicSettingsModule.java ./elasticsearch-1.5.0-src/src/main/java/org/elasticsearch/cluster/settings/");
    system("cp ./buggy/ZenDiscovery.java ./elasticsearch-1.5.0-src/src/main/java/org/elasticsearch/discovery/zen/");
    printf("inject ES_8321 ...\n");
#else
    system("cp ./fixed/ClusterDynamicSettingsModule.java ./elasticsearch-1.5.0-src/src/java/org/apache/cassandra/cql3/");
    system("cp ./fixed/ZenDiscovery.java ./elasticsearch-1.5.0-src/src/main/java/org/elasticsearch/discovery/zen/");
    printf("don't inject ES_8321 ...\n");
#endif
}
