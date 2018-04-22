package com.shotgun.viewserver;

import com.shotgun.viewserver.user.User;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.viewserver.Constants;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ControllerUtils{

    private static final Logger logger = LoggerFactory.getLogger(ControllerUtils.class);

    public static HashMap<String,Object> mapDefault(String json){
        return io.viewserver.controller.ControllerUtils.mapDefault(json);
    }

    public static String toString(Object ser){
        return io.viewserver.controller.ControllerUtils.toString(ser);
    }

    public static String execute(String method, String targetURL, HashMap<String, String> parameters) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for(HashMap.Entry<String, String> e : parameters.entrySet()){
            if(sb.length() > 0){
                sb.append('&');
            }
            sb.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), "UTF-8"));
        }

        return execute(method, targetURL, sb.toString());
    }

    public static String execute(String method, String targetURL, String urlParameters) {
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Length",Integer.toString(urlParameters.getBytes().length));
        headers.put("Content-Language", "en-US");
        return execute(method, targetURL, urlParameters,headers);
    }

    public static String postToURL(String url, String message, HttpClient httpClient,  Map<String,String> requestHeaders) throws IOException, RuntimeException {
        HttpPost postRequest = new HttpPost(url);
        logger.info("Making request to: {} with parameters {}",url,message);
        StringEntity input = new StringEntity(message);
        input.setContentType("application/json");
        postRequest.setEntity(input);
        for(Map.Entry<String,String> entry :  requestHeaders.entrySet()){
            postRequest.setHeader(entry.getKey(), entry.getValue());
        }

        HttpResponse response = httpClient.execute(postRequest);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatusLine().getStatusCode());
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));

        String output;
        StringBuffer totalOutput = new StringBuffer();
        System.out.println("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            System.out.println(output);
            totalOutput.append(output);
        }
        return totalOutput.toString();
    }

    public static String execute(String method, String targetURL, String urlParameters, Map<String,String> requestHeaders) {
        HttpURLConnection connection = null;
        URL url = null;
        try {
            //Create connection

            boolean isGet = method.equals("GET");
            url = new URL(targetURL + (isGet ? "?" + urlParameters : ""));
            logger.info("Making request to: {}",url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            for(Map.Entry<String,String> entry :  requestHeaders.entrySet()){
                connection.setRequestProperty(entry.getKey(),entry.getValue());
            }
            connection.setConnectTimeout(10000);
            connection.setUseCaches(false);
            connection.setDoOutput(true);


            //Send request
            if(!isGet){
                connection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
            }


            InputStream errorStream = connection.getErrorStream();
            if(connection.getResponseCode() == 401){
                throw new RuntimeException("Authentication issue. Api key broken or you have run out of requests");
            }
            if(connection.getResponseCode() == 400){
                throw new RuntimeException("Bad request");
            }
            else if(connection.getResponseCode() != 200){
                throw new RuntimeException("Problem executing request. Response code is " + connection.getResponseCode() + " errors are " + getString(errorStream));
            }
            InputStream is = connection.getInputStream();
            String string = getString(is);
            logger.info("Response to request \"{}\" is \"{}\"",url,string);
            return string;
        } catch (Exception e) {
            logger.error(String.format("Problem making request to: %s",url),e);
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String urlToString(URL url) {
        try{
            InputStream is = new FileInputStream(new File(url.getFile()));
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine(); StringBuilder sb = new StringBuilder(); while(line != null){ sb.append(line).append("\n"); line = buf.readLine(); }
            return sb.toString();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static String getString(InputStream is) throws IOException {
        if(is == null){
            return "";
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
        }
        rd.close();
        return response.toString();
    }

    public static String getUserId() {
        String driverId = (String) ControllerContext.get("userId");
        if(driverId == null){
            throw new RuntimeException("Cannot find user id in controller context. Either you aren't logged in or you're doing this on a strange thread");
        }
        return driverId;
    }


    public static  User getUser() {
        User user = (User) ControllerContext.get("user");
        if (user == null) {
            throw new RuntimeException("User must be logged in to get current payment cards");
        }
        return user;
    }

    public static ITable getTable(String tableName){
        IOperator table = ControllerContext.Current().getPeerSession().getSystemCatalog().getOperatorByPath(tableName);
        if(table == null){
            throw new RuntimeException("Table " + tableName + " does not exist");
        }
        if (!(table instanceof ITable)) {
            throw new RuntimeException("Operator '" + tableName + "' is not a table");
        }

        return (ITable)table;
    }

    public static IOperator getOperator(String tableName){
        IOperator operator = ControllerContext.Current().getPeerSession().getSystemCatalog().getOperatorByPath(tableName);
        return operator;
    }

    public static KeyedTable getKeyedTable(String tableName){
        IOperator table = ControllerContext.Current().getPeerSession().getSystemCatalog().getOperatorByPath(tableName);
        if (!(table instanceof KeyedTable)) {
            throw new RuntimeException("Operator '" + tableName + "' is not a keyed table");
        }

        return (KeyedTable)table;
    }

    public static Throwable Unwrap(Exception e){
        if(e instanceof InvocationTargetException){
            return ((InvocationTargetException)e).getTargetException();
        }
        return e;
    }

    public static Object getColumnValue(ITable table, String column, int row){
        IOutput output = table.getOutput();
        Schema schema = output.getSchema();
        ColumnHolder col = schema.getColumnHolder(column);
        if(col == null){
            throw new RuntimeException("Unable to find column named '" + column + "' in table " + table.getName());
        }
        return ColumnHolderUtils.getValue(col, row);
    }

    public static Object getOperatorColumnValue(IOperator table, String column, int row){
        IOutput output = table.getOutput(Constants.OUT);
        Schema schema = output.getSchema();
        ColumnHolder col = schema.getColumnHolder(column);
        if(col == null){
            throw new RuntimeException("Unable to find column named '" + column + "' in table " + table.getName());
        }
        return ColumnHolderUtils.getValue(col, row);
    }

    public static Object getColumnValue(KeyedTable table, String column, String key){
       return getColumnValue(table, column, table.getRow(new TableKey(key)));
    }

    public static String generateGuid(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String encryptPassword(String plainText){
        // Create instance
        Argon2 argon2 = Argon2Factory.create();
        char[] charStr = plainText.toCharArray();

        try {
            int N = 65536;
            int r = 2;
            int p = 1;
            // Hash password
            return argon2.hash(r, N, p, charStr);
        } finally {
            argon2.wipeArray(charStr);
        }
    }

    public static boolean validatePassword(String plainText, String hashedText) {
        // Create instance
        Argon2 argon2 = Argon2Factory.create();
        char[] charStr = plainText.toCharArray();

        try {
            int N = 65536;
            int r = 2;
            int p = 1;

            return argon2.verify(hashedText, charStr);
        } finally {
            argon2.wipeArray(charStr);
        }
    }
}
