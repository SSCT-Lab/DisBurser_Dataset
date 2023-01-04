# Redit-Kafka-13852

### Details

Title: Kafka Acl documentation bug for wildcard '*'


|         Label         |        Value        |      Label      |        Value        |
|:---------------------:|:-------------------:|:---------------:|:-------------------:|
|       **Type**        |         Bug         |  **Priority**   |        Minor        |
|      **Status**       |      RESOLVED       | **Resolution**  |        Fixed        |
| **Affects Version/s** | 3.1.0, 3.2.0, 3.1.2 | **Component/s** | docs, documentation |

### Description

There is a Kafka Acl documentation bug for wildcard '*' in the Examples.

The bug is when we run the below script in one folder which is not empty, we can not set ACL correctly. However, it
works only the folder is empty.

We can find the scripts with wildcard '*' from the Kafka documentation.

```

// Adding Acls
> bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:Peter --allow-host 198.51.200.1 --producer --topic *

// List Acls
> bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --list --topic *
```

Reproduce the issue:

1. Create a file foo.txt under an empty folder
2. Run the script to add an acl by using the wildcard resource '*'
3. We can find the resource name is 'foo.txt'. Not wildcard '*'

```
// code placeholder
(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  ll
total 0
(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  touch foo.txt
(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  ll
total 0
-rw-r--r--  1 hongwei.xiang  345931250     0B Apr 23 19:05 foo.txt
(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  ~/bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:Peter --allow-host 198.51.200.1 --producer --topic *
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=foo.txt, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=foo.txt, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)
(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  ~/bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:Peter --allow-host 198.51.200.1 --topic * --producer
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=foo.txt, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=foo.txt, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)

(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  ~/bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --list --topic *
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=foo.txt, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)
```

To resolve the issue:

Add single quotes for the wildcard '*' in the script.

```
(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  ~/bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:Peter --allow-host 198.51.200.1 --producer --topic '*'
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=*, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=*, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)


(base)  hongwei.xiang@hongweixiang  ~/Downloads/test  ~/bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --list --topic '*'
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=*, patternType=LITERAL)`:
(principal=User:Peter, host=198.51.200.1, operation=WRITE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=CREATE, permissionType=ALLOW)
(principal=User:Peter, host=198.51.200.1, operation=DESCRIBE, permissionType=ALLOW)
I've submitted a pull request: "KAFKA-13852: Kafka Acl documentation bug for wildcard '*' #12090"
```

I've submitted a pull request: "KAFKA-13852: Kafka Acl documentation bug for wildcard '*' #12090"

### Testcase

First, start zookeeper and kafka in a three-node cluster.
Next, create two directories on server no.1, one is empty and the other is non-empty.
After that, run the script to add an acl by using the wildcard resource '*' in each of the two directories and compare the output, and the above exception is shown in command line.
The ACL of the non-empty directory is 'test.txt' rather than *.
