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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class ToBeTested {

    private final static Logger logger = LoggerFactory.getLogger(ToBeTested.class);

    public static void getNewestSales30Days(String secret, Connection conn, String itemName) throws Exception {

        String ItemName = "Aufkleber | device | Atlanta 2017";
        logger.info("Skinbaron API GetNewestSales30Days has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appId\": 730}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetNewestSales30Days");
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

        if (resultJson.has("message")) {
            System.out.println("Result: " + resultJson.get("message"));
            throw new Exception((String) resultJson.get("message"));
        }

        //TODO API does not work yet?

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

}
