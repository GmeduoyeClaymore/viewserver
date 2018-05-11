package io.viewserver.command;

import io.viewserver.controller.ControllerContext;
import io.viewserver.execution.Options;
import io.viewserver.network.IPeerSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionUtils {
    public static void substituteParamsInFilterExpression(IPeerSession session, Options options) {
        Set<String> params = ControllerContext.getParamNames(session);
        if (params != null) {
            substituteParamsInFilterExpression(params,options, session);

        }
    }
    public static void substituteParamsInFilterExpression(Set<String> params, Options options, IPeerSession session) {
        String filterExpression = options.getFilterExpression();
        if (filterExpression != null && filterExpression.contains("@")) {
            for (String param : params) {

                Object value = ControllerContext.get(param,session);
                if (value != null && filterExpression.contains(String.format("\"@%s\"", param))) {
                    options.setFilterExpression(filterExpression.replace("@" + param, value.toString()));
                }
            }
        }
    }
}
