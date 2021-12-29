package com.company.service;

import com.company.SkinbaronAPI;
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

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import static com.company.common.readPasswordFromFile;

public class BuyService {
    private final static Logger LOGGER = Logger.getLogger(SkinbaronAPI.class.getName());

    public static void buyItem(String name,  String itemId, Double price) throws Exception {

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

        try {
            JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

            if (resultJson.has("generalErrors")) {
                System.out.println(resultJson.get("generalErrors").toString());
                if ("[\"some offer(s) are already sold\"]".equals(resultJson.get("generalErrors").toString())
                        || "[\"count mismatch - maybe some offers have been sold or canceled or you provided wrong saleids\"]".equals(resultJson.get("generalErrors").toString())
                ) {
                    LOGGER.info("Item is already gone.");
                }
                return;
            }

            if (!resultJson.has("items")) {
                LOGGER.info("There was no json query 'result' found in:");
                LOGGER.info(resultJson.toString());
                return;
            }

            //success
            LOGGER.info("Item \"" + name + "\" was bought for " + price +".");
        } catch (Exception exp) {
            LOGGER.info("Error while buying item "+name);
            LOGGER.info("Received Message:");
            LOGGER.info(result);
            throw new Exception();
        }
    }
}