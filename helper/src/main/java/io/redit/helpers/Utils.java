package io.redit.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class Utils {
    public static void runCmdList(ArrayList<String> list) {
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            runCmd(it.next());
        }
    }
    public static void runCmd(String cmd) {
        Runtime runtime = Runtime.getRuntime();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(runtime.exec(cmd).getInputStream()));
            String line = null;
            StringBuffer b = new StringBuffer();
            while ((line = br.readLine()) != null) {
                b.append(line + "\n");
            }
            System.out.println("runCmd: " + cmd);
            System.out.println("result: " + b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
