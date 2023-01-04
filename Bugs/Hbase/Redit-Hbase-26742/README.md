# Redit-HBASE-26742

### Details

Title: delete with null columnQualifier occurs NullPointerException when NewVersionBehavior is on

|         Label         |        Value        |      Label      |         Value          |
|:---------------------:|:-------------------:|:---------------:|:----------------------:|
|       **Type**        |         Bug         |  **Priority**   |         Major          |
|      **Status**       |      RESOLVED       | **Resolution**  |         Fixed          |
| **Affects Version/s** | 1.8.0 , 3.0.0-alpha-2 , 2.4.9 | **Component/s** |     None     |

### Description

 In server side, checkAndMutate ignores CompareOperator for null or empty comparator value, but NOT_EQUAL should be treated specially.

The check logic in HRegion#checkAndMutateInternal is as follows,

```
boolean valueIsNull =
  comparator.getValue() == null || comparator.getValue().length == 0;
if (result.isEmpty() && valueIsNull) {
  matches = true;
} else if (result.size() > 0 && result.get(0).getValueLength() == 0 && valueIsNull) {
  matches = true;
  cellTs = result.get(0).getTimestamp();
} else if (result.size() == 1 && !valueIsNull) {
  Cell kv = result.get(0);
  cellTs = kv.getTimestamp();
  int compareResult = PrivateCellUtil.compareValue(kv, comparator);
  matches = matches(op, compareResult);
}
```

For current logics, here are some  counter examples(Comparator value is set null),

1. result is null, operator is NOT_EQUAL, but matches is true;
2. result size >0, the value of the first cell is empty, operator is NOT_EQUAL, but matches is true;
3. result size is 1, operator is NOT_EQUAL, but matches is false;

### Testcase

Start an hbase cluster, connect to the cluster and get the admin object. Create a table, add test data to the table, and use the checkAndMutate method to generate a Failure:

```
org.junit.ComparisonFailure: 
Expected :v1
Actual   :v0
<Click to see difference>

	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at io.redit.samples.hbase26742.SampleTest.testCheckAndMutateForNull(SampleTest.java:113)
	at io.redit.samples.hbase26742.SampleTest.sampleTest(SampleTest.java:94)
	......
```