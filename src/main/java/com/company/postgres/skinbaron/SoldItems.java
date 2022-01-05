package com.company.postgres.skinbaron;

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
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.company.old.helper.getConnection;
import static com.company.old.helper.readPasswordFromFile;

public class SoldItems {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SoldItems.class);

    public static int get() throws Exception {
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

            String itemId = null;
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);

                //String classid = jObject.get("classid").toString();
                //String last_updated = jObject.get("last_updated").toString();
                //String instanceid = jObject.get("instanceid").toString();
                //String list_time = jObject.get("list_time").toString();
                String price = jObject.get("price").toString();
                //String assetid = jObject.get("assetid").toString();
                String name = jObject.get("name").toString();
                //String txid = jObject.get("txid").toString();
                //String commission = jObject.get("commission").toString();
                itemId = jObject.get("id").toString();

                try (Statement stmt2 = conn.createStatement()) {
                    ResultSet rs2 = stmt2.executeQuery("select * from steam_item_sale.sold_items where sale_id='" + itemId + "'");

                    if (rs2.next()) {
                        conn.commit();
                        return 200;
                    }
                }

                String sqlIinsert = "INSERT INTO steam_item_sale.sold_items\n" +
                        "(sale_id, name, price,load_counter)\n" +
                        "VALUES(?, ?, ?,?);";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlIinsert, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, itemId);
                    pstmt.setString(2, name);
                    pstmt.setDouble(3, Double.parseDouble(price));
                    pstmt.setInt(4, counter * 50 + i + 1);

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
        return 200;
    }

}
