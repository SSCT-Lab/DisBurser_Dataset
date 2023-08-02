#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef ES_6866
    system("cp ./buggy/MasterFaultDetection.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/discovery/zen/fd/");
    system("cp ./buggy/NodesFaultDetection.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/discovery/zen/fd/");
    printf("inject ES_6866 ...\n");
#else
    system("cp ./fixed/MasterFaultDetection.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/discovery/zen/fd/");
    system("cp ./fixed/NodesFaultDetection.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/discovery/zen/fd/");
    printf("don't inject ES_6866 ...\n");
#endif


#ifdef ES_7210
    system("cp ./buggy/pom.xml ./elasticsearch-1.2.1-src/");
    system("cp ./buggy/LZFCompressedStreamOutput.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/common/compress/lzf/");
    system("cp ./buggy/LZFCompressor.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/common/compress/lzf/");
    printf("inject ES_7210 ...\n");
#else
    system("cp ./fixed/pom.xml ./elasticsearch-1.2.1-src/");
    system("cp ./fixed/LZFCompressedStreamOutput.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/common/compress/lzf/");
    system("cp ./fixed/LZFCompressor.java ./elasticsearch-1.2.1-src/src/main/java/org/elasticsearch/common/compress/lzf/");
    printf("don't inject ES_7210 ...\n");
#endif
}
