package com.company.entrypoints;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static com.company.SkinbaronAPI.getSales;
import static com.company.SkinbaronAPI.getSkinbaronInventory;

public class InventoryCrawler {
    public InventoryCrawler() {


        clearInventory();
        getSkinbaronInventory(secret, conn);
        getItemsfromInventory(conn, "https://steamcommunity.com/inventory/76561198286004569/730/2?count=2000", "steam");
        getItemsfromInventory(conn, "https://steamcommunity.com/inventory/76561198331678576/730/2?count=2000", "smurf");
        getSales(secret, conn);
        getStorageItems(conn);
    }

    public void clearInventory() {
        //TODO send Request to CLEAR InventoryLists
    }

    public void getItemsfromInventory(Connection conn, String inventoryurl, String type) throws Exception {

        HttpGet httpGet = new HttpGet(inventoryurl);

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpResponse response = client.execute(httpGet);

        String resultJSON = EntityUtils.toString(response.getEntity());

        String SQLInsert = "INSERT INTO steam_item_sale.inventory(inv_type,name,still_there,amount) "
                + "VALUES(?,?,true,?)";

        HashMap<String, Integer> map = getItemsFromSteamHTTP(resultJSON);

        try (PreparedStatement pstmt = conn.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {

            for (String key : map.keySet()) {
                pstmt.setString(1, type);
                pstmt.setString(2, key);
                pstmt.setInt(3, map.get(key));
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

    public void getStorageItems(Connection conn) throws IOException {

        HttpGet httpGet = new HttpGet("https://steamcommunity.com/inventory/76561198286004569/730/2?count=2000");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpResponse response = client.execute(httpGet);
        String result = EntityUtils.toString(response.getEntity());

        JSONObject result_json = (JSONObject) new JSONTokener(result).nextValue();

        JSONArray assets_array = result_json.getJSONArray("descriptions");

        String SQLInsert = "INSERT INTO steam_item_sale.inventory(inv_type,name,still_there,amount) "
                + "VALUES('storage',?,true,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {

            for (Object jo : assets_array) {
                if (jo instanceof JSONObject) {

                    //if (!((JSONObject) jo).keySet().contains("fraudwarnings")){
                    //    continue;
                    //}
                    if (!"Storage Unit".equals(((JSONObject) jo).getString("market_hash_name"))) {
                        continue;
                    }
                    String amount_string = ((JSONObject) jo).getJSONArray("descriptions").getJSONObject(2).getString("value");
                    int amount = Integer.parseInt(amount_string.split(" ")[3]);

                    if (amount == 0) {
                        continue;
                    }

                    String item_name = ((String) (((JSONObject) jo).getJSONArray("fraudwarnings").get(0))).split("''")[1];

                    if ("Broken Fang Case".equals(item_name)) {
                        item_name = "Operation Broken Fang Case";
                    }

                    if ("Sticker | Tyloo 2020".equals(item_name)) {
                        item_name = "Sticker | TYLOO | 2020 RMR";
                    }

                    if ("Wildfire Case".equals(item_name)) {
                        item_name = "Operation Wildfire Case";
                    }

                    if ("Sticker | Navi 2020".equals(item_name)) {
                        item_name = "Sticker | Natus Vincere | 2020 RMR";
                    }

                    if ("Operation Breakout".equals(item_name)) {
                        item_name = "Operation Breakout Weapon Case";
                    }

                    if ("Vanguard Case".equals(item_name)) {
                        item_name = "Operation Vanguard Weapon Case";
                    }

                    List<String> name_list = new ArrayList<>();

                    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select name from steam_item_sale.item_informations")) {
                        while (rs.next()) {
                            name_list.add(rs.getString("name"));
                        }
                    }

                    if (!name_list.contains(item_name)) {
                        continue;
                    }

                    pstmt.setString(1, item_name);
                    pstmt.setInt(2, amount);
                    pstmt.addBatch();
                }
            }

            int[] updateCounts = pstmt.executeBatch();
            int amount_inserts = IntStream.of(updateCounts).sum();
            if (amount_inserts != 0) {
                System.out.println(amount_inserts + " items were inserted!");
            }

            conn.commit();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public HashMap<String, Integer> getItemsFromSteamHTTP(String resultJSON) {
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
}
