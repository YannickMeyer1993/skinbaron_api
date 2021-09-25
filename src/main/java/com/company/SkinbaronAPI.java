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
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.IntStream;

import static com.company.SteamCrawler.setRowInOverviewTable;
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

        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

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

                Statement stmt2 = conn.createStatement();
                ResultSet rs2 = stmt2.executeQuery("select * from steam_item_sale.sold_items where sale_id='"+item_id+"'");

                if (rs2.next()){
                    conn.commit();
                    conn.close();
                    return 200;
                }

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

    public static Double getBalance(String secret, Boolean overwriteDB) throws Exception {

        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        System.out.println("Skinbaron API GetBalance has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\"}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetBalance");
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

        Double skinbaronBalance = result_json.getDouble("balance");

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from steam_item_sale.overview where \"DATE\" = CURRENT_DATE;");

        if (!rs.next()) //Start of today
        {
            setRowInOverviewTable(conn);
        } //End Start of the Day

        String SQLUpdate = "Update steam_item_sale.overview set steam_balance =  ? where \"DATE\" = CURRENT_DATE;";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpdate, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, skinbaronBalance);
            int rowsAffected = pstmt.executeUpdate();
        }

        System.out.println("Skinbaron Balance ist zur Zeit bei: "+skinbaronBalance+" Euro.");
        return skinbaronBalance;
    }

    public static String[] Search(String secret, Connection conn, String after_saleid) throws IOException, SQLException {

        int amount_inserts = 0;
        String SQLUpsert = "WITH\n" +
                "    to_be_upserted (id,name,price,stickers,wear) AS (\n" +
                "        VALUES\n" +
                "            (?,?,?,?,?)\n" +
                "    ),\n" +
                "    updated AS (\n" +
                "        UPDATE\n" +
                "            steam_item_sale.skinbaron_market_search_results s\n" +
                "        SET\n" +
                "            price = to_be_upserted.price::numeric\n" +
                "        FROM\n" +
                "            to_be_upserted\n" +
                "        WHERE\n" +
                "            s.id = to_be_upserted.id\n" +
                "        RETURNING s.id\n" +
                "    )\n" +
                "INSERT INTO steam_item_sale.skinbaron_market_search_results\n" +
                "    SELECT * FROM to_be_upserted\n" +
                "    WHERE id NOT IN (SELECT id FROM updated);";

        System.out.println("Skinbaron API Search has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appid\": 730,\"items_per_page\": 50" + (!"".equals(after_saleid) ? ",\"after_saleid\":\"" + after_saleid + "\"" : "") + "}";

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

        JSONObject result_json = (JSONObject) new JSONTokener(result).nextValue();

        JSONArray result_array = ((JSONArray) result_json.get("sales"));

        String id = null;
        String wear;

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {
            for (Object o : result_array) {
                if (o instanceof JSONObject) {
                    System.out.println(o.toString());
                    id = ((JSONObject) o).getString("id");
                    Double price_euro = ((JSONObject) o).getDouble("price");
                    String name = ((JSONObject) o).getString("market_name");
                    String stickers = ((JSONObject) o).getString("stickers");
                    try {
                        wear = ((JSONObject) o).get("wear").toString();
                    }
                    catch (JSONException je) {
                        wear = null;
                    }

                    pstmt.setString(1, id);
                    pstmt.setString(2, name);
                    pstmt.setDouble(3, price_euro);
                    pstmt.setString(4, stickers);
                    pstmt.setString(5, ""+wear);
                    pstmt.addBatch();
                }
            }
            int[] updateCounts = pstmt.executeBatch();
            amount_inserts = IntStream.of(updateCounts).sum();
            System.out.println(amount_inserts + " items were inserted!");

            conn.commit();

        } catch (SQLException throwables) {
                    throwables.printStackTrace();
        }

        String[] return_object = new String[2];
        return_object[0] = ""+amount_inserts;
        return_object[1] = id;
        return return_object;
    }
}

