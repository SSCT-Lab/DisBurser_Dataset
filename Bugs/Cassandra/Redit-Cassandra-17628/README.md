# Redit-Cassandra-17628

### Details

Title: CQL writetime and ttl functions should be forbidden for multicell columns

|         Label         |                  Value                   |      Label      |     Value      |
|:---------------------:|:----------------------------------------:|:---------------:|:--------------:|
|       **Type**        |                   Bug                    |  **Priority**   |    Normal      |
|      **Status**       |                 RESOLVED                 | **Resolution**  |     Fixed      |
| **Affects Version/s** |                  None                    | **Component/s** |  CQL/Semantics |

### Description

CQL writetime and ttl functions are currently forbidden for collections, frozen or not. Also, they are always allowed for UDTs, frozen or not:

```
CREATE TYPE udt (a int, b int);
CREATE TABLE t (k int PRIMARY KEY, s set<int>, fs frozen<set<int>>, t udt, ft frozen<udt>);

SELECT writetime(s) FROM t; -- fails
SELECT writetime(st) FROM t; -- fails
SELECT writetime(t) FROM t; -- allowed
SELECT writetime(ft) FROM t; -- allowed
```

This is done by checking in Selectable.WritetimeOrTTL#newSelectorFactory whether the column is a collection or not. However, I think that what we should check is whether the column is multi-cell. That way the function would work with frozen collections and UDTs, and it would reject unfrozen collections and UDTs:

```
SELECT writetime(s) FROM t; -- fails
SELECT writetime(st) FROM t; -- allowed
SELECT writetime(t) FROM t; -- fails
SELECT writetime(ft) FROM t; -- allowed
```

### Testcase

Start a Cassandra cluster, define a cluster class to connect to the cluster, and get the session object. Create a keyspace, customize the data type UDT and create a column family containing that type. Tests whether the writetime and ttl functions are currently prohibited for collections and UDTs.