# Redit-HDFS-14987

### Details

Title: EC: EC file blockId location info displaying as "null" with hdfs fsck -blockId command

|         Label         |  Value   |      Label      |    Value    |
|:---------------------:|:--------:|:---------------:|:-----------:|
|       **Type**        |   Bug    |  **Priority**   |    Major    |
|      **Status**       | RESOLVED | **Resolution**  |  Duplicate  |
| **Affects Version/s** |  3.1.2   | **Component/s** |  ec, tools  |

### Description

EC file blockId location info displaying as "null" with hdfs fsck -blockId command

Check the blockId information of an EC enabled file with "hdfs fsck -blockId"  Check the blockId information of an EC enabled file with "hdfs fsck -blockId"    blockId location related info will display as null,which needs to be rectified.    
Check the attachment "EC_file_block_info"

=======================================================
![img.png](https://issues.apache.org/jira/secure/attachment/12985740/12985740_image-2019-11-13-18-36-29-063.png)

Actual Output :-     null   
Expected output :- It should display the blockId location related info as (nodes, racks) of the block  as specified in the usage info of fsck -blockId option.                 [like : Block replica on datanode/rack: BLR10000xx038/default-rack is HEALTHY]

### Testcase

Start a hadoop cluster, create two test folders in hdfs, and set their erasure coding policies to XOR-2-1-1024k and replicate respectively. Pass in a txt file in each of the two files, and obtain the blockId of the file storage, through /bin/hdfs fsck checks the block information separately, and the results are as follows:
```
$ /hadoop/hadoop-3.1.2/bin/hdfs fsck /test_ec/aa.txt -blockId blk_-9223372036854775792

Block Id: blk_-9223372036854775792
Block belongs to: /test_ec/aa.txt
No. of Expected Replica: 2
No. of live Replica: 2
No. of excess Replica: 0
No. of stale Replica: 2
No. of decommissioned Replica: 0
No. of decommissioning Replica: 0
No. of corrupted Replica: 0
null

Fsck on blockId 'blk_-9223372036854775792

$ /hadoop/hadoop-3.1.2/bin/hdfs fsck /test_replica/aa.txt -blockId blk_1073741825

Block Id: blk_1073741825
Block belongs to: /test_replica/aa.txt
No. of Expected Replica: 3
No. of live Replica: 3
No. of excess Replica: 0
No. of stale Replica: 3
No. of decommissioned Replica: 0
No. of decommissioning Replica: 0
No. of corrupted Replica: 0
Block replica on datanode/rack: dn3/default-rack is HEALTHY
Block replica on datanode/rack: dn2/default-rack is HEALTHY
Block replica on datanode/rack: dn1/default-rack is HEALTHY

```
