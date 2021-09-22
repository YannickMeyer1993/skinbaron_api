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
import org.apache.xpath.operations.Bool;
import org.json.*;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
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

        JSONObject result_json= (JSONObject) new JSONTokener(result).nextValue();

        if (result_json.has("message"))
        {
            System.out.println("Result: "+result_json.get("message"));
            throw new Exception((String) result_json.get("message"));
        }

        System.out.println("Result: "+response.getStatusLine().getStatusCode());
        return response.getStatusLine().getStatusCode();
    }

    public static int writeSoldItems(String secret) throws Exception {
        String queryId = "";

        //TODO
        //Query id with highest load_counter

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
        st.close();

        Boolean run = true;

        int counter = 0;
        while(run) {

            System.out.println("Skinbaron API getSales has been called. ("+counter+")");
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

            JSONObject result_json = (JSONObject) new JSONObject(result);
            JSONArray jArray = (JSONArray) new JSONArray(result_json.get("response").toString());

            String item_id = null;
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
                item_id = jObject.get("id").toString();

                String SQLinsert = "INSERT INTO steam_item_sale.sold_items\n" +
                        "(sale_id, name, price,load_counter)\n" +
                        "VALUES(?, ?, ?,?);";
                try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, item_id);
                    pstmt.setString(2, name);
                    pstmt.setDouble(3, Double.parseDouble(price));
                    pstmt.setInt(4,counter*50+i+1);

                    int rowsAffected = pstmt.executeUpdate();
                }
            }

            counter++;
            queryId = item_id;
            conn.commit();
            if (jArray.length()<50){
                break; //This means that the last 50 items were reached
            }

        }

    conn.close();;
    return 200;
    }

    public static int buyItemById(String secret,String itemId) throws Exception {
        //TODO
        return 200;
    }
}

