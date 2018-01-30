package com.shotgun.viewserver.user;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.PhoneNumberStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.Controller;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

@Controller(name = "nexmoController")
public class NexmoController {
    private static final Logger log = LoggerFactory.getLogger(NexmoController.class);
    private Catalog systemCatalog;

    public NexmoController(int httpPort, Catalog systemCatalog) {
        this.systemCatalog = systemCatalog;
        this.createHttpServer(httpPort);
    }

    private void createHttpServer(int httpPort) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
            log.debug("PhoneCallController endpoint started at " + httpPort);
            server.createContext("/answer", new CallHandler(this.systemCatalog));
            server.createContext("/event", new EventHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class CallHandler implements HttpHandler {
        private Catalog systemCatalog;

        public CallHandler(Catalog systemCatalog) {
            this.systemCatalog = systemCatalog;
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
            systemCatalog.getExecutionContext().getReactor().scheduleTask(() -> {
                try {
                    HashMap<String, String> parameters = getParameters(he.getRequestBody());
                    String fromNumber = parameters.get("from");
                    String toNumber = parameters.get("to");

                    String ncco = getConnectNcco(fromNumber, toNumber, systemCatalog).toString();

                    he.sendResponseHeaders(200, ncco.length());
                    he.getResponseHeaders().add("Content-Type", "application/json");
                    OutputStream os = he.getResponseBody();
                    os.write(ncco.getBytes());
                    os.close();
                } catch (Exception ex) {
                    log.error("Could not handle call", ex);
                }
            }, 0, 0);
        }
    }

    private class EventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {


            systemCatalog.getExecutionContext().getReactor().scheduleTask(() -> {
                try {
                    HashMap<String, String> parameters = getParameters(he.getRequestBody());
                    log.debug(parameters.toString());

                    setPhoneNumberStatus(parameters.get("status").toUpperCase(), parameters.get("to"), parameters.get("from"));

                } catch (Exception ex) {
                    log.error("Could not handle call", ex);
                }
            }, 0, 0);
        }
    }

    private HashMap<String, String> getParameters(InputStream request){
        try {
            InputStreamReader isr = new InputStreamReader(request, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();

            return new Gson().fromJson(query, HashMap.class);
        }catch (Exception ex) {
            log.error("Could not parse the query string", ex);
            throw new RuntimeException(ex);
        }
    }

    private void setPhoneNumberStatus(String status, String toNumber, String fromNumber){
        KeyedTable phoneNumberTable = (KeyedTable)systemCatalog.getOperator(TableNames.PHONE_NUMBER_TABLE_NAME);
        IRowSequence rows = phoneNumberTable.getOutput().getAllRows();

        while (rows.moveNext()) {
            String userPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "userPhoneNumber", rows.getRowId());
            String virtualPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumber", rows.getRowId());

            if (userPhoneNumber.equals(toNumber.trim()) || userPhoneNumber.equals(fromNumber.trim())) {
                phoneNumberTable.updateRow(new TableKey(virtualPhoneNumber), row -> {
                    row.setString("status", status);

                    if(status.equals(PhoneNumberStatuses.COMPLETED.name())){
                        row.setString("orderId", "");
                        row.setString("userPhoneNumber", "");
                    }
                });
            }
        }
    }

    private HashMap getProxyRoute(String fromNumber, String toNumber, ICatalog systemCatalog) {
        KeyedTable phoneNumberTable = (KeyedTable)systemCatalog.getOperator(TableNames.PHONE_NUMBER_TABLE_NAME);
        IRowSequence rows = phoneNumberTable.getOutput().getAllRows();
        HashMap proxyRoute = new HashMap();

        while (rows.moveNext()) {
            String userPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "userPhoneNumber", rows.getRowId());
            String virtualPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumber", rows.getRowId());

            if (userPhoneNumber.equals(fromNumber)) {
                proxyRoute.put("from", virtualPhoneNumber);
            }

            if (virtualPhoneNumber.equals(toNumber)) {
                proxyRoute.put("to", userPhoneNumber);
            }

            proxyRoute.put("rowId", rows.getRowId());
        }

        log.debug(String.format("Creating Nexmo proxy call from %s proxying virtual number %s to real number %s", fromNumber, toNumber, proxyRoute.get("to")));

        return proxyRoute;
    }

    private JsonArray getConnectNcco(String fromNumber, String toNumber, ICatalog systemCatalog) {
        HashMap<String, String> proxyRoute = getProxyRoute(fromNumber, toNumber, systemCatalog);
        JsonObject ncco = new JsonObject();

        //number is assigned return the proxy connect ncco
        if(proxyRoute.containsKey("from") && proxyRoute.containsKey("to")) {
            JsonObject endpoint = new JsonObject();
            endpoint.add("type", new JsonPrimitive("phone"));
            endpoint.add("number", new JsonPrimitive(proxyRoute.get("to")));
            JsonArray endpointArray = new JsonArray();
            endpointArray.add(endpoint);

            ncco.add("action", new JsonPrimitive("connect"));
            ncco.add("timeout", new JsonPrimitive("5"));
            ncco.add("limit", new JsonPrimitive("300"));
            ncco.add("from", new JsonPrimitive(proxyRoute.get("from")));
            ncco.add("endpoint", endpointArray);
        }else{ //number not assigned tell the customer
            ncco.add("action", new JsonPrimitive("talk"));
            ncco.add("voiceName", new JsonPrimitive("Amy"));
            ncco.add("text", new JsonPrimitive("Welcome to shotgun, please use the shotgun App to call your driver or customer"));
        }

        JsonArray nccoArray = new JsonArray();
        nccoArray.add(ncco);
        return nccoArray;
    }
}
