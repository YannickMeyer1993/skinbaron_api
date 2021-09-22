package com.company;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import static com.company.common.readPasswordFromFile;

public class SkinbaronAPI {

    public static int resendTradeOffers(String secret) throws Exception {

        System.out.println("Skinbaron API resendTradeOffers has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\"}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/ResendFailedTradeOffers");
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

        Object obj = JSONValue.parse(result);
        JSONObject o = (JSONObject) obj;

        if (o.containsKey("message"))
        {
            System.out.println("Result: "+o.get("message"));
            throw new Exception((String) o.get("message"));
        }

        System.out.println("Result: "+response.getStatusLine().getStatusCode());
        return response.getStatusLine().getStatusCode();
    }

    public static int writeSoldItems(String secret) throws Exception {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        Statement st = conn.createStatement();
        st.execute("TRUNCATE TABLE steam_item_sale.sold_items");
        conn.commit();
        st.close();

        //TODO
     return 200;
    }

    public static int buyItemById(String secret,String itemId) throws Exception {
        //TODO
        return 200;
    }
}

