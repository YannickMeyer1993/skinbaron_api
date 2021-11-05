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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.company.SteamCrawler.setRowInOverviewTable;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

public class SkinbaronAPI {

    private final static Logger LOGGER = Logger.getLogger(SkinbaronAPI.class.getName());

    public static class SkinBaronException extends Exception {

        public SkinBaronException(String message) {
            super(message);
        }
    }

    public static int resendTradeOffers(String secret) throws Exception {

        LOGGER.info("Skinbaron API resendTradeOffers has been called.");
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

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

        if ( resultJson.has("message")) {
            System.out.println("Result: " + resultJson.get("message"));
            throw new SkinBaronException((String) resultJson.get("message"));
        }

        System.out.println("Result: " + response.getStatusLine().getStatusCode());
        return response.getStatusLine().getStatusCode();
    }

    public static int writeSoldItems(String secret) throws Exception {
        String queryId = "";

        Connection conn = getConnection();

        int counter = 0;
        while (true) {

            LOGGER.info("Skinbaron API getSales has been called. (" + counter + ")");
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
            JSONArray jArray = new JSONArray( resultJson.get("response").toString());

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

                try(Statement stmt2 = conn.createStatement()) {
                    ResultSet rs2 = stmt2.executeQuery("select * from steam_item_sale.sold_items where sale_id='" + itemId + "'");

                    if (rs2.next()) {
                        conn.commit();
                        conn.close();
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

        conn.close();

        return 200;
    }

    public static void buyItem(Connection conn, String secret, String itemId, Double price) throws Exception {

        String SqlInsert = "Insert into steam_item_sale.skinbaron_transactions (steam_price,success,name,saleid,price) VALUES (?,?,?,?,?)";

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
        try {resultJson = (JSONObject) new JSONTokener(result).nextValue();}
        catch (ClassCastException exp){
            System.out.println(result);
            throw new ClassCastException();
        }

        if ( resultJson.has("generalErrors")) {
            System.out.println( resultJson.get("generalErrors").toString());
            if ("[\"some offer(s) are already sold\"]".equals( resultJson.get("generalErrors").toString())
                    || "[\"count mismatch - maybe some offers have been sold or canceled or you provided wrong saleids\"]".equals( resultJson.get("generalErrors").toString())
            ) {
                Statement st = conn.createStatement();
                st.execute("DELETE FROM steam_item_sale.skinbaron_market_search_results where id='" + itemId + "'");
                System.out.println("Deleted one Id from Skinbaron table.");
                st.close();
                conn.commit();
            }
            return;
        }

        if (! resultJson.has("items")) {
            LOGGER.info("There was no json query 'result' found.");
            return;
        }

        LOGGER.info(resultJson.toString());

        JSONArray resultArray = resultJson.getJSONArray("items");

        String name = "";
        String saleId = "";
        double steamPrice;

        for (Object o : resultArray) {
            if (o instanceof JSONObject) {
                name = ((JSONObject) o).getString("name");
                saleId = ((JSONObject) o).getString("saleid");
                price = ((JSONObject) o).getDouble("price");
            }

            try(Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("select price_euro from steam_item_sale.steam_most_recent_prices smrp where name ='" + name + "'");

                if (!rs.next()) {
                    throw new NoSuchElementException(name + " has no Steam price.");
                }

                steamPrice = rs.getDouble("price_euro");
            }


            try (PreparedStatement pstmt = conn.prepareStatement(SqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                //steam_price,success,name,saleid,price
                pstmt.setDouble(1, steamPrice);
                pstmt.setBoolean(2, true);
                pstmt.setString(3, name);
                pstmt.setString(4, saleId);
                pstmt.setDouble(5, price);
                int rowsAffected = pstmt.executeUpdate();
            }

            try(Statement st = conn.createStatement()) {
                st.execute("DELETE FROM steam_item_sale.skinbaron_market_search_results where id='" + itemId + "'");
            }

            LOGGER.info("Item \"" + name + "\" was bought for " + price + ". Steam: " + steamPrice);

            conn.commit();
        }
    }

    public static Double getBalance(String secret, Boolean overwriteDB, Connection conn) throws Exception {


        LOGGER.info("Skinbaron API GetBalance has been called.");
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

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

        if (resultJson.has("message")) {
            System.out.println("Result: " + resultJson.get("message"));
            throw new SkinBaronException((String) resultJson.get("message"));
        }

        double skinbaronBalance = resultJson.getDouble("balance");

        try(Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("select count(*) from steam_item_sale.overview where \"DATE\" = CURRENT_DATE;");

            if (!rs.next()) //Start of today
            {
                setRowInOverviewTable(conn);
            } //End Start of the Day
            rs.close();
        }

        String SQLUpdate = "Update steam_item_sale.overview set skinbaron_balance =  ? where \"DATE\" = CURRENT_DATE;";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpdate, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, skinbaronBalance);
            pstmt.executeUpdate();
        }

        LOGGER.info("Skinbaron Balance ist zur Zeit bei: " + skinbaronBalance + " Euro.");
        return skinbaronBalance;
    }

    public static String[] Search(String secret, Connection conn, String after_saleid) throws IOException, InterruptedException {

        int amountInserts = 0;
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

        LOGGER.info("Skinbaron API Search has been called.");
        Thread.sleep(1000);
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

        try {
            JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

            JSONArray resultArray = ((JSONArray) resultJson.get("sales"));


            String id = null;
            Double wear;

            try (PreparedStatement pstmt = conn.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {
                for (Object o : resultArray) {
                    if (o instanceof JSONObject) {
                        id = ((JSONObject) o).getString("id");
                        double price_euro = ((JSONObject) o).getDouble("price");
                        String name = ((JSONObject) o).getString("market_name");
                        String stickers = ((JSONObject) o).getString("stickers");
                        try {
                            pstmt.setDouble(5, ((JSONObject) o).getDouble("wear"));
                        } catch (JSONException je) {
                            pstmt.setNull(5, Types.DOUBLE);
                        }

                        pstmt.setString(1, id);
                        pstmt.setString(2, name);
                        pstmt.setDouble(3, price_euro);
                        pstmt.setString(4, stickers);
                        pstmt.addBatch();
                    }
                }
                int[] updateCounts = pstmt.executeBatch();
                amountInserts = IntStream.of(updateCounts).sum();
                if (amountInserts != 0) {
                    LOGGER.info(amountInserts + " items were inserted!");
                }

                conn.commit();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }


            String[] returnObject = new String[2];
            returnObject[0] = "" + amountInserts;
            returnObject[1] = id;
            return returnObject;
        } catch (ClassCastException e) {
            System.out.println(result);
            String[] returnObject = new String[2];
            returnObject[0] = "" + 50;
            returnObject[1] = after_saleid;
            return returnObject;
        }
    }

    public static void main(String[] args) throws Exception {
        Connection conn = getConnection();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String id = "";

        Scanner sc= new Scanner(System.in);
        System.out.println("Enter last Id: ");
        id = sc.nextLine();

        //noinspection InfiniteLoopStatement
        while (true) { //infinite times
            while (true) { //as long as there are inserts
                String[] output = Search(secret, conn, id);
                if (Integer.parseInt(output[0]) == 0) {
                    break;
                }
                id = output[1];
                System.out.println("Not finished yet. Last id=" + id + " (" + (new Timestamp(System.currentTimeMillis())) + ")");
            }
            id = "";
            //noinspection BusyWait
            Thread.sleep((long) 5 * 1000);
            System.out.println("------------------------------------------------------------------");
            System.out.println("New Search started.");
            System.out.println("------------------------------------------------------------------");
        }
    }

    public static void getSkinbaronInventory(String secret, Connection conn) throws Exception {

        LOGGER.info("Skinbaron API GetInventory has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"type\": 2,\"appid\": 730,\"items_per_page\": 50}"; //items_er_page?

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetInventory");
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

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();
        JSONArray resultArray = ((JSONArray) resultJson.get("items"));

        String SQLInsert = "INSERT INTO steam_item_sale.inventory(inv_type,name,still_there,amount) "
                + "VALUES('skinbaron',?,true,?)";

        HashMap<String, Integer> map = new HashMap<>();

        for (Object o : resultArray) {
            if (o instanceof JSONObject) {
                String name = ((JSONObject) o).getString("marketHashName");
                if (!map.containsKey(name)) {
                    map.put(name, 1);
                } else {
                    map.put(name, map.get(name) + 1);
                }
            }
        }

        try (PreparedStatement pstmt = conn.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {

            for (String key : map.keySet()) {
                pstmt.setString(1, key);
                pstmt.setInt(2, map.get(key));
                pstmt.addBatch();
            }

            int[] updateCounts = pstmt.executeBatch();
            int amount_inserts = IntStream.of(updateCounts).sum();
            if (amount_inserts != 0) {
                System.out.println(amount_inserts + " items were inserted!");
            }
        }
        conn.commit();
    }

    public static void getSales(String secret, Connection conn) throws Exception {

        LOGGER.info("Skinbaron API GetSales has been called.");
        String id = null;

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetSales");
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HashMap<String, Integer> map = new HashMap<>();

        while (true) {
            String jsonInputString = "{\"apikey\": \"" + secret + "\",\"type\":2,\"appid\": 730,\"items_per_page\": 50" + (id == null ? "" : ",\"after_saleid\":\"" + id + "\"") + "}";

            HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
            httpPost.setEntity(entity);
            HttpResponse response = client.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());

            JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();
            JSONArray resultArray = ((JSONArray) resultJson.get("response"));

            if (resultArray.length() == 0) {
                break;
            }
            for (Object o : resultArray) {
                if (o instanceof JSONObject) {
                    String name = ((JSONObject) o).getString("name");
                    id = ((JSONObject) o).getString("id");

                    if (!map.containsKey(name)) {
                        map.put(name, 1);
                    } else {
                        map.put(name, map.get(name) + 1);
                    }
                }
            }
        }

        String SQLInsert = "INSERT INTO steam_item_sale.inventory(inv_type,name,still_there,amount) "
                + "VALUES('skinbaron_sales',?,true,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {

            for (String key : map.keySet()) {
                pstmt.setString(1, key);
                pstmt.setInt(2, map.get(key));
                pstmt.addBatch();
            }

            int[] updateCounts = pstmt.executeBatch();
            int amount_inserts = IntStream.of(updateCounts).sum();
            if (amount_inserts != 0) {
                System.out.println(amount_inserts + " items were inserted!");
            }
        }
        conn.commit();
    }

    public static void buyFromSelect(String secret, Connection conn) throws Exception {

        try(Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("select smsr.price,smsr.id from steam_item_sale.skinbaron_market_search_results smsr inner join steam_item_sale.cases on smsr.name=cases.name and price = 0.02");

            while (rs.next()) //Start of today
            {
                buyItem(conn, secret, rs.getString("id"), rs.getDouble("price"));
            }
            rs.close();
        }
    }

    public static void getExtendedPriceList(String secret, Connection conn) throws Exception {

        Statement st = conn.createStatement();
        st.execute("TRUNCATE TABLE steam_item_sale.skinbaron_pricelist");
        st.close();

        String SQLinsert = "INSERT INTO steam_item_sale.skinbaron_pricelist (name, exterior, is_stattrak, is_souvenir, lowestprice, markethashname, minwear, maxwear, imageurl) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ? )";

        LOGGER.info("Skinbaron API getExtendedPriceList has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"appId\": 730}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetExtendedPriceList");
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
            throw new SkinBaronException((String) resultJson.get("message"));
        }

        JSONArray resultArray = (JSONArray) resultJson.get("map");

        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {

            String name = null;
            String exterior = null;
            boolean statTrak = false;
            boolean souvenir = false;
            String marketHashName = null;
            BigDecimal minWear = null;
            BigDecimal maxWear = null;
            String imageUrl = null;
            BigDecimal lowestPrice = null;

            for (Object o : resultArray) {
                if (o instanceof JSONObject) {

                    name = (String) ((JSONObject) o).get("name");

                    if (((JSONObject) o).has("exterior")) {
                        exterior = (String) ((JSONObject) o).get("exterior");
                    }
                    if (((JSONObject) o).has("statTrak")) {
                        statTrak = (boolean) ((JSONObject) o).get("statTrak");
                    }
                    if (((JSONObject) o).has("souvenir")) {
                        souvenir = (boolean) ((JSONObject) o).get("souvenir");
                    }
                    if (((JSONObject) o).get("lowestPrice") instanceof Integer){
                        lowestPrice = new BigDecimal ((Integer)((JSONObject) o).get("lowestPrice"));
                    } else {
                        lowestPrice = (BigDecimal) ((JSONObject) o).get("lowestPrice");
                    }
                    marketHashName = (String) ((JSONObject) o).get("marketHashName");

                    if (((JSONObject) o).has("minWear")) {
                        minWear = (BigDecimal) ((JSONObject) o).get("minWear");
                    }

                    if (((JSONObject) o).has("maxWear")) {
                        maxWear = (BigDecimal) ((JSONObject) o).get("maxWear");
                    }
                    imageUrl = (String) ((JSONObject) o).get("imageUrl");


                    pstmt.setString(1,name );
                    pstmt.setString(2,exterior);
                    pstmt.setBoolean(3,statTrak);
                    pstmt.setBoolean(4,souvenir);
                    pstmt.setBigDecimal(5,lowestPrice);
                    pstmt.setString(6,marketHashName);
                    pstmt.setBigDecimal(7,minWear);
                    pstmt.setBigDecimal(8,maxWear);
                    pstmt.setString(9,imageUrl);
                    pstmt.addBatch();
                }

            }

            int[] updateCounts = pstmt.executeBatch();
            System.out.println(updateCounts.length + " items were inserted!");
            conn.commit();

        }

    }

    public static void getNewestSales30Days(String secret, Connection conn, String itemName) throws Exception {

        String ItemName = "Aufkleber | device | Atlanta 2017";
        LOGGER.info("Skinbaron API GetNewestSales30Days has been called.");
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
            throw new SkinBaronException((String) resultJson.get("message"));
        }

        //TODO API does not work yet?

    }
}

