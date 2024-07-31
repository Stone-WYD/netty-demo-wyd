package com.test.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author xh
 * @date 2024-07-02
 * @Description:
 */
public class ConfigPropertiesUtil {

    private final static Properties prop = new Properties();

    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            // 加载properties文件
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return prop.getProperty(key);
    }

    public static Properties getProp() {
        return prop;
    }

}
