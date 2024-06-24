package com.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author xh
 * @date 2024-06-24
 * @Description:
 */
public class CodeTest {

    public static void main(String[] args) throws IOException {
        System.out.println(Charset.defaultCharset());
        // String x = new String("小明".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        // String y = new String(x.getBytes(StandardCharsets.UTF_8), Charset.forName("GBK"));
        String x = new String("小明".getBytes(Charset.forName("GBK")));
        System.out.println(x);
        // System.out.println(y);

        FileWriter fileWriter = new FileWriter("output.txt");
        fileWriter.write(x);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        bufferedWriter.close();
    }
}
