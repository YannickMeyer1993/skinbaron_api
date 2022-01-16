package com.company.entrypoints;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.*;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.getConnection;
import static com.company.entrypoints.Bot.buyItem;

//TODO Buy Orders
//TODO Differentiate between tradelock and no tradelock
public class SkinbaronCrawler {
    private final static Logger logger = LoggerFactory.getLogger(SkinbaronCrawler.class);

    public static void main(String[] args) throws Exception {

        setUpClass(); //disable Logging

        requestCleanUp();

        checkLiveSkinbaron();

        getSoldItems();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String id = getLastSkinbaronId();

        //noinspection InfiniteLoopStatement
        while (true) { //infinite times
            while (true) { //as long as there are inserts
                try {
                    String[] output = Search(secret, id,50);
                    if (Integer.parseInt(output[0]) == 0) {
                        break;
                    }
                    id = output[1];

                } catch (org.apache.http.conn.HttpHostConnectException e)
                {
                    Thread.sleep(2000);
                }
                logger.info("Not finished yet. Last id=" + id + " (" + (new Timestamp(System.currentTimeMillis())) + ")");
            }
            id = "";
            //noinspection BusyWait
            Thread.sleep((long) 5 * 1000);
            logger.info("------------------------------------------------------------------");
            logger.info("New Search started.");
            logger.info("------------------------------------------------------------------");
        }
    }

    private static void requestCleanUp() {
        logger.info("Skinbaron Items Clean Up...");
        String url = "http://localhost:8080/api/v1/cleanup";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * "tradelocked": true is useless
     * price >= 0.05
     * @param secret api secret
     * @param after_saleid last id
     * @return [0] amount_inserts [1] last id
     */
    public static String[] Search(String secret, String after_saleid, int items_per_page) throws Exception {
        int amountInserts = 0;

        logger.info("Skinbaron API Search has been called.");
        Thread.sleep(1000);
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appid\": 730,\"min\":0.05,\"items_per_page\": "+items_per_page + (!"".equals(after_saleid) ? ",\"after_saleid\":\"" + after_saleid + "\"" : "") + "}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/Search");
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
        httpPost.setEntity(entity);
        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());

