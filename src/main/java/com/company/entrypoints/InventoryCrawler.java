package com.company.entrypoints;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.company.common.Constants.*;
import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;

public class InventoryCrawler {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(InventoryCrawler.class);

    public InventoryCrawler() throws Exception {
        setUpClass();

        clearInventory();
        getSkinbaronInventory();
        getItemsfromInventory("https://steamcommunity.com/inventory/76561198286004569/730/2?count=2000", INV_TYPE_steam);
        getItemsfromInventory("https://steamcommunity.com/inventory/76561198331678576/730/2?count=2000", INV_TYPE_smurf);
        getSkinbaronSales();
        getStorageItems();
    }

    public void clearInventory() {
        String url = "http://localhost:8080/api/v1/DeleteInventoryItems";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(null, headers);

        restTemplate.postForObject(url, request, String.class);
    }

    public void getItemsfromInventory(String inventoryurl, String type) throws Exception {

        logger.info("Getting inventory: "+type);
        HttpGet httpGet = new HttpGet(inventoryurl);

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpResponse response = client.execute(httpGet);

        String resultJSON = EntityUtils.toString(response.getEntity());

        HashMap<String, Integer> map = getItemsFromSteamHTTP(resultJSON);

        for (String key : map.keySet()) {
            sendRequestInsertInventoryItem(key,map.get(key),type);
        }
    }

    public void getStorageItems() throws IOException {

        logger.info("Getting storage items");

        HttpGet httpGet = new HttpGet("https://steamcommunity.com/inventory/76561198286004569/730/2?count=2000");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpResponse response = client.execute(httpGet);
        String result = EntityUtils.toString(response.getEntity());

        JSONObject result_json = (JSONObject) new JSONTokener(result).nextValue();

        JSONArray assets_array = result_json.getJSONArray("descriptions");

        for (Object jo : assets_array) {
            if (jo instanceof JSONObject) {

                if (!"Storage Unit".equals(((JSONObject) jo).getString("market_hash_name"))) {
                    continue;
                }
                String amount_string = ((JSONObject) jo).getJSONArray("descriptions").getJSONObject(2).getString("value");
                int amount = Integer.parseInt(amount_string.split(" ")[3]);

                if (amount == 0) {
                    continue;
                }

                String item_name = ((String) (((JSONObject) jo).getJSONArray("fraudwarnings").get(0))).split("''")[1];

                switch (item_name){
                    case "Broken Fang Case":item_name = "Operation Broken Fang Case";
                        break;
                    case "Sticker | Tyloo 2020":item_name = "Sticker | TYLOO | 2020 RMR";
                        break;
                    case "Wildfire Case":item_name = "Operation Wildfire Case";
                        break;
                    case "Sticker | Navi 2020":item_name = "Sticker | Natus Vincere | 2020 RMR";
                        break;
                    case "Operation Breakout":item_name = "Operation Breakout Weapon Case";
                        break;
                    case "Vanguard Case":item_name = "Operation Vanguard Weapon Case";
                        break;
                }
                sendRequestInsertInventoryItem(item_name,amount,INV_TYPE_storage);
            }
        }
    }

    public static HashMap<String, Integer> getItemsFromSteamHTTP(String resultJSON) {
        JSONObject result_json = (JSONObject) new JSONTokener(resultJSON).nextValue();

        HashMap<String, Integer> assets_map = new HashMap<>();
        HashMap<String, String> descriptions_map = new HashMap<>();

        JSONArray assets_array = result_json.getJSONArray("assets");
        JSONArray descriptions_array = result_json.getJSONArray("descriptions");

        for (Object jo : assets_array) {
            if (jo instanceof JSONObject) {
                String classid = ((JSONObject) jo).getString("classid");
                if (!assets_map.containsKey(classid)) {
                    assets_map.put(classid, 1);
                } else {
                    assets_map.put(classid, assets_map.get(classid) + 1);
                }
            }
        }

        for (Object jo : descriptions_array) {
            if (jo instanceof JSONObject) {
                if (((JSONObject) jo).getInt("marketable") == 1) {
                    descriptions_map.put(((JSONObject) jo).getString("classid"), ((JSONObject) jo).getString("market_hash_name"));
                }
            }
        }

        HashMap<String, Integer> map = new HashMap<>();

        for (String classid : descriptions_map.keySet()) {
            String name = descriptions_map.get(classid);
            if (!assets_map.containsKey(classid)) {
                continue;
            }
            int amount = assets_map.get(classid);

            map.put(name, amount);
        }

        return map;
    }

    /**
     * items_per_page is not needed
     * @throws Exception breaks if error occurs
     */
    public void getSkinbaronInventory() throws Exception {

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        logger.info("Skinbaron API GetInventory has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"type\": 2,\"appid\": 730,\"items_per_page\": 50}";

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

        for (Object o : resultArray) {
            if (o instanceof JSONObject) {
                String name = ((JSONObject) o).getString("marketHashName");
                sendRequestInsertInventoryItem(name,1,INV_TYPE_skinbaron);
            }
        }
    }

    /**
     * type in API: 2
     */
    public void getSkinbaronSales() throws Exception {

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        logger.info("Skinbaron API GetSales has been called.");
        String id = null;

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetSales");
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

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

                    sendRequestInsertInventoryItem(name,1,INV_TYPE_SKINBARON_SALES);
                }
            }
        }
    }

    public void sendRequestInsertInventoryItem(String ItemName, int amount,String InventoryType) {
        String url = "http://localhost:8080/api/v1/AddInventoryItem";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("itemname", ItemName);
        JsonObject.put("amount", amount);
        JsonObject.put("inventorytype", InventoryType);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }
}
