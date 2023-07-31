# Redit-HDFS-11379

### Details

Title: ***DFSInputStream may infinite loop requesting block locations***

JIRA link：[https://issues.apache.org/jira/browse/HDFS-11379](https://issues.apache.org/jira/browse/HDFS-11379)

|         Label         |  Value   |       Label       |           Value            |
|:---------------------:|:--------:|:-----------------:|:--------------------------:|
|       **Type**        |   Bug    |   **Priority**    |          Critical          |
|      **Status**       | RESOLVED |  **Resolution**   |           Fixed            |
| **Affects Version/s** |  2.7.0   | **Fix Version/s** | 2.8.0, 2.7.4, 3.0.0-alpha4 |

### Description

DFSInputStream creation caches file size and initial range of locations. If the file is truncated (or replaced) and the client attempts to read outside the initial range, the client goes into a tight infinite looping requesting locations for the nonexistent range.

### Testcase

Reproduced version：2.7.0

Steps to reproduce：
1. Create an output stream of the test file and write three block-sized bytes.
2. Prevent initial pre-fetch of multiple block locations.
3. Truncate a file while it's open.
4. Verify that reading bytes outside the initial pre-fetch do not send the client into an infinite loop querying locations.

The strange thing is that the test cases are executed in v2.7.0 and v2.8.0(fix version), and the results are the same.
