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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;
import java.util.stream.IntStream;

import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.getConnection;

public class SkinbaronCrawler {
    private final static Logger logger = LoggerFactory.getLogger(SkinbaronCrawler.class);

    public static void main(String[] args) throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        Scanner sc = new Scanner(System.in);
        logger.info("Enter last Id: ");
        String id = sc.nextLine();

        //noinspection InfiniteLoopStatement
        while (true) { //infinite times  //TODO überprüfen, ob while true x2 notwendig
            while (true) { //as long as there are inserts
                try {
                    String[] output = Search(secret, id);
                    if (Integer.parseInt(output[0]) == 0) {
                        break;
                    }
                    id = output[1];

                } catch (org.apache.http.conn.HttpHostConnectException e)
                {
                    Thread.sleep(2000);
                }
                logger.info("Not finished yet. Last id=" + id + " (" + (new Timestamp(System.currentTimeMillis())) + ")");
            }
            id = "";
            //noinspection BusyWait
            Thread.sleep((long) 5 * 1000);
            logger.info("------------------------------------------------------------------");
            logger.info("New Search started.");
            logger.info("------------------------------------------------------------------");
        }
    }

    public static String[] Search(String secret, String after_saleid) throws IOException, InterruptedException {

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

        logger.info("Skinbaron API Search has been called.");
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
            try (Connection conn = getConnection();PreparedStatement pstmt = conn.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {
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
                    logger.info(amountInserts + " items were inserted!");
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
            logger.info(result);
            String[] returnObject = new String[2];
            returnObject[0] = "" + 50;
            returnObject[1] = after_saleid;
            return returnObject;
        }
    }
}
