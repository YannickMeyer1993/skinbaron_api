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
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.getConnection;
import static com.company.entrypoints.SkinbaronCrawler.checkIfExists;
import static com.company.entrypoints.SkinbaronCrawler.getBalance;
import static com.company.entrypoints.SteamCrawler.getSteamPriceForGivenName;
import static java.lang.Math.min;

public class Bot {

    private static Double max_price;
    private static Logger logger = LoggerFactory.getLogger(Bot.class);

    //TODO
    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);    //System.in is a standard input stream
        logger.info("Buy items (true/false)?");
        boolean buy_item =sc.nextBoolean();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        double balance = getBalance();

        logger.info("Enter max price: ");
        max_price = min(sc.nextDouble(), balance);

        String query = "select steam_preis_aktuell,skinbaron_preis,steam_preis, name from steam_item_sale.auf_skinbaron_kaufbare_skins where skinbaron_preis<=" + max_price + " order by rati desc";

        //to get id
        String query2 = "select s.name,s.id, s.price from steam_item_sale.skinbaron_market_search_results s where s.name = ? and  s.price <= ?";

        while (true) {
            logger.info("Bot is started...");
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    if (!rs.getBoolean("steam_preis_aktuell")) {
                        double recent_price = getSteamPriceForGivenName(rs.getString("name"));
                        if (recent_price < rs.getDouble("steam_preis")) {
                            logger.info("Steam Preis nicht mehr aktuell für Item " + rs.getString("name") + ".");
                            continue;
                        }
                    }

                    try (PreparedStatement pstmt = conn.prepareStatement(query2, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, rs.getString("name"));
                        pstmt.setDouble(2, rs.getDouble("skinbaron_preis"));

                        try (ResultSet rs2 = pstmt.executeQuery()) {

                            String id = rs2.getString("id");
                            String name = rs2.getString("name");
                            double price = rs2.getDouble("price");
                            double steam_price = rs.getDouble("steam_preis");

                            while (rs2.next()) {
                                logger.info(name + " " + id + " " + price);
                                try {
                                    if (buy_item) {
                                        buyItem(id, price,steam_price);
                                    } else {
                                        boolean exists = checkIfExists(name, price);
                                        if (!exists) {
                                            deleteNonExistingSkinbaronItems(name,price);
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.info("Item isn't there anymore.");
                                }
                            }
                        }
                    }
                }

                logger.info("No Items found!.");
            }
        }
    }

    public static void buyItem( String itemId, Double price, double steamPrice) throws Exception {

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
            if ("[\"some offer(s) are already sold\"]".equals(result) || "[\"count mismatch - maybe some offers have been sold or canceled or you provided wrong saleids\"]".equals(error)) {
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

    public static void deleteNonExistingSkinbaronItems(String ItemName,double price) {
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
}
