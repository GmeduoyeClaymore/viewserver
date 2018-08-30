package io.viewserver.controller;

public class FutureLoggerInterceptor {

    public static boolean plusOneIntercepted;
    public static Integer plusInterceptedAddition;
    private static Object userId;

    public static void plusOneIntercepted(){
        FutureLoggerInterceptor.userId = ControllerContext.get("userId");
        FutureLoggerInterceptor.plusOneIntercepted = true;
    }
    public static void plusIntercepted(Integer addition){
        FutureLoggerInterceptor.userId = ControllerContext.get("userId");
        FutureLoggerInterceptor.plusInterceptedAddition = addition;
    }

    public static void reset() {
        userId = null;
        plusOneIntercepted = false;
        plusInterceptedAddition = null;
    }
}


