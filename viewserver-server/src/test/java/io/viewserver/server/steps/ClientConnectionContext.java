package io.viewserver.server.steps;

import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.client.ClientSubscription;
import io.viewserver.client.CommandResult;
import io.viewserver.client.ViewServerClient;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IGenericJSONCommand;
import io.viewserver.network.Command;

import java.util.HashMap;
import java.util.Map;

public class ClientConnectionContext{
    private String name;
    private ViewServerClient client;
    private ViewServerClientContext viewServerClientContext;
    private ReportContext reportContext = new ReportContext();
    private Options options = new Options();
    private Map<String, ClientSubscription> subscriptions = new HashMap<>();
    private Map<String, TestSubscriptionEventHandler> eventHandlerHashMap = new HashMap<>();

    public ClientConnectionContext(String name,ViewServerClient client, ViewServerClientContext viewServerClientContext) {
        this.name = name;
        this.client = client;
        this.viewServerClientContext = viewServerClientContext;
        options.setOffset(0);
        options.setLimit(100);
    }

    public String getName() {
        return name;
    }

    public ViewServerClient getClient() {
        return client;
    }

    public ReportContext getReportContext() {
        return reportContext;
    }

    public Options getOptions() {
        return options;
    }

    public Map<String, ClientSubscription> getSubscriptions() {
        return subscriptions;
    }


    public ClientSubscription addSubscription(String name, ClientSubscription clientSubscription, TestSubscriptionEventHandler eventHandler) {
        ClientSubscription sub = subscriptions.get(name);
        if(sub != null){
            try {
                sub.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        subscriptions.put(name, clientSubscription);
        TestSubscriptionEventHandler handler = eventHandlerHashMap.get(name);
        eventHandlerHashMap.put(name, eventHandler);
        return clientSubscription;
    }

    public void removeSubscription(String name){
        subscriptions.remove(name);
        eventHandlerHashMap.remove(name);
    }

    public ClientSubscription getSubscription(String name) {
        return subscriptions.get(name);
    }

    public TestSubscriptionEventHandler getSubscriptionEventHandler(String name) {
        return eventHandlerHashMap.get(name);
    }


    public ListenableFuture<ClientSubscription> subscribe(String operatorName, Options options, TestSubscriptionEventHandler testSubscriptionEventHandler) {
        return client.subscribe(operatorName,options,testSubscriptionEventHandler);
    }

    public ListenableFuture<ClientSubscription> subscribeToReport(TestSubscriptionEventHandler eventHandler) {
        return this.client.subscribeToReport(
                getReportContext(),
                getOptions(),
                eventHandler);
    }

    public void setResult(String controllerName, String action, String result) {
        if(result != null){
            this.viewServerClientContext.getContextParams().put(name + "_" + controllerName + "_" + action + "_result", result.replaceAll("^\"|\"$",""));
        }
    }

    public ListenableFuture<CommandResult> invokeJSONCommand(String controllerName, String action, String data) {
        IGenericJSONCommand genericJSONCommand = MessagePool.getInstance().get(IGenericJSONCommand.class)
                .setAction(action)
                .setPayload(data)
                .setPath(controllerName);
        Command command = new Command("genericJSON", genericJSONCommand);

        return client.sendCommand(command);
    }



}
