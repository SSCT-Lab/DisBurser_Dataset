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


#ifdef HB_26027
    system("cp ./buggy/AsyncRequestFutureImpl.java ./hbase-2.4.11-src/hbase-client/src/main/java/org/apache/hadoop/hbase/client/");
    printf("inject HB_26027 ...\n");
#else
    system("cp ./fixed/AsyncRequestFutureImpl.java ./hbase-2.4.11-src/hbase-client/src/main/java/org/apache/hadoop/hbase/client/");
    printf("don't inject HB_26027 ...\n");
#endif


#ifdef HB_27027
    system("cp ./buggy/HttpServer.java ./hbase-2.4.11-src/hbase-http/src/main/java/org/apache/hadoop/hbase/http/");
    system("cp ./buggy/RESTServer.java ./hbase-2.4.11-src/hbase-rest/src/main/java/org/apache/hadoop/hbase/rest/");
    system("cp ./buggy/ThriftServer.java ./hbase-2.4.11-src/hbase-thrift/src/main/java/org/apache/hadoop/hbase/thrift/");
    printf("inject HB_27027 ...\n");
#else
    system("cp ./fixed/HttpServer.java ./hbase-2.4.11-src/hbase-http/src/main/java/org/apache/hadoop/hbase/http/");
    system("cp ./fixed/RESTServer.java ./hbase-2.4.11-src/hbase-rest/src/main/java/org/apache/hadoop/hbase/rest/");
    system("cp ./fixed/ThriftServer.java ./hbase-2.4.11-src/hbase-thrift/src/main/java/org/apache/hadoop/hbase/thrift/");
    printf("don't inject HB_27027 ...\n");
#endif
}
