package com.shotgun.viewserver.user;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.UserNotificationContract;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.PhoneNumberStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.PhoneNumberDataSource;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


@Controller(name = "nexmoController")
public class NexmoController implements INexmoController, UserNotificationContract {
    private static final Logger log = LoggerFactory.getLogger(NexmoController.class);
    private final int httpPort;
    private ICatalog systemCatalog;
    private NexmoControllerKey nexmoControllerKey;
    private final IDatabaseUpdater iDatabaseUpdater;
    private String NUMBER_INSIGHT_URI = "https://api.nexmo.com/ni/basic/json";
    private IMessagingController messagingController;
    private ConcurrentHashMap<String,String> resolvedCache;

    public NexmoController(int httpPort, ICatalog systemCatalog, NexmoControllerKey nexmoControllerKey, IDatabaseUpdater iDatabaseUpdater, IMessagingController messagingController) {
        this.httpPort = httpPort;
        this.systemCatalog = systemCatalog;
        this.resolvedCache = new ConcurrentHashMap<>();
        this.nexmoControllerKey = nexmoControllerKey;
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.messagingController = messagingController;
        this.createHttpServer(httpPort);
    }

    @Override
    @ControllerAction(path = "getInternationalFormatNumber", isSynchronous = false)
    public String getInternationalFormatNumber(String phoneNumber) {
        if (phoneNumber == null || "".equals(phoneNumber)) {
            throw new RuntimeException("Phone number cannot be null");
        }
        try {
            if(resolvedCache.containsKey(phoneNumber)){
                return resolvedCache.get(phoneNumber);
            }

            HashMap<String, String> params = new HashMap<>();
            params.put("api_key", nexmoControllerKey.getKey());
            params.put("api_secret", nexmoControllerKey.getSecret());
            params.put("number", phoneNumber);
            params.put("country", "GB");

            String response = ControllerUtils.execute("POST", NUMBER_INSIGHT_URI, params);
            HashMap<String, Object> map = ControllerUtils.mapDefault(response);
            if(map.get("status") != null && !map.get("status").equals(0)){
                throw new RuntimeException("Problem with nexmo request status:" + map.get("status") + " - message:" + map.get("status_message"));
            }
            return (String) map.get("international_format_number");
        } catch (Exception ex) {
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

    @Override
    public KeyedTable getUserTable() {
        return (KeyedTable) systemCatalog.getOperatorByPath(TableNames.USER_TABLE_NAME);
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public IMessagingController getMessagingController() {
        return this.messagingController;
    }

    private class CallHandler implements HttpHandler {
        private ICatalog systemCatalog;

        public CallHandler(ICatalog systemCatalog) {
            this.systemCatalog = systemCatalog;
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
            systemCatalog.getExecutionContext().getReactor().scheduleTask(() -> {
                try {
                    HashMap<String, String> parameters = getParameters(he.getRequestBody());
                    String fromNumber = parameters.get("from");
                    String toNumber = parameters.get("to");

                    log.info("Call handler params - " + parameters.toString());
                    String ncco = getConnectNcco(fromNumber, toNumber, systemCatalog).toString();

                    he.sendResponseHeaders(200, ncco.length());
                    he.getResponseHeaders().add("Content-Type", "application/json");
                    OutputStream os = he.getResponseBody();
                    os.write(ncco.getBytes());
                    os.close();
                } catch (Exception ex) {
                    log.error("Could not handle Nexmo call", ex);
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
                    log.info("Event handler params - " + parameters.toString());
                    setPhoneNumberStatus(parameters.get("status").toUpperCase(), parameters.get("to"), parameters.get("from"), parameters.get("duration"));
                } catch (Exception ex) {
                    log.error("Could not handle Nexmo event", ex);
                }
            }, 0, 0);
        }
    }

    private HashMap<String, String> getParameters(InputStream request) {
        try {
            InputStreamReader isr = new InputStreamReader(request, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();

            return new Gson().fromJson(query, HashMap.class);
        } catch (Exception ex) {
            log.error("Could not parse the query string", ex);
            throw new RuntimeException(ex);
        }
    }

    private void setPhoneNumberStatus(String status, String fromNumber, String toNumber, String callDuration) {
        KeyedTable phoneNumberTable = (KeyedTable) systemCatalog.getOperatorByPath(TableNames.PHONE_NUMBER_TABLE_NAME);
        IRowSequence rows = phoneNumberTable.getOutput().getAllRows();
        String toNumberTrim = toNumber.trim();
        String fromNumberTrim = fromNumber.trim();
        log.info(String.format("Attempting to find a phone number record with userPhoneNumber %s and virtual numnber %s", toNumberTrim, fromNumberTrim));


        String fromUserId = null;
        String toUserId = null;

        while (rows.moveNext()) {
            String userPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "userPhoneNumber", rows.getRowId());
            String virtualPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumber", rows.getRowId());

            log.info(String.format("Comparing %s==%s || %s==%s",userPhoneNumber,toNumberTrim,virtualPhoneNumber,fromNumberTrim));

            if (virtualPhoneNumber.equals(fromNumberTrim)) {

                fromUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "fromUserId", rows.getRowId());
                toUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "toUserId", rows.getRowId());
                Integer version = (Integer) ControllerUtils.getColumnValue(phoneNumberTable, "version", rows.getRowId());

                log.debug(String.format("found record with userPhoneNumber %s and virtual number %s and version is %s", userPhoneNumber, virtualPhoneNumber, version));
                Record phoneNumberRecord = new Record();
                phoneNumberRecord.addValue("phoneNumber", virtualPhoneNumber);
                phoneNumberRecord.addValue("phoneNumberStatus", status);
                phoneNumberRecord.addValue("version", version);

                if (status.equals(PhoneNumberStatuses.COMPLETED.name()) ||
                    status.equals(PhoneNumberStatuses.REJECTED.name()) ||
                    status.equals(PhoneNumberStatuses.FAILED.name()) ||
                    status.equals(PhoneNumberStatuses.TIMEOUT.name()) ||
                    status.equals(PhoneNumberStatuses.BUSY.name()) ||
                    status.equals(PhoneNumberStatuses.CANCELLED.name())) {

                    phoneNumberRecord.addValue("fromUserId", "");
                    phoneNumberRecord.addValue("toUserId", "");
                    phoneNumberRecord.addValue("userPhoneNumber", "");
                    if(fromUserId != null && toUserId != null) {
                        releaseOtherNumbersFromCallSession(fromUserId, toUserId, status);
                    }
                }

                iDatabaseUpdater.addOrUpdateRow(TableNames.PHONE_NUMBER_TABLE_NAME, PhoneNumberDataSource.getDataSource().getSchema(), phoneNumberRecord).subscribe();
            }
        }




        if(fromUserId != null && toUserId != null) {
            log.info(String.format("Call from %s to %s has status %s", fromUserId, toUserId, status));
            if (status.equals(PhoneNumberStatuses.REJECTED.name()) ||
                    status.equals(PhoneNumberStatuses.FAILED.name()) ||
                    status.equals(PhoneNumberStatuses.TIMEOUT.name()) ||
                    status.equals(PhoneNumberStatuses.BUSY.name()) ||
                    status.equals(PhoneNumberStatuses.COMPLETED.name()) ||
                    status.equals(PhoneNumberStatuses.CANCELLED.name())) {
                log.info(String.format("Call from %s to %s failed so notifying user", fromUserId, toUserId, status));
                notifyCallReceived(fromUserId, toUserId);
            }
        }
    }

    private void releaseOtherNumbersFromCallSession(String fromUserId, String toUserId, String status) {
        KeyedTable phoneNumberTable = (KeyedTable) systemCatalog.getOperatorByPath(TableNames.PHONE_NUMBER_TABLE_NAME);
        IRowSequence rows = phoneNumberTable.getOutput().getAllRows();

        while (rows.moveNext()) {
            String tableFromUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "fromUserId", rows.getRowId());
            String tableToUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "toUserId", rows.getRowId());
            String virtualPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumber", rows.getRowId());
            String userPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "userPhoneNumber", rows.getRowId());

