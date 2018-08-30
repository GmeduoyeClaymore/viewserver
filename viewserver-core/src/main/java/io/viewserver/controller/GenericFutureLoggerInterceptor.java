package io.viewserver.controller;

public class GenericFutureLoggerInterceptor {


    public static String name;
    public static Object[] args;

    public static void intercept(String name, Object[] args){
        GenericFutureLoggerInterceptor.name = name;
        GenericFutureLoggerInterceptor.args = args;
    }

    public static void reset() {
        name = null;
        args = null;
    }

}
