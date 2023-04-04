package de.yannickm.steambot.entrypoints;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;
import static de.yannickm.steambot.common.PasswordHelper.readPasswordFromFile;
import static de.yannickm.steambot.common.PostgresExecutor.getConnection;
import static de.yannickm.steambot.entrypoints.Bot.buyItem;

public class SkinbaronCrawler {
    private final static Logger logger = LoggerFactory.getLogger(SkinbaronCrawler.class);

    public static void main(String[] args) throws Exception {

        setUpClass(); //disable Logging
        requestCleanUp();
        checkLiveSkinbaron();
        getPriceList();
        getSoldItems();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String id = getLastSkinbaronId();

        //noinspection InfiniteLoopStatement
        while (true) {//infinite times
            while (1==1 || 1==0) { //as long as there are inserts
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

    static void getPriceList() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        logger.info("Skinbaron API /GetPriceList has been called.");
        String jsonInputString = "{\"appId\": 730,\"apikey\": \"" + secret + "\"}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetPriceList");
        httpPost.setHeader("Content-Type", "application/json");
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

        System.out.println(result);

        requestInsertPriceList(resultJson);

    }

    static void requestInsertPriceList(JSONObject o) {
        String url = "http://localhost:8080/api/v1/InsertPriceList";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(o.toString(), headers);

        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(url, request, String.class);

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


    //TODO batch + to back
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
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appid\": 730,\"min\":0.05,\"items_per_page\": "+items_per_page + (!"".equals(after_saleid) ? ",\"after_saleid\":\"" + after_saleid + "\"" : "") + "}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/Search");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
        httpPost.setEntity(entity);
        HttpResponse response = client.execute(httpPost);

        if (response.getStatusLine().getStatusCode()!=200) {
            throw new Exception(response.getStatusLine().toString());
        }

        String result = EntityUtils.toString(response.getEntity());

        try {
            JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();
            JSONArray resultArray = ((JSONArray) resultJson.get("sales"));

            String id = null;
            Double wear;
            boolean alreadyExisting;

                for (Object o : resultArray) {
                    if (o instanceof JSONObject) {
                        //set id
                        id = ((JSONObject) o).getString("id");

                        //TODO Batch
                        alreadyExisting = requestInsertSkinbaronItem((JSONObject) o);

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

    static boolean requestInsertSkinbaronItem(JSONObject o) {
        String url = "http://localhost:8080/api/v1/AddSkinbaronItem";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String id = o.getString("id");

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(o.toString(), headers);

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

        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appid\": 730,\"search_item\"=\"" + name + "\",\"max\"=" + price + ",\"items_per_page\": 50}";

        System.out.println(jsonInputString);
        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/Search");
        httpPost.setHeader("Content-Type", "application/json");
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
     * only works fully, if insert is not interrupted/partially
     */
    public static void getSoldItems() throws Exception {
        logger.info("Getting Sold Items...");
        Connection conn = getConnection();
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String queryId = "";

        while (true) {

            logger.info("Skinbaron API getSales has been called.");
            String jsonInputString = "{\"apikey\": \"" + secret + "\",\"type\":4,\"appid\": 730,\"items_per_page\": 50" + (queryId == null ? "" : ",\"after_saleid\":\"" + queryId + "\"") + ",sort_order:2}";

            HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetSales");
            httpPost.setHeader("Content-Type", "application/json");
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

                itemId = jObject.get("id").toString();

                boolean was_inserted = requestInsertSoldSkinbaronItem(jArray.getJSONObject(i));

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

    public static boolean requestInsertSoldSkinbaronItem(JSONObject payload) {
        String url = "http://localhost:8080/api/v1/InsertSoldSkinbaronItem";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(payload.toString(), headers);

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
        httpPost.setHeader("Content-Type", "application/json");
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
