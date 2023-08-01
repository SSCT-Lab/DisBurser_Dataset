#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[14][200]    = {	"      // report block replicas status on datanodes\n",
    				"      if (blockInfo.isStriped()) {\n",
    				"        for (int idx = (blockInfo.getCapacity() - 1); idx >= 0; idx--) {\n",
    				"          DatanodeDescriptor dn = blockInfo.getDatanode(idx);\n",
    				"          if (dn == null) {\n",
    				"            continue;\n",
    				"          }\n",
    				"          printDatanodeReplicaStatus(block, corruptionRecord, dn);\n",
    				"        }\n",
    				"      } else {\n",
    				"        for (int idx = (blockInfo.numNodes() - 1); idx >= 0; idx--) {\n",
    				"          DatanodeDescriptor dn = blockInfo.getDatanode(idx);\n",
    				"          printDatanodeReplicaStatus(block, corruptionRecord, dn);\n",
    				"        }\n"};
    				
    char str2[21][200]    = {	"  private void printDatanodeReplicaStatus(Block block,\n",
    				"      Collection<DatanodeDescriptor> corruptionRecord, DatanodeDescriptor dn) {\n",
    				"    out.print(\"Block replica on datanode/rack: \" + dn.getHostName() +\n",
    				"        dn.getNetworkLocation() + \" \");\n",
    				"    if (corruptionRecord != null && corruptionRecord.contains(dn)) {\n",
    				"      out.print(CORRUPT_STATUS + \"\\t ReasonCode: \" +\n",
    				"          blockManager.getCorruptReason(block, dn));\n",
    				"    } else if (dn.isDecommissioned()){\n",
    				"      out.print(DECOMMISSIONED_STATUS);\n",
    				"    } else if (dn.isDecommissionInProgress()) {\n",
    				"      out.print(DECOMMISSIONING_STATUS);\n",
    				"    } else if (this.showMaintenanceState && dn.isEnteringMaintenance()) {\n",
    				"      out.print(ENTERING_MAINTENANCE_STATUS);\n",
    				"    } else if (this.showMaintenanceState && dn.isInMaintenance()) {\n",
    				"      out.print(IN_MAINTENANCE_STATUS);\n",
    				"    } else {\n",
    				"      out.print(HEALTHY_STATUS);\n",
    				"    }\n",
    				"    out.print(\"\\n\");\n",
    				"  }\n",
    				"\n"};


#ifdef HDFS_14987
    system("cp ./buggy/NamenodeFsck.java ./hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/namenode/");
    printf("inject HDFS_14987 ...\n");
#else
    fp1 = fopen("./buggy/NamenodeFsck.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/NamenodeFsck.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 305)
        {
            for (int j = 0; j < 14; j++)
            {
                fputs(str1[j], fp2);
            }
        }
        else if (i == 337)
        {
            for (int j = 0; j < 21; j++)
            {
                fputs(str2[j], fp2);
            }
            fputs(str, fp2);
        }
        else if (i == 306 || i == 307 || i == 308 || i == 309 || i == 310 || i == 311 || i == 312 || i == 313 || i == 314 || i == 315 || i == 316 || i == 317 || i == 318 || i == 319 || i == 320 || i == 321 || i == 322 || i == 323 || i == 324)
        {
        }
        else
        {
            fputs(str, fp2);
        }
        i++;
    }
    i = 0;
    fclose(fp1);
    fclose(fp2);
    system("cp ./fixed/NamenodeFsck.java ./hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/namenode/");
    printf("don't inject HDFS_14987 ...\n");
#endif


#ifdef HDFS_15443
    system("cp ./buggy/DataXceiverServer.java ./hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/datanode/");
    printf("inject HDFS_15443 ...\n");
#else
    system("cp ./fixed/DataXceiverServer.java ./hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/datanode/");
    printf("don't inject HDFS_15443 ...\n");
#endif


#ifdef HDFS_14869
    system("cp ./buggy/DistCp.java ./hadoop-3.1.2-src/hadoop-tools/hadoop-distcp/src/main/java/org/apache/hadoop/tools/");
    system("cp ./buggy/DistCpSync.java ./hadoop-3.1.2-src/hadoop-tools/hadoop-distcp/src/main/java/org/apache/hadoop/tools/");
    printf("inject HDFS_14869 ...\n");
#else
    system("cp ./fixed/DistCp.java ./hadoop-3.1.2-src/hadoop-tools/hadoop-distcp/src/main/java/org/apache/hadoop/tools/");
    system("cp ./fixed/DistCpSync.java ./hadoop-3.1.2-src/hadoop-tools/hadoop-distcp/src/main/java/org/apache/hadoop/tools/");
    printf("don't inject HDFS_14869 ...\n");
#endif
}
