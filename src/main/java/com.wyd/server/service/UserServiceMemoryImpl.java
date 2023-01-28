package com.wyd.server.service;

import java.util.HashMap;
import java.util.Map;

public class UserServiceMemoryImpl implements UserService{

    private static Map<String, String> userMap = new HashMap<>();

    {
        userMap.put("wyd","123");
        userMap.put("yxy","123");
        userMap.put("zhangsan","123");
        userMap.put("wangwu","123");
        userMap.put("lisi","123");
        userMap.put("xiaoming","123");
    }

    @Override
    public boolean login(String username, String password) {
        String s = userMap.get(username);
        if (s != null){
            return s.equals(password);
        }
        return false;
    }


}