        try {
            JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();
            JSONArray resultArray = ((JSONArray) resultJson.get("sales"));

            String id = null;
            Double wear;
            boolean alreadyExisting;

                for (Object o : resultArray) {
                    if (o instanceof JSONObject) {
                        id = ((JSONObject) o).getString("id");
                        double price_euro = ((JSONObject) o).getDouble("price");
                        String name = ((JSONObject) o).getString("market_name");
                        String stickers = ((JSONObject) o).getString("stickers");
                        String inspect ="";
                        if (((JSONObject) o).has("inspect")) {
                            inspect = ((JSONObject) o).getString("inspect");
                        }
                        String sbinspect="";
                        if (((JSONObject) o).has("sbinspect")) {
                            sbinspect = ((JSONObject) o).getString("sbinspect");
                        }
                        try {
                            wear =  ((JSONObject) o).getDouble("wear");
                        } catch (JSONException je) {
                            wear = null;
                        }
                        alreadyExisting = requestInsertSkinbaronItem(id,name,price_euro,stickers,wear,inspect,sbinspect);

                        if (!alreadyExisting) {
                            amountInserts++;
                        }
                    }
                }

                if (amountInserts != 0) {
                    logger.info(amountInserts + " items were inserted!");
                }

            String[] returnObject = new String[2];
            returnObject[0] = "" + amountInserts;
            returnObject[1] = id;
            return returnObject;
        } catch (ClassCastException e) {
            logger.info(result);
            String[] returnObject = new String[2];
            returnObject[0] = "" + 50;
            returnObject[1] = after_saleid;
            return returnObject;
        }
    }

    static boolean requestInsertSkinbaronItem(@NotNull String id, String name, double price_euro, String stickers, Double wear,String inspect,String sbinspect) {
        String url = "http://localhost:8080/api/v1/AddSkinbaronItem";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("id",id);
        JsonObject.put("price",price_euro);
        JsonObject.put("name",name);
        JsonObject.put("sticker",stickers);
        JsonObject.put("wear",wear);
        JsonObject.put("inspect",inspect);
        JsonObject.put("sbinspect",sbinspect);



        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(url, request, String.class);

        //exists already if not id as response
        return (!id.equals(responseEntityStr.getBody()));
    }

    public static void checkLiveSkinbaron() {

        logger.info("Checking that Skinbaron is live...");
        int wait_counter = 0;

        while (true) {
            try {

                System.out.println("Waiting for " + Math.pow(2, wait_counter) + " seconds");
                Thread.sleep((long) (Math.pow(2, wait_counter) * 1000));
                buyItem("a52eca5d-6beb-4cf8-8173-a1eae90cbb14", 0.09,0d);
                break;
            } catch (PSQLException e) {
                System.out.println("Postgres is down.");
                break;
            } catch (InterruptedException e) {
                System.out.println("Program got interrupted.");
                break;
            }
            catch (Exception e) {
                System.out.println("Skibaron APIs are still down.");
                wait_counter++;

            }

        }
    }

    public static Boolean checkIfExists(String name, double price) throws Exception {

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        logger.info("Skinbaron API Search has been called.");
        Thread.sleep(1000);
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appid\": 730,\"search_item\"=\"" + name + "\",\"max\"=" + price + ",\"items_per_page\": 50}";

        System.out.println(jsonInputString);
        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/Search");
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
        httpPost.setEntity(entity);
        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());
        System.out.println(result);
        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();
        JSONArray resultArray = ((JSONArray) resultJson.get("sales"));

        return resultArray.length() != 0;
    }

    /**
     * type in API: 4
     */
    public static void getSoldItems() throws Exception {
        Connection conn = getConnection();
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String queryId = "";

        while (true) {

            logger.info("Skinbaron API getSales has been called.");
            String jsonInputString = "{\"apikey\": \"" + secret + "\",\"type\":4,\"appid\": 730,\"items_per_page\": 50" + (queryId == null ? "" : ",\"after_saleid\":\"" + queryId + "\"") + ",sort_order:2}";

            HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetSales");
            httpPost.setHeader("Content.Type", "application/json");
            httpPost.setHeader("x-requested-with", "XMLHttpRequest");
            httpPost.setHeader("Accept", "application/json");

            HttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setCookieSpec(CookieSpecs.STANDARD).build())
                    .build();

            HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
            httpPost.setEntity(entity);
            HttpResponse response = client.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());

            JSONObject resultJson = new JSONObject(result);
            JSONArray jArray = new JSONArray(resultJson.get("response").toString());

            String lastSoldId = getLastSoldSkinbaronId();

            String itemId = null;
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);

                String classid = jObject.get("classid").toString();
                String last_updated = jObject.get("last_updated").toString();
                String instanceid = jObject.get("instanceid").toString();
                String list_time = jObject.get("list_time").toString();
                double price = Double.parseDouble(jObject.get("price").toString());
                String assetid = jObject.get("assetid").toString();
                String name = jObject.get("name").toString();
                String txid = jObject.get("txid").toString();
                double commission = Double.parseDouble(jObject.get("commission").toString());
                itemId = jObject.get("id").toString();

                boolean was_inserted = requestInsertSoldSkinbaronItem(itemId,name,price,classid,last_updated,instanceid,list_time,assetid,txid,commission);

                if (lastSoldId.equals(itemId) || !was_inserted) { //if not inserted, then is was already present => end loop
                    return;
                }
            }

            queryId = itemId;
            conn.commit();
            if (jArray.length() < 50) {
                break; //This means that the last 50 items were reached
            }
        }
    }

    public static String getLastSkinbaronId() {

        String url = "http://localhost:8080/api/v1/lastSkinbaronId";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntityStr = restTemplate.getForEntity( url,String.class);

        logger.info("last inserted Skinbaron Id: "+responseEntityStr.getBody());
        return (responseEntityStr.getBody());
    }

    public static String getLastSoldSkinbaronId() {

        String url = "http://localhost:8080/api/v1/lastSoldSkinbaronId";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntityStr = restTemplate.getForEntity( url,String.class);

        return (responseEntityStr.getBody());
    }

    public static boolean requestInsertSoldSkinbaronItem(String itemId, String name, double price, String classid, String last_updated, String instanceid, String list_time, String assetid, String txid, double commission) {
        String url = "http://localhost:8080/api/v1/InsertSoldSkinbaronItem";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("itemId",itemId);
        JsonObject.put("name",name);
        JsonObject.put("price",price);
        JsonObject.put("classid",classid);
        JsonObject.put("last_updated",last_updated);
        JsonObject.put("instanceid",instanceid);
        JsonObject.put("list_time",list_time);
        JsonObject.put("assetid",assetid);
        JsonObject.put("txid",txid);
        JsonObject.put("commission",commission);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        try {
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Double getBalance() throws Exception {

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        logger.info("Skinbaron API GetBalance has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\"}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetBalance");
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
        httpPost.setEntity(entity);
        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

        if (resultJson.has("message")) {
            System.out.println("Result: " + resultJson.get("message"));
            throw new Exception((String) resultJson.get("message"));
        }

        double skinbaronBalance = resultJson.getDouble("balance");

        logger.info("Skinbaron Balance: " + skinbaronBalance + " euro.");
        return skinbaronBalance;
    }
}
