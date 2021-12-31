package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.helper.getConnection;


public class SteamCrawler {

    private static double conversionFromUSDtoEUR;


    static {
        try {
            conversionFromUSDtoEUR = CurrencyConverter.getUSDinEURO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        String query = "select highest_iteration_steam+1 as iteration from steam_item_sale.overview where \"DATE\" = CURRENT_DATE;";

        try(Connection conn = getConnection();Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(query)) {
            int iteration;
            if (!rs.next()) //Start of today
            {
                setRowInOverviewTable(conn);
                iteration = 1;
            } //End Start of the Day
            else {
                iteration = rs.getInt("iteration"); //rs.next() was called above
            }

            System.out.println("Starte mit Iteration  " + iteration);

            int MAX_ITERATION = 1600;
            int wait_counter = 3;
            while (iteration < MAX_ITERATION) {
                try {
                    System.out.println("Waiting for " + Math.pow(2, wait_counter) + " seconds");
                    Thread.sleep((long) (Math.pow(2, wait_counter) * 1000));
                    Boolean works = getItemsforSteamPageNumber(conn, iteration);
                    setIterationCounter(conn, iteration);
                    conn.commit();
                    wait_counter = 4;

                    if (works) {
                        iteration++;

                    }
                } catch (InterruptedException e) {
                    System.out.println("Program got interrupted.");
                    return;
                } catch (Exception e) {
                    wait_counter++;
                }

            }

            System.out.println("Maximale Iterationsanzahl erreicht. Programm wird beendet.");

            conn.commit();
        }
    }

    public static void setRowInOverviewTable(Connection conn) throws SQLException {
        String SQLinsert = "INSERT INTO steam_item_sale.overview(\"DATE\",highest_iteration_steam,steam_balance,steam_open_sales,skinbaron_balance,smurf_inv_value,skinbaron_open_sales_wert,steam_inv_value,skinbaron_inv_value,kommentar) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, 0);
            pstmt.setInt(3, 0);
            pstmt.setInt(4, 0);
            pstmt.setInt(5, 0);
            pstmt.setInt(6, 0);
            pstmt.setInt(7, 0);
            pstmt.setInt(8, 0);
            pstmt.setInt(9, 0);
            pstmt.setString(10, "");
            int rowsAffected = pstmt.executeUpdate();
        }
    }

    public static void setIterationCounter(@NotNull Connection conn, int i) {
        String SQLinsert = "UPDATE steam_item_sale.overview set highest_iteration_steam=? where \"DATE\"=current_date";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, i);

            //System.out.println(pstmt);
            int rowsAffected = pstmt.executeUpdate();
            conn.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


    public static @NotNull Boolean getItemsforSteamPageNumber(Connection conn, int pageNumber) throws Exception {
        System.out.println("Iteration: " + pageNumber);

        if (pageNumber % 50 == 0) {
            conversionFromUSDtoEUR = CurrencyConverter.getUSDinEURO();
            System.out.println("Conversion Factor from USD to EUR: " + conversionFromUSDtoEUR);
        }


        String url = "https://steamcommunity.com/market/search?appid=730&currency=3#p" + pageNumber + "_popular_desc";

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(1000);

        List<DomElement> Items = page.getByXPath("//*[contains(@class, 'market_listing_row market_recent_listing_row market_listing_searchresult')]");

        System.out.println("There are " + Items.size() + " Items on the Steam Page no. " + pageNumber + "\n");

        if (Items.size() == 0) {
            throw new Exception("No Items found.");
        }

        for (DomElement item : Items) {
            String item_xml = item.asXml();

            //System.out.println(item_xml);

            Document document = new SAXReader().read(new StringReader(item_xml));
            String name = document.valueOf("/div/@data-hash-name");

            int quantity = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-qty"));
            int price_source = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-price"));
            int currencyId = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-currency"));

            if (name == null) {
                return false;
            }

            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            df.setRoundingMode(java.math.RoundingMode.HALF_UP);

            double price_eur;

            if (1 == currencyId) {

                price_eur = Double.parseDouble(df.format(conversionFromUSDtoEUR * price_source / 100).replace(",", "."));
            } else if (3 == currencyId) {
                price_eur = Double.parseDouble(df.format(price_source / 100).replace(",", "."));
            } else {
                return false;
            }

            String SQLinsert = "INSERT INTO steam_item_sale.steam_item_prices(name,quantity,price_euro) "
                    + "VALUES(?,?,?)";

            try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, price_eur);

                //System.out.println(pstmt);
                int rowsAffected = pstmt.executeUpdate();
            } catch (Exception e) {
                return false;
            }
            conn.commit();

        } //End of for each Item

        return true;
    }

    public static void updateItemPricesLongNotSeen(Connection conn) throws Exception {

        try(Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery("select * from steam_item_sale.steam_most_recent_prices where name not like 'Souvenir%' order by \"timestamp\" asc")) {
            String name;
            while (rs.next()) {
                name = rs.getString("name");
                getSteamPriceForGivenName(name, conn);
            }
        }
    }

    public static void updateItemPrices0Euro(Connection conn) throws Exception {

        try(Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery("select * from steam_item_sale.steam_most_recent_prices where price_euro = 0")) {
            String name;
            while (rs.next()) {
                name = rs.getString("name");
                getSteamPriceForGivenName(name, conn);
            }
        }
    }

    public static void getItemsfromInventory(Connection conn, String inventoryurl, String type) throws Exception {

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

    public static void getStorageItems(Connection conn) throws IOException {

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

                    List<String> name_list = new ArrayList<>();

                    try(Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery("select name from steam_item_sale.item_informations")) {
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


    public static  HashMap<String, Integer> getItemsFromSteamHTTP(String resultJSON) {
        JSONObject result_json = (JSONObject) new JSONTokener(resultJSON).nextValue();

        HashMap<String, Integer> assets_map = new HashMap<>();
        HashMap<String, String> descriptions_map = new HashMap<>();

        JSONArray assets_array = result_json.getJSONArray("assets");
        JSONArray descriptions_array = result_json.getJSONArray("descriptions");


        for (Object jo : assets_array) {
            if (jo instanceof JSONObject) {
                String classid = ((JSONObject) jo).getString("classid");
                if (!assets_map.containsKey(classid)){
                    assets_map.put(classid,1);
                } else {
                    assets_map.put(classid,assets_map.get(classid)+1);
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


