#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[7][200] =  {	"  <dependencies>\n",
    				"    <dependency>\n",
    				"      <groupId>org.mockito</groupId>\n",
    				"      <artifactId>mockito-core</artifactId>\n",
    				"      <scope>test</scope>\n",
    				"    </dependency>\n",
    				"  </dependencies>\n"};
    char str2[200]    = 	"import org.apache.zookeeper.inspector.logger.LoggerFactory;\n";
    char str3[6][200] =  {	"    private void showWarnDialog(String message){\n",
    				"        JOptionPane.showMessageDialog(this,\n",
    				"                message, \"Error\",\n",
    				"                JOptionPane.ERROR_MESSAGE);\n",
    				"    }\n",
    				"\n"};
    char str4[23][200] =  {	"                    boolean success;\n",
    				"                    try {\n",
    				"                        success = get();\n",
    				"                    } catch (Exception e) {\n",
    				"                        success = false;\n",
    				"                        LoggerFactory.getLogger().error(\"create fail for {} {}\", parentNode, newNodeName, e);\n",
    				"                        showWarnDialog(\"create \" + newNodeName + \" in \" + parentNode + \" fail, exception is \" + e.getMessage());\n",
    				"                    }\n",
    				"\n",
    				"                    if (!success) {\n",
    				"                        showWarnDialog(\"create \" + newNodeName + \" in \" + parentNode + \" fail, see log for more detail\");\n",
    				"                    }\n",
    				"                    else {\n",
    				"                        int i = 0;\n",
    				"                        for (; i < parentNode.getChildCount(); i++) {\n",
    				"                            ZooInspectorTreeNode existingChild = (ZooInspectorTreeNode) parentNode.getChildAt(i);\n",
    				"                            if (newNodeName.compareTo(existingChild.getName()) < 0) {\n",
    				"                                break;\n",
    				"                            }\n",
    				"                        }\n",
    				"                        insertNodeInto(new ZooInspectorTreeNode(newNodeName, parentNode, 0), parentNode, i);\n",
    				"                        parentNode.setNumDisplayChildren(parentNode.getNumDisplayChildren() + 1);\n",
    				"                    }\n"};
    char str5[200]    = 	"    DataEncryptionManager encryptionManager;\n";
    char str6[200]    = 	"    ZooKeeper zooKeeper;\n";
    char str7[7][200] =  {	"                    String node;\n",
    				"                    if (parent.endsWith(\"/\")) {\n",
    				"                        node = parent + nodeElement;\n",
    				"                    }\n",
    				"                    else {\n",
    				"                        node = parent + \"/\" + nodeElement;\n",
    				"                    }\n"};

#ifdef ZK_4473
    system("cp ./buggy/pom.xml ./apache-zookeeper-3.8.0/zookeeper-contrib/zookeeper-contrib-zooinspector/");
    system("cp ./buggy/ZooInspectorTreeView.java ./apache-zookeeper-3.8.0/zookeeper-contrib/zookeeper-contrib-zooinspector/src/main/java/org/apache/zookeeper/inspector/gui/");
    system("cp ./buggy/ZooInspectorManagerImpl.java ./apache-zookeeper-3.8.0/zookeeper-contrib/zookeeper-contrib-zooinspector/src/main/java/org/apache/zookeeper/inspector/manager/");
    printf("inject ZK_4473 ...\n");
#else
    // File 1: pom.xml
    fp1 = fopen("./buggy/pom.xml", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/pom.xml", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 82)
        {
            for (int j = 0; j < 7; j++)
            {
                fputs(str1[j], fp2);
            }
            fputs(str, fp2);
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
    system("cp ./fixed/pom.xml ./apache-zookeeper-3.8.0/zookeeper-contrib/zookeeper-contrib-zooinspector/");

    // File 2: ZooInspectorTreeView.java
    fp1 = fopen("./buggy/ZooInspectorTreeView.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/ZooInspectorTreeView.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 21)
        {
            fputs(str2, fp2);
            fputs(str, fp2);
        }
        else if (i == 357)
        {
            for (int j = 0; j < 6; j++)
            {
                fputs(str3[j], fp2);
            }
            fputs(str, fp2);
        }
        else if (i == 388)
        {
            for (int j = 0; j < 23; j++)
            {
                fputs(str4[j], fp2);
            }
        }
        else if (i == 389 || i == 390 || i == 391 || i == 392 || i == 393 || i == 394 || i == 395 || i == 396 || i == 397 || i == 398)
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
    system("cp ./fixed/ZooInspectorTreeView.java ./apache-zookeeper-3.8.0/zookeeper-contrib/zookeeper-contrib-zooinspector/src/main/java/org/apache/zookeeper/inspector/gui/");

    // File 3: ZooInspectorManagerImpl.java
    fp1 = fopen("./buggy/ZooInspectorManagerImpl.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/ZooInspectorManagerImpl.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 108)
        {
            fputs(str5, fp2);
        }
        else if (i == 111)
        {
            fputs(str6, fp2);
        }
        else if (i == 392)
        {
            for (int j = 0; j < 7; j++)
            {
                fputs(str7[j], fp2);
            }
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
    system("cp ./fixed/ZooInspectorManagerImpl.java ./apache-zookeeper-3.8.0/zookeeper-contrib/zookeeper-contrib-zooinspector/src/main/java/org/apache/zookeeper/inspector/manager/");
    printf("don't inject ZK_4473 ...\n");
#endif
}
