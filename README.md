# Redit Bugs Benchmark

*Benchmarks of distributed systems*

## Components

| benchmark | create_time | reference |
| :----: | :----: | :----: |
|       Zookeeper     |       2022_05_24        |          https://github.com/apache/zookeeper        |
|       Kafka         |       2022_05_28        |          https://github.com/apache/kafka            | 
|       Hbase         |       2022_06_01        |          https://github.com/apache/hbase            |
|       Hadoop        |       2022_06_08        |          https://github.com/apache/hadoop           |
|       Elasticsearch |       2022_06_18        |          https://github.com/elastic/elasticsearch   |
|       Cassandra     |       2022_06_27        |          https://github.com/apache/cassandra        |
|       Rocketmq      |       2022_06_30        |          https://github.com/apache/rocketmq         |
|       Activemq      |       2022_07_01        |          https://github.com/apache/activemq         |


### Zookeeper

Apache ZooKeeper is an effort to develop and maintain an open-source server which enables highly reliable distributed coordination.

#### Role

**Leader :**

- The only scheduler and processor of transaction requests (write operations), ensuring the order of cluster transaction processing.
  
- The scheduler of each server within the cluster.
  
- For the write operation request, it needs to be forwarded to the leader for processing. The leader needs to decide the number and execute the operation.


**Follower :**

- Process client non-transaction (read operation) requests, forward transaction requests to Leader

- Participate in cluster Leader election voting 2n-1 units can vote for the cluster.


**Observer :**

- For zookeeper clusters with a large number of visits, the observer role can also be added.

- Observe the latest state changes of the Zookeeper cluster and synchronize these states, which can be processed independently for non-transactional requests, and forwarded to the Leader server for processing for transactional requests.

- It will not participate in any form of voting and only provide non-transactional services, which are usually used to improve the non-transactional processing capability of the cluster without affecting the transactional processing capability of the cluster.



### Kafka

Apache Kafka is an open-source distributed event streaming platform used by thousands of companies for high-performance data pipelines, streaming analytics, data integration, and mission-critical applications.

#### Role

**Server :**

- Kafka is run as a cluster of one or more servers that can span multiple datacenters or cloud regions. Some of these servers form the storage layer, called the brokers.
  
- Other servers run Kafka Connect to continuously import and export data as a stream of events to integrate Kafka with your existing systems such as relational databases and other Kafka clusters.
  
- Kafka clusters are highly scalable and fault-tolerant.


**Client :**

- They allow you to write distributed applications and microservices that read, write, and process streams of events in a parallel, large-scale, and fault-tolerant manner, even in the event of network problems or machine failures.

- Kafka ships with a few such clients that are enhanced by dozens of clients provided by the Kafka community.



### Hbase

Apache HBase is an open-source, distributed, versioned, column-oriented store modeled after Google' Bigtable.

#### Features

- Linear and modular scalability.

- Strictly consistent reads and writes.

- Automatic and configurable sharding of tables

- Automatic failover support between RegionServers.

- Convenient base classes for backing Hadoop MapReduce jobs with Apache HBase tables.

- Easy to use Java API for client access.

- Block cache and Bloom Filters for real-time queries.

- Query predicate push down via server side Filters

- Thrift gateway and a REST-ful Web service that supports XML, Protobuf, and binary data encoding options

- Extensible jruby-based (JIRB) shell

- Support for exporting metrics via the Hadoop metrics subsystem to files or Ganglia; or via JMX



### Hadoop

The Apache Hadoop software library is a framework that allows for the distributed processing of large data sets across clusters of computers using simple programming models. It is designed to scale up from single servers to thousands of machines, each offering local computation and storage. Rather than rely on hardware to deliver high-availability, the library itself is designed to detect and handle failures at the application layer, so delivering a highly-available service on top of a cluster of computers, each of which may be prone to failures.

#### Modules

- Hadoop Common: The common utilities that support the other Hadoop modules.

- Hadoop Distributed File System (HDFS™): A distributed file system that provides high-throughput access to application data.

- Hadoop YARN: A framework for job scheduling and cluster resource management.

- Hadoop MapReduce: A YARN-based system for parallel processing of large data sets.



### Elasticsearch

Elasticsearch is the distributed search and analytics engine at the heart of the Elastic Stack.

#### Features

- Elasticsearch provides near real-time search and analytics for all types of data. Whether you have structured or unstructured text, numerical data, or geospatial data, Elasticsearch can efficiently store and index it in a way that supports fast searches.

- You can go far beyond simple data retrieval and aggregate information to discover trends and patterns in your data. And as your data and query volume grows, the distributed nature of Elasticsearch enables your deployment to grow seamlessly right along with it.

- While not every problem is a search problem, Elasticsearch offers speed and flexibility to handle data in a wide variety of use cases.



### Cassandra

Apache Cassandra is a highly-scalable partitioned row store. Rows are organized into tables with a required primary key.

#### Features

- Distribution provides power and resilience.

- Want more power? Add more nodes.

- partitions.

- Replication ensures reliability and fault tolerance.

- Tuning your consistency.



### Rocketmq

Apache RocketMQ is a distributed messaging and streaming platform with low latency, high performance and reliability, trillion-level capacity and flexible scalability.

#### Features

- Messaging patterns including publish/subscribe, request/reply and streaming.

- Financial grade transactional message

- Built-in fault tolerance and high availability configuration options base on DLedger.

- Versatile big-data and streaming ecosystem integration



### Activemq

Apache ActiveMQ is a high performance Apache 2.0 licensed Message Broker and JMS 1.1 implementation.

#### Features

- Supports a variety of Cross Language Clients and Protocols from Java, C, C++, C#, Ruby, Perl, Python, PHP.

- Full support for the Enterprise Integration Patterns both in the JMS client and the Message Broker.

- Supports many advanced features such as Message Groups, Virtual Destinations, Wildcards and Composite Destinations.

- Fully supports JMS 1.1 and J2EE 1.4 with support for transient, persistent, transactional and XA messaging.
