package com.shotgun.viewserver.user;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.ShotgunTableUpdater;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.PhoneNumberStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller(name = "nexmoController")
public class NexmoController {
    private static final Logger log = LoggerFactory.getLogger(NexmoController.class);
    private final int httpPort;
    private Catalog systemCatalog;
    private final String apiKey;
    private final String apiSecret;
    private final ShotgunTableUpdater shotgunTableUpdater;
    private String NUMBER_INSIGHT_URI = "https://api.nexmo.com/ni/basic/json";

    public NexmoController(int httpPort, Catalog systemCatalog, String apiKey, String apiSecret, ShotgunTableUpdater shotgunTableUpdater) {
        this.httpPort = httpPort;
        this.systemCatalog = systemCatalog;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.shotgunTableUpdater = shotgunTableUpdater;
        this.createHttpServer(httpPort);
    }

    @ControllerAction(path = "getPhoneNumberInfo", isSynchronous = false)
    public HashMap<String, Object> getPhoneNumberInfo(String phoneNumber) {
        if(phoneNumber == null || "".equals(phoneNumber)){
            throw new RuntimeException("Phone number cannot be null");
        }
        try {

            HashMap<String, String> params = new HashMap<>();
            params.put("api_key", apiKey);
            params.put("api_secret", apiSecret);
            params.put("number", phoneNumber);
            params.put("country", "GB");

            String response = ControllerUtils.execute("POST", NUMBER_INSIGHT_URI, params);
            HashMap<String,Object> map = ControllerUtils.mapDefault(response);
            return map;
        }catch (Exception ex){
            log.error(String.format("Problem getting number info from Nexmo for %s", phoneNumber), ex);
            throw new RuntimeException(ex);
        }
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

                    log.debug("Call handler params - " + parameters.toString());
                    String ncco = getConnectNcco(fromNumber, toNumber, systemCatalog).toString();

                    he.sendResponseHeaders(200, ncco.length());
                    he.getResponseHeaders().add("Content-Type", "application/json");
                    OutputStream os = he.getResponseBody();
                    os.write(ncco.getBytes());
                    os.close();
                } catch (Exception ex) {
                    log.error("Could not handle call", ex);
                }
            }, 0, -1);
        }
    }

    private class EventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            systemCatalog.getExecutionContext().getReactor().scheduleTask(() -> {
                try {
                    HashMap<String, String> parameters = getParameters(he.getRequestBody());
                    log.debug("Event handler params - " + parameters.toString());
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
        String toNumberTrim = toNumber.trim();
        String fromNumberTrim = fromNumber.trim();
        log.info(String.format("Attempting to find a user record with userPhoneNumber %s and virtual numnber %s",toNumberTrim,fromNumberTrim));


        while (rows.moveNext()) {
            String userPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "userPhoneNumber", rows.getRowId());
            String virtualPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumber", rows.getRowId());
            log.debug(String.format("found record with userPhoneNumber %s and virtual numnber %s",userPhoneNumber,virtualPhoneNumber));
            if (userPhoneNumber.equals(toNumberTrim) || userPhoneNumber.equals(fromNumberTrim)) {
                Record phoneNumberRecord = new Record().addValue("phoneNumber", virtualPhoneNumber).addValue("status", status);
                if(status.equals(PhoneNumberStatuses.COMPLETED.name())){
                    phoneNumberRecord.addValue("orderId", "");
                    phoneNumberRecord.addValue("userPhoneNumber", "");
                }

                shotgunTableUpdater.addOrUpdateRow((KeyedTable)systemCatalog.getOperator(TableNames.PHONE_NUMBER_TABLE_NAME), "phoneNumber", phoneNumberRecord);
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