            log.info(String.format("Comparing %s==%s || %s==%s",fromUserId,tableFromUserId,toUserId,tableToUserId));

            if (fromUserId.equals(tableFromUserId) && toUserId.equals(tableToUserId)) {

                fromUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "fromUserId", rows.getRowId());
                toUserId = (String) ControllerUtils.getColumnValue(phoneNumberTable, "toUserId", rows.getRowId());
                Object version = ControllerUtils.getColumnValue(phoneNumberTable, "version", virtualPhoneNumber);
                log.debug(String.format("found record with userPhoneNumber %s and virtual number %s and version is %s", userPhoneNumber, virtualPhoneNumber, version));
                Record phoneNumberRecord = new Record();
                phoneNumberRecord.addValue("phoneNumber", virtualPhoneNumber);
                phoneNumberRecord.addValue("phoneNumberStatus", status);
                phoneNumberRecord.addValue("fromUserId", "");
                phoneNumberRecord.addValue("toUserId", "");
                phoneNumberRecord.addValue("version", version);
                phoneNumberRecord.addValue("userPhoneNumber", "");

                iDatabaseUpdater.addOrUpdateRow(TableNames.PHONE_NUMBER_TABLE_NAME, PhoneNumberDataSource.getDataSource().getSchema(), phoneNumberRecord).subscribe();
            }
        }
    }

    private HashMap getProxyRoute(String fromNumber, String toNumber, ICatalog systemCatalog) {
        KeyedTable phoneNumberTable = (KeyedTable) systemCatalog.getOperatorByPath(TableNames.PHONE_NUMBER_TABLE_NAME);
        IRowSequence rows = phoneNumberTable.getOutput().getAllRows();
        HashMap proxyRoute = new HashMap();

        log.info(String.format("Examining contents of table to create proxy call from %s to %s", fromNumber, toNumber));

        while (rows.moveNext()) {
            String userPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "userPhoneNumber", rows.getRowId());
            String virtualPhoneNumber = (String) ControllerUtils.getColumnValue(phoneNumberTable, "phoneNumber", rows.getRowId());
            if(userPhoneNumber == null){
                continue;
            }
            String internationalFormatNumber = getInternationalFormatNumber(userPhoneNumber);
            log.info("from comparing {}=={}",internationalFormatNumber, fromNumber);
            if (internationalFormatNumber.equals(fromNumber)) {
                log.info("found from comparing {}=={}",internationalFormatNumber, fromNumber);
                proxyRoute.put("from", virtualPhoneNumber);
            }
            log.info("to comparing {}=={}",virtualPhoneNumber, toNumber);
            if (virtualPhoneNumber.equals(toNumber)) {
                log.info("found to comparing {}=={}",virtualPhoneNumber, toNumber);
                proxyRoute.put("to", internationalFormatNumber);
            }

            proxyRoute.put("rowId", rows.getRowId());
        }

        log.info(String.format("proxy route is from:%s to%s", proxyRoute.get("from"), proxyRoute.get("to")));
        log.info(String.format("Creating Nexmo proxy call from %s proxying virtual number %s to real number %s", fromNumber, toNumber, proxyRoute.get("to")));

        return proxyRoute;
    }

    private JsonArray getConnectNcco(String fromNumber, String toNumber, ICatalog systemCatalog) {
        HashMap<String, String> proxyRoute = getProxyRoute(fromNumber, toNumber, systemCatalog);
        JsonObject ncco = new JsonObject();

        //number is assigned return the proxy connect ncco
        if (proxyRoute.containsKey("from") && proxyRoute.containsKey("to")) {
            JsonObject endpoint = new JsonObject();
            endpoint.add("type", new JsonPrimitive("phone"));
            endpoint.add("number", new JsonPrimitive(proxyRoute.get("to")));
            JsonArray endpointArray = new JsonArray();
            endpointArray.add(endpoint);

            ncco.add("action", new JsonPrimitive("connect"));
            ncco.add("timeout", new JsonPrimitive("30"));
            ncco.add("limit", new JsonPrimitive("300"));
            ncco.add("from", new JsonPrimitive(proxyRoute.get("from")));
            ncco.add("endpoint", endpointArray);
        } else { //number not assigned tell the customer
            ncco.add("action", new JsonPrimitive("talk"));
            ncco.add("voiceName", new JsonPrimitive("Amy"));
            ncco.add("text", new JsonPrimitive("Welcome to Shotgun. We're unable to connect your call at this moment. Please call later"));
        }

        JsonArray nccoArray = new JsonArray();
        nccoArray.add(ncco);
        return nccoArray;
    }
}
