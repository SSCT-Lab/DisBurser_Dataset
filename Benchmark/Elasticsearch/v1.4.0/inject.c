#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef ES_7580
    system("cp ./buggy/Bootstrap.java ./elasticsearch-1.4.0-src/src/main/java/org/elasticsearch/bootstrap/");
    system("rm -rf ./elasticsearch-1.4.0-src/src/main/java/org/elasticsearch/bootstrap/JVMCheck.java");
    printf("inject ES_7580 ...\n");
#else
    system("cp ./fixed/Bootstrap.java ./elasticsearch-1.4.0-src/src/main/java/org/elasticsearch/bootstrap/");
    system("cp ./fixed/JVMCheck.java ./elasticsearch-1.4.0-src/src/main/java/org/elasticsearch/bootstrap/");
    printf("don't inject ES_7580 ...\n");
#endif


#ifdef ES_9541
    system("cp ./buggy/ZenDiscovery.java ./elasticsearch-1.4.0-src/src/main/java/org/elasticsearch/discovery/zen/");
    printf("inject ES_9541 ...\n");
#else
    system("cp ./fixed/ZenDiscovery.java ./elasticsearch-1.4.0-src/src/main/java/org/elasticsearch/discovery/zen/");
    printf("don't inject ES_9541 ...\n");
#endif
}
