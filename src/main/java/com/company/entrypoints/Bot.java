package com.company.entrypoints;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.entrypoints.BuffCrawler.getBuffItemNoExterior;
import static com.company.entrypoints.BuffCrawler.getBuffItemWithExterior;
import static com.company.entrypoints.SkinbaronCrawler.checkIfExists;
import static com.company.entrypoints.SkinbaronCrawler.getBalance;
import static com.company.entrypoints.SteamAPI.getSteamPriceForGivenName;
import static java.lang.Math.min;

public class Bot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws Exception {

        setUpClass();

        Scanner sc = new Scanner(System.in);    //System.in is a standard input stream
        logger.info("Buy items (true/false)?");
        boolean buy_item = sc.nextBoolean();

        double balance = getBalance();

        logger.info("Enter max price: ");
        double max_price = min(sc.nextDouble(), balance);

        while (true) {

            logger.info("Bot is started...");

            JSONArray array = getItemsToBuy();

            logger.info("Amount of different Items: "+array.length());

            for (Object o: array) {
                if (o instanceof JSONObject) {
                    boolean steam_price_is_new = ((JSONObject) o).getBoolean("steam_price_is_new");
                    String skinbaron_ids = ((JSONObject) o).getString("skinbaron_ids");
                    String name = ((JSONObject) o).getString("name");
                    double price = ((JSONObject) o).getDouble("skinbaron_price");
                    double steam_price = ((JSONObject) o).getDouble("steam_price");

                    boolean buff_price_is_new = ((JSONObject) o).getBoolean("buff_price_is_new");
                    double buff_price = ((JSONObject) o).getDouble("buff_price");
                    boolean has_exterior = ((JSONObject) o).getBoolean("has_exterior");
                    int buff_id = ((JSONObject) o).getInt("buff_id");

                    if (buff_id ==0) {
                        logger.error("Buff id is null!");
                        continue;
                    }

                    if (price > max_price) {
                        continue;
                    }

                    if (!steam_price_is_new) {
                        double current_price = getSteamPriceForGivenName(name);
                        if (current_price < steam_price) {
                            logger.info("steam price is now lower for item " + name + " ("+current_price+").");
                            continue;
                        }
                    }

                    if (!buff_price_is_new) {

                        double current_price;
                        logger.info("Checking buff price for id: "+buff_id);
                        if (has_exterior) {
                            current_price = getBuffItemWithExterior(buff_id);
                        } else {
                            current_price = getBuffItemNoExterior(buff_id);
                        }

                        buff_price = current_price;
                    }

                    if (price <= buff_price*0.9) {
                        logger.info("Did not buy item, because buff price is cheaper than Skinbaron price!");
                        continue;
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

    //TODO buy item at most 10x
    public static void buyItem(String id,double price,double steamPrice) throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String url = "https://api.skinbaron.de/BuyItems";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add("Content.Type", "application/json");
        headers.add("x-requested-with", "XMLHttpRequest");
        headers.add("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject JsonObject = new JSONObject();
        JsonObject.put("apikey",secret);
        JsonObject.put("toInventory",true);
        JsonObject.put("total",price);
        JSONArray array = new JSONArray();
        array.put(0,id);
        JsonObject.put("saleids",array);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);
        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(url, request, String.class);
        String result = responseEntityStr.getBody();

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
            if ("[\"some offer(s) are already sold\"]".equals(error) || "[\"count mismatch - maybe some offers have been sold or canceled or you provided wrong saleids\"]".equals(error) || "[\"total mismatch - maybe price changed or offers were sold\"]".equals(error)) {
                requestDeleteSkinbaronId(id);
            }
            return;
        }

        JSONObject o = (JSONObject) resultJson.getJSONArray("items").get(0);
        String name = o.getString("name");
        price = o.getDouble("price");

        requestDeleteSkinbaronId(id);
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
        ResponseEntity<String> responseEntityStr = restTemplate.getForEntity( url,String.class);

        return new JSONArray((responseEntityStr.getBody()));
    }
}
