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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.entrypoints.SkinbaronCrawler.checkIfExists;
import static com.company.entrypoints.SkinbaronCrawler.getBalance;
import static com.company.entrypoints.SteamCrawler.getSteamPriceForGivenName;
import static java.lang.Math.min;

public class Bot {

    private static Double max_price;
    private static Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws Exception {

        setUpClass();

        Scanner sc = new Scanner(System.in);    //System.in is a standard input stream
        logger.info("Buy items (true/false)?");
        boolean buy_item = sc.nextBoolean();

        double balance = getBalance();

        logger.info("Enter max price: ");
        max_price = min(sc.nextDouble(), balance);

        while (true) {

            logger.info("Bot is started...");

            JSONArray array = getItemsToBuy();

            for (Object o: array) {
                if (o instanceof JSONObject) {
                    System.out.println(o);

                    boolean steam_price_is_new = ((JSONObject) o).getBoolean("steam_price_is_new");
                    String skinbaron_ids = ((JSONObject) o).getString("skinbaron_ids");
                    String name = ((JSONObject) o).getString("name");
                    double price = ((JSONObject) o).getDouble("steam_price");
                    double steam_price = ((JSONObject) o).getDouble("steam_price");

                    if (price > max_price) {
                        continue;
                    }

                    if (!steam_price_is_new) {
                        double recent_price = getSteamPriceForGivenName(name);
                        if (recent_price < steam_price) {
                            logger.info("steam price is now lower for item " + name + ".");
                            continue;
                        }
                    }

                    for (String id : skinbaron_ids.split(",")) {
                        logger.info(name + " " + id + " " + price);
                        try {
                            if (buy_item) {
                                buyItem(id, price, steam_price);
                            } else {
                                boolean exists = checkIfExists(name, price);
                                if (!exists) {
                                    deleteNonExistingSkinbaronItems(name, price);
                                }
                            }
                        } catch (Exception e) {
                            logger.info("Item isn't there anymore.");
                        }
                    }
                }
            }
            logger.info("No Items found!.");
        }
    }

    public static void buyItem(String itemId, Double price, double steamPrice) throws Exception {

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"total\":" + price + ",\"saleids\":[\"" + itemId + "\"]}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/BuyItems");
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

        JSONObject resultJson;
        try {
            resultJson = (JSONObject) new JSONTokener(result).nextValue();
        } catch (ClassCastException exp) {
            logger.error(result);
            throw new ClassCastException();
        }

        if (resultJson.has("generalErrors")) {
            String error = resultJson.get("generalErrors").toString();
            logger.error(error);
            if ("[\"some offer(s) are already sold\"]".equals(result) || "[\"count mismatch - maybe some offers have been sold or canceled or you provided wrong saleids\"]".equals(error) || "[\"total mismatch - maybe price changed or offers were sold\"]".equals(error)) {
                requestDeleteSkinbaronId(itemId);
            }
            return;
        }

        JSONObject o = (JSONObject) resultJson.getJSONArray("items").get(0);
        String name = o.getString("name");
        price = o.getDouble("price");

        requestDeleteSkinbaronId(itemId);
        logger.info("Item \"" + name + "\" was bought for " + price + ". Steam: " + steamPrice);
    }

    public static void requestDeleteSkinbaronId(String id) {
        String url = "http://localhost:8080/api/v1/DeleteSkinbaronId";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("id", id);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }

    public static void deleteNonExistingSkinbaronItems(String ItemName, double price) {
        String url = "http://localhost:8080/api/v1/DeleteNonExistingSkinbaronItems";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("ItemName", ItemName);
        JsonObject.put("price", price);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }

    public static JSONArray getItemsToBuy() {

        String url = "http://localhost:8080/api/v1/GetItemsToBuy";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        ResponseEntity<String> responseEntityStr = restTemplate.getForEntity( url,String.class);

        return new JSONArray((responseEntityStr.getBody()));
    }
}
