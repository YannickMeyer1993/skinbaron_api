package com.company.entrypoints;

import com.company.old.helper;
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
import java.util.Scanner;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.entrypoints.Bot.buyItem;
import static com.company.old.helper.getConnection;

public class SkinbaronCrawler {
    private final static Logger logger = LoggerFactory.getLogger(SkinbaronCrawler.class);

    public static void main(String[] args) throws Exception {

        setUpClass(); //disable Logging

        requestCleanUp();

        checkLiveSkinbaron();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        Scanner sc = new Scanner(System.in);
        logger.info("Enter last Id: ");
        String id = sc.nextLine();

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

        String secret = helper.readPasswordFromFile("C:/passwords/api_secret.txt");

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
}
