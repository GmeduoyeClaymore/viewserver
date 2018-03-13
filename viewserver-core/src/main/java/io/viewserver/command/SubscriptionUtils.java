package io.viewserver.command;

import io.viewserver.controller.ControllerContext;
import io.viewserver.execution.Options;
import io.viewserver.network.IPeerSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionUtils {
    public static void substituteParamsInFilterExpression(IPeerSession session, Options options) {
        ConcurrentHashMap<String, Object> params = ControllerContext.getParams(session);
        if (params != null) {
            substituteParamsInFilterExpression(params,options);

        }
    }
    public static void substituteParamsInFilterExpression(ConcurrentHashMap<String, Object> params, Options options) {
        String filterExpression = options.getFilterExpression();
        if (filterExpression != null && filterExpression.contains("@")) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();
                if (value != null && filterExpression.contains("@" + key)) {
                    options.setFilterExpression(filterExpression.replace("@" + key, value.toString()));
                }
            }
        }
    }
}
