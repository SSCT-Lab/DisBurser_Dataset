# Redit-Zookeeper-4473

### Details
Title: zooInspector create root node fail with path validate

|         Label         | Value              | Label           | Value               |
|:---------------------:|:--------:          |:---------------:|:--------:           |
|       **Type**        | Bug                | **Priority**    | Major               |
|      **Status**       | RESOLVED           | **Resolution**  | Fixed               |
| **Affects Version/s** | 3.7.0, 3.6.2       | **Component/s** | contrib             |

### Description
Create root node using zoo inspector will fail with an exception, and what's worse is the UI will be updated, even creation failed.
```
java.lang.IllegalArgumentException: Invalid path string "//test" caused by empty node name specified @1
```

### Testcase
Start a three-node zookeeper cluster, create a ZooInspectorManagerImpl object and connect to the cluster. Test create zookeeper node operation and create a normal child node. The above exception will be thrown during node creation.
