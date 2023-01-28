package com.wyd.server.service;

public class UserServiceFactory {

    private static UserService userService = new UserServiceMemoryImpl();

    public static UserService getUserService(){
        return userService;
    }
}
