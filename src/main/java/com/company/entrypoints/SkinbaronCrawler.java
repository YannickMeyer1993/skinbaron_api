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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.getConnection;
import static com.company.entrypoints.Bot.buyItem;

public class SkinbaronCrawler {
    private final static Logger logger = LoggerFactory.getLogger(SkinbaronCrawler.class);

    public static void main(String[] args) throws Exception {

        setUpClass(); //disable Logging

        requestCleanUp();

        checkLiveSkinbaron();

        getSoldItems();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        Scanner sc = new Scanner(System.in);
        logger.info("Enter last Id: ");
        String id = sc.nextLine();
        //TODO get last inserted id

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
        String url = "http://localhost:8080/api/v1/cleanup";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }


    /**
     *
     * @param secret api secret
     * @param after_saleid last id
     * @return [0] amount_inserts [1] last id
     */
    public static String[] Search(String secret, String after_saleid, int items_per_page) throws Exception {
        int amountInserts = 0;

        logger.info("Skinbaron API Search has been called.");
        Thread.sleep(1000);
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appid\": 730,\"items_per_page\": "+items_per_page + (!"".equals(after_saleid) ? ",\"after_saleid\":\"" + after_saleid + "\"" : "") + "}";

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
                        try {
                            wear =  ((JSONObject) o).getDouble("wear");
                        } catch (JSONException je) {
                            wear = null;
                        }
                        alreadyExisting = requestInsertSkinbaronItem(id,name,price_euro,stickers,wear);

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

    static boolean requestInsertSkinbaronItem(@NotNull String id, String name, double price_euro, String stickers, Double wear) {
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

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(url, request, String.class);

        //exists already if not id as response
        return (!id.equals(responseEntityStr.getBody()));
    }

    public static void checkLiveSkinbaron() throws Exception {

        int wait_counter = 3;

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        while (true) {
            try (Connection conn = getConnection()){

                System.out.println("Waiting for " + Math.pow(2, wait_counter) + " seconds");
                Thread.sleep((long) (Math.pow(2, wait_counter) * 1000));
                buyItem( secret, "a52eca5d-6beb-4cf8-8173-a1eae90cbb14", 0.09);
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

    public static Boolean checkIfExists( String secret, String name, double price) throws IOException, InterruptedException {

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

        //TODO delete from dao

        return resultArray.length() != 0;
    }

    //TODO
    public static void getSoldItems() throws Exception {
        Connection conn = getConnection();
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String queryId = "";

        int counter = 0;
        while (true) {

            logger.info("Skinbaron API getSales has been called. (" + counter + ")");
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

            List<String> existing_ids = new ArrayList<>();

            try (Statement stmt2 = conn.createStatement()) {
                ResultSet rs2 = stmt2.executeQuery("select id from steam.skinbaron_sold_items");
                while (rs2.next()) {
                    existing_ids.add(rs2.getString("id"));
                }
            }

            String sqlIinsert = "INSERT INTO steam.skinbaron_sold_items\n" +
                    "(id, name, price,classid,last_updated,instanceid,list_time,assetid,txid,commission)\n" +
                    "VALUES(?, ?, ?,?,?, ?, ?,?,?, ?);";

            String itemId = null;
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);

                String classid = jObject.get("classid").toString();
                String last_updated = jObject.get("last_updated").toString();
                String instanceid = jObject.get("instanceid").toString();
                String list_time = jObject.get("list_time").toString();
                String price = jObject.get("price").toString();
                String assetid = jObject.get("assetid").toString();
                String name = jObject.get("name").toString();
                String txid = jObject.get("txid").toString();
                String commission = jObject.get("commission").toString();
                itemId = jObject.get("id").toString();


                if (existing_ids.contains(itemId)) { //if exists, finish
                    return;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlIinsert, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, itemId);
                    pstmt.setString(2, name);
                    pstmt.setDouble(3, Double.parseDouble(price));
                    pstmt.setString(4,classid);
                    pstmt.setString(5,last_updated);
                    pstmt.setString(6,instanceid);
                    pstmt.setString(7,list_time);
                    pstmt.setString(8,assetid);
                    pstmt.setString(9,txid);
                    pstmt.setDouble(10,Double.parseDouble(commission));
                    //pstmt.setInt(11, counter * 50 + i + 1);

                    pstmt.executeUpdate();
                }
            }

            counter++;
            queryId = itemId;
            conn.commit();
            if (jArray.length() < 50) {
                break; //This means that the last 50 items were reached
            }
        }
    }
}
