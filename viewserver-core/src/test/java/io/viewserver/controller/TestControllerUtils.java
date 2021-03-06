package io.viewserver.controller;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Created by Gbemiga on 30/01/18.
 */
public class TestControllerUtils {

    private static ListeningExecutorService service = MoreExecutors.newDirectExecutorService();


    public static String invoke(ControllerRegistration reg, String method, String params) {
        return invoke("foo",reg,method,params);
    }
    public static String invoke(String userId,ControllerRegistration reg, String method, String params) {
        try {
            ControllerContext ctxt = getControllerContext(userId);
            return reg.getActions().get(method).invoke(params,ctxt,service).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ControllerContext getControllerContext(String userId) {
        ControllerContext ctxt = ControllerContext.create(new MockSession());
        ControllerContext.set("userId",userId);
        return ctxt;
    }
}
