package de.yannickm.steambot.crawler.steamcrawler;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PostgresHelper.getConnection;

public class steamCrawler {
    private static final Logger logger = LoggerFactory.getLogger(steamCrawler.class);
    private static final String UrlPost = "http://localhost:8080/api/v1/AddSteamPrice";
    private static Double conversionRateUSDinEUR;

    public static void main(String[] args) throws Exception {
        setUpClass(); //disable Logging
        Boolean repeat = true;
        int start = 5000;

        while (repeat) {
            try {
                repeat = requestSearch(start);
                Thread.sleep(3000);
                start+=100;
            } catch (Exception e) {
                logger.error(e.getMessage());
                logger.error("Retry for Index: "+start);
                Thread.sleep(7000);
            }
        }
    }

    /**
     * @param start start index in search
     * @return true, if end is not reached yet. Else false
     * @throws Exception
     */
    static Boolean requestSearch(int start) throws Exception {
        HttpPost httpPost = new HttpPost("https://steamcommunity.com/market/search/render/?search_descriptions=0&sort_column=name&sort_dir=desc&appid=730&norender=1&count=500&currency=3&start="+start);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

        if (!resultJson.has("success") || resultJson.getInt("total_count")==0) {
            throw new Exception(resultJson.toString());
        }

        if (start > resultJson.getInt("total_count")) {
            logger.info("End reached.");
            return false;
        }

        logger.info("Success: "+resultJson.getBoolean("success"));
        logger.info("Start: "+resultJson.getInt("start"));
        logger.info("Page Size: "+resultJson.getInt("pagesize"));
        logger.info("Total Count: "+resultJson.getInt("total_count"));
        logger.info("Count within JSON: "+((JSONArray)resultJson.get("results")).length());

        JSONArray insertArray = new JSONArray();

        int add_to_start = 0;
        for (Object o: ((JSONArray)resultJson.get("results"))) {
            if (o instanceof JSONObject) {
                String name = ((JSONObject) o).getString("hash_name");
                int quantity = ((JSONObject) o).getInt("sell_listings");
                Double sell_price = Double.valueOf(((JSONObject) o).getString("sell_price_text").substring(1).replace(",",""));
                Double price = Double.valueOf(((JSONObject) o).getString("sale_price_text")
                        .substring(1) //currency symbol
                        .replace(",","") //thousand
                );

                JSONObject o1 = new JSONObject();
                o1.put("itemname",name);
                o1.put("quantity",quantity);
                //o1.put("sell_price",sell_price); //what is this value?
                o1.put("price",price);

                int steamStartIndex = start+add_to_start;
                o1.put("start_parameter",steamStartIndex);

                insertArray.put(o1);

                //TODO batch insert over insertArray
                requestInsertNewSteamprice(name,price,quantity,steamStartIndex);

            }
            add_to_start++;
        }
        return true;
    }

    public static void requestInsertNewSteamprice(String name, Double price, int quantity, Integer steamStartIndex) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("itemname", name);
        JsonObject.put("price", price);
        JsonObject.put("quantity",quantity);
        JsonObject.put("steamstartindex",steamStartIndex);

        HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(UrlPost, request, String.class);
    }

    public static int getStartIndexForGivenName(String hash_name) throws Exception {
        try(Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select start_index from steam.steam_item_indexes s\n" +
                " where s.name = '"+hash_name+"';")) {

            rs.next();
            return rs.getInt("start_index");
        }
    }

    public static double getSteamPriceForGivenName(String hash_name) throws Exception {

        Boolean repeat = true;
        while (repeat) {
            try {
                repeat = false;
                requestSearch(getStartIndexForGivenName(hash_name));
                Thread.sleep(3000);
            } catch (Exception e) {
                repeat = true;
                logger.error("Retry for Item: "+hash_name);
                Thread.sleep(7000);
            }
        }

        try(Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select s.price_euro from steam.steam_current_prices s\n" +
                " where s.name = '"+hash_name+"';")) {

            rs.next();
            return rs.getDouble("price_euro");
        }
    }
}
