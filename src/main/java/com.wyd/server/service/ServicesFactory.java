package com.wyd.server.service;

import com.wyd.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServicesFactory {

    static Properties properties;
    static Map<Class<?>, Object> map = new ConcurrentHashMap<>();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                if (name.endsWith("Service")) {
                    Class<?> interfaceName = Class.forName(name);
                    Object impl = Class.forName(properties.getProperty(name)).newInstance();
                    map.put(interfaceName, impl);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getService(Class<T> interfaceClass){
        return (T) map.get(interfaceClass);
    }

}
