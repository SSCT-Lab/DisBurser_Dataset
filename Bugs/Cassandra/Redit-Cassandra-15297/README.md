# Redit-Cassandra-15297

### Details

Title: nodetool can not create snapshot with snapshot name that have special character

|         Label         |                  Value                   |      Label      |     Value      |
|:---------------------:|:----------------------------------------:|:---------------:|:--------------:|
|       **Type**        |                   Bug                    |  **Priority**   |     Normal     |
|      **Status**       |                 RESOLVED                 | **Resolution**  |     Fixed      |
| **Affects Version/s** | 3.0.0                                    | **Component/s** | Tool/nodetool  |

### Description

We make snapshot through "nodetool snapshot -t snapshotname " , when snapshotname contains special characters like "/", the make snapshot process successfully , but the result
can be different ,when we check the data file directory or use "nodetool listsnapshots".
here is some case :

1. nodetool snapshot -t "p/s"
The listsnapshot resturns snapshot p for all table but actually the snapshot name is "p/s"; also the data directory is like the format : datapath/snapshots/p/s/snapshot-datafile-link

2. nodetool snapshot -t "/"
The listsnapshot resturns "there is not snapshot"; but the make snapshot process return successfully and the data directory is like the format : datapath/snapshots/snapshot-datafile-link

The Attachements are the result under our environment. So for me , we suggest that the snapshot name should not contains special character. just throw exception and told the user not to use special character.

### Testcase

1. Start a Cassandra cluster, set snapshots for all keyspaces through bin/nodetool snapshot -t snapshotname, and the address is "p/s". Check the snapshots through bin/nodetool listsnapshots and find that the Snapshot name is p, and its actual snapshot address is "data_file + /snapshots/p/s"
2. Start a Cassandra cluster, set snapshots for all keyspaces through bin/nodetool snapshot -t snapshotname, the address is "/", check the snapshots through bin/nodetool listsnapshots and find that there are no snapshots, the actual snapshot address is "data_file + /snapshots/"