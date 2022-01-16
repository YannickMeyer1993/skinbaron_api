package com.company.dataaccessobject;

import com.company.common.Constants;
import com.company.model.Item;
import com.company.model.ItemCollection;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.StringReader;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.IntStream;

import static com.company.common.PostgresHelper.*;

//TODO pg dump pro Tabelle
//TODO Insert Investment Items
//TODO Insert Collections
//TODO Items which are sold on 100% Steam Price
@Repository("postgres")
public class PostgresDAO implements ItemDAO {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PostgresDAO.class);
    final String resourcePath = "src/main/resources/PostgresDAO/";

    public PostgresDAO() throws Exception {
        init();
    }

    @Override
    public void init() throws Exception {

        executeDDLsfromDirectory(resourcePath + "init/");

        if (checkIfResultsetIsEmpty("select * from steam.item_informations")) {
            crawlItemInformations();
            //crawlWearValues();
        }

        //all data is already inside the tables

    }

    @Override
    public String addSkinbaronItem(SkinbaronItem item) throws Exception {
        ArrayList<SkinbaronItem> list = new ArrayList<>();
        list.add(item);
        return addSkinbaronItems(list);
    }

    public String addSkinbaronItems(List<SkinbaronItem> items) throws Exception {
        int amountInserts;
        String last_id = null;
        String SQLUpsert = "WITH\n" +
                "    to_be_upserted (id,name,price,stickers,wear,inspect,sbinspect) AS (\n" +
                "        VALUES\n" +
                "            (?,?,?,?,?,?,?)\n" +
                "    ),\n" +
                "    updated AS (\n" +
                "        UPDATE\n" +
                "            steam.skinbaron_items s\n" +
                "        SET\n" +
                "            price = to_be_upserted.price::numeric\n" +
                "        FROM\n" +
                "            to_be_upserted\n" +
                "        WHERE\n" +
                "            s.id = to_be_upserted.id\n" +
                "        RETURNING s.id\n" +
                "    )\n" +
                "INSERT INTO steam.skinbaron_items (id,name,price,stickers,wear,inspect,sbinspect)\n" +
                "    SELECT id,name,price,stickers,wear,inspect,sbinspect FROM to_be_upserted\n" +
                "    WHERE id NOT IN (SELECT id FROM updated);";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            for (SkinbaronItem item : items) {
                pstmt.setString(1, item.getId());
                pstmt.setString(2, item.getName());
                pstmt.setDouble(3, item.getPrice().getValue());
                pstmt.setString(4, item.getStickers());
                pstmt.setDouble(5, item.getWear());
                pstmt.setString(6, item.getInspect());
                pstmt.setString(7, item.getSbinspect());
                pstmt.addBatch();

                last_id = item.getId();
            }

            logger.info(pstmt.toString());

            int[] updateCounts = pstmt.executeBatch();
            amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted!");
            }

            connection.commit();
        }

        return (amountInserts == 1 ? last_id : "");
    }

    @Override
    public void addSteamPrice(SteamPrice price) throws Exception {
        ArrayList<SteamPrice> list = new ArrayList<>();
        list.add(price);
        addSteamPrices(list);
    }

    public void addSteamPrices(List<SteamPrice> prices) throws Exception {
        String Insert = "INSERT INTO steam.steam_prices(name,quantity,price_euro) VALUES(?,?,?)";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(Insert, Statement.RETURN_GENERATED_KEYS)) {

            for (SteamPrice price : prices) {
                pstmt.setString(1, price.getItemName());
                pstmt.setInt(2, price.getQuantity());
                pstmt.setDouble(3, price.getValue());
                pstmt.addBatch();
            }

            logger.info(pstmt.toString());

            int[] updateCounts = pstmt.executeBatch();
            int amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted!");
            }

            connection.commit();
        }
    }

    @Override
    public JSONArray getItemsToBuy() throws Exception {
        String query = "select steam_price_is_new,skinbaron_price,steam_price, name,skinbaron_ids from steam.skinbaron_buyable_items order by rati desc";

        org.json.JSONArray array = new JSONArray();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                JSONObject JsonObject = new JSONObject();
                JsonObject.put("steam_price_is_new", rs.getBoolean("steam_price_is_new"));
                JsonObject.put("skinbaron_ids", rs.getString("skinbaron_ids"));
                JsonObject.put("name", rs.getString("name"));
                JsonObject.put("skinbaron_price", rs.getDouble("skinbaron_price"));
                JsonObject.put("steam_price", rs.getDouble("steam_price"));

                array.put(JsonObject);
            }
            System.out.println(array);
        }
        return array;
    }

    @Override
    public int getHighestSteamIteration() throws Exception {
        String query = "select iteration from steam.steam_iteration where \"date\" = CURRENT_DATE;";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (!rs.next()) {
                initHightestSteamIteration();
                return getHighestSteamIteration();
            } else {
                return rs.getInt("iteration"); //rs.next() was called above
            }
        }
    }

    @Override
    public void initHightestSteamIteration() throws Exception {

        Connection connection = getConnection();

        if (!checkIfResultsetIsEmpty("select iteration from steam.steam_iteration where \"date\" = CURRENT_DATE;")) {
            return;
        }
        String SQLinsert = "INSERT INTO steam.steam_iteration(iteration) "
                + "VALUES(0)";
        try (PreparedStatement pstmt = connection.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();
            connection.commit();
        }

        logger.info("table steam_iteration has a new entry for today.");

    }

    @Override
    public void setHighestSteamIteration(int iteration) throws Exception {

        //for day change
        initHightestSteamIteration();

        String SQLinsert = "UPDATE steam.steam_iteration set iteration=? where \"date\"=current_date";

        if (checkIfResultsetIsEmpty("select iteration from steam.steam_iteration where \"date\" = CURRENT_DATE;")) {
            throw new Exception("steam.steam_iteration must be initialized.");
        }

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, iteration);

            pstmt.executeUpdate();
            connection.commit();
        }
        logger.info("Highest steam iteration for today is: " + iteration);
    }

    @Override
    public void addInventoryItems(JsonNode payload) throws Exception {

        executeDDL("update steam.inventory set still_there = false where still_there;");

        String sql = "select name from steam.item_informations where name =?;";
        String SQLInsert = "INSERT INTO steam.inventory(inv_type,name,amount,still_there) "
                + "VALUES(?,?,?,true)";

        try (Connection connection = getConnection();
             PreparedStatement pstmt1 = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmt2 = connection.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {

            JSONArray array = new JSONArray(payload.toString());
            for (int i=0;i<array.length();i++) {
                JSONObject o = (JSONObject) array.get(i);
                String itemname = o.getString("itemname");
                String inventorytype = o.getString("inventorytype");
                int amount = o.getInt("amount");
                pstmt1.setString(1, itemname);

                ResultSet rs = pstmt1.executeQuery();
                if (!rs.next()) {
                    continue;
                }

                pstmt2.setString(1, inventorytype);
                pstmt2.setString(2, itemname);
                pstmt2.setInt(3, amount);
                pstmt2.addBatch();
                logger.info("Item \"" + itemname + "\" was inserted to inventory.");
            }

            int amountInserts;
            int[] updateCounts = pstmt2.executeBatch();
            amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted!");
            }
            connection.commit();
        }
    }

    @Override
    public Item getItem(String ItemName) {
        ItemCollection collection = new ItemCollection("", false);
        return new Item(ItemName, collection);
    }

    @Override
    public void cleanUp() throws Exception {
        executeDDLfromPath(resourcePath + "cleanUp.sql");
    }

    @Override
    public void crawlItemInformations() throws Exception {

        Map<String, String[]> map = new HashMap<>();

        executeDDL("TRUNCATE TABLE steam.item_informations;");

        String url = "https://csgo.exchange/prices/";

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(1000);

        List<DomElement> Items = page.getByXPath("//*[contains(@class, 'cItem')]");

        System.out.println("Item Information on csgo.exchhange will be scawled!");

        for (DomElement item : Items) {
            String item_xml = item.asXml();

            String name = item.getFirstChild().asNormalizedText();

            if ("".equals(name) || name.contains("Souvenir Souvenir") || name.contains("Sealed Graffiti")) {
                continue;
            }


            String weapon = item.getAttribute("data-weapon");
            String collection = item.getAttribute("data-collection");
            String quality = item.getAttribute("data-quality");

            Double vn_price = !"0.00".equals(item.getAttribute("data-vn").trim()) ? Double.parseDouble(item.getAttribute("data-vn")) : null;
            Double bs_price = !"0.00".equals(item.getAttribute("data-bs").trim()) ? Double.parseDouble(item.getAttribute("data-bs")) : null;
            Double ww_price = !"0.00".equals(item.getAttribute("data-ww").trim()) ? Double.parseDouble(item.getAttribute("data-ww")) : null;
            Double ft_price = !"0.00".equals(item.getAttribute("data-ft").trim()) ? Double.parseDouble(item.getAttribute("data-ft")) : null;
            Double mw_price = !"0.00".equals(item.getAttribute("data-mw").trim()) ? Double.parseDouble(item.getAttribute("data-mw")) : null;
            Double fn_price = !"0.00".equals(item.getAttribute("data-fn").trim()) ? Double.parseDouble(item.getAttribute("data-fn")) : null;

            if (vn_price == null && fn_price == null && mw_price == null && ft_price == null && ww_price == null && bs_price == null) {
                continue;
            }

            if (name.contains("StatTrak")) {
                name = name.replace("StatTrak", "StatTrak\u2122");
            }

            if (name.contains("/")) {
                name = name.replace("/", "-");
            }

            //Knife
            if ("Covert".equals(quality) && (weapon.contains("Knife") || weapon.contains("Bayonet") || weapon.contains("Shadow Daggers") || weapon.contains("Karambit") || "".equals(weapon))) {
                name = "\u2605 " + name;
            }

            //Gloves
            if (name.contains("Gloves") || name.contains("Hand Wraps")) {
                name = "\u2605 " + name;
            }

            name = name.replaceAll(" {2}", " ");


            String[] infos = new String[4];
            if (vn_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name, infos);
            }
            if (bs_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Battle-Scarred)", infos);
            }
            if (ww_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Well-Worn)", infos);
            }
            if (ft_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Field-Tested)", infos);
            }
            if (mw_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Minimal Wear)", infos);
            }
            if (fn_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Factory New)", infos);
            }
        }

        String SQLinsert = "INSERT INTO steam.item_informations(name,weapon,collection,quality,name_without_exterior) "
                + "VALUES(?,?,?,?,?)";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {

            for (String key : map.keySet()) {
                pstmt.setString(1, key); //name
                pstmt.setString(2, map.get(key)[0]); //weapon
                pstmt.setString(3, map.get(key)[1]); //collection
                pstmt.setString(4, map.get(key)[2]); //quality
                pstmt.setString(5, map.get(key)[3]); //name_without_exterior
                pstmt.addBatch();
            }

            int[] updateCounts = pstmt.executeBatch();
            System.out.println(updateCounts.length + " were inserted!");

            connection.commit();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getLastSkinbaronId() throws Exception {
        String result;
        String sql = "with maxtimestamp as\n" +
                "(select Max(timestamp) t from steam.skinbaron_items si)\n" +
                "select id from steam.skinbaron_items\n" +
                "inner join maxtimestamp on \"timestamp\" = maxtimestamp.t";
        try (Connection connection = getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                result = rs.getString("id");
            } else {
                result = "";
            }
        }
        return result;
    }

    @Override
    public void deleteNonExistingSkinbaronItems(String ItemName, double price) throws Exception {
        executeDDL("DELETE FROM steam.skinbaron_items where name='" + ItemName + "' and price <= " + price);
    }

    //TODO \" entfernen
    @Override
    public void insertSoldSkinbaronItem(JsonNode payload) throws Exception {
        String classid = payload.get("classid").toString();
        String last_updated = payload.get("last_updated").toString();
        String instanceid = payload.get("instanceid").toString();
        String list_time = payload.get("list_time").toString();
        double price = Double.parseDouble(payload.get("price").toString());
        String assetid = payload.get("assetid").toString();
        String name = payload.get("name").toString();
        String txid = payload.get("txid").toString();
        double commission = Double.parseDouble(payload.get("commission").toString());
        String itemId = payload.get("id").toString();

        String sqlIinsert = "INSERT INTO steam.skinbaron_sold_items\n" +
                "(id, name, price,classid,last_updated,instanceid,list_time,assetid,txid,commission)\n" +
                "VALUES(?, ?, ?,?,?, ?, ?,?,?, ?);";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(sqlIinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, itemId);
            pstmt.setString(2, name);
            pstmt.setDouble(3, price);
            pstmt.setString(4, classid);
            pstmt.setString(5, last_updated);
            pstmt.setString(6, instanceid);
            pstmt.setString(7, list_time);
            pstmt.setString(8, assetid);
            pstmt.setString(9, txid);
            pstmt.setDouble(10, commission);

            pstmt.executeUpdate();
            connection.commit();
        }
    }

    @Override
    public String getLastSoldSkinbaronId() throws Exception {
        String result;
        String sql = "with maxtimestamp as\n" +
                "(select Max(timestamp) t from steam.skinbaron_sold_items si)\n" +
                "select id from steam.skinbaron_sold_items\n" +
                "inner join maxtimestamp on \"timestamp\" = maxtimestamp.t";
        try (Connection connection = getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            result = rs.getString("id");
        }
        return result;
    }

    @Override
    public void crawlWearValues() throws Exception {

        logger.info("Crawler WEAR values. This takes long!");

        Map<String, String[]> mapWears = new HashMap<>();

        int max_iteration = 20000;

        String SQLinsert = "INSERT INTO steam.item_wears(name,id,min_wear,max_wear) "
                + "VALUES(?,?,?,?)";

        try (Connection conn = getConnection()) {

            Collection<Integer> iterators = new HashSet<>();

            for (int i = 250; i <= max_iteration; i++) {
                iterators.add(i);
            }
            iterators.add(Integer.MAX_VALUE);

            //select all ids and excute the complement
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select id from steam.item_wears")) {

                while (rs.next()) {
                    iterators.remove(rs.getInt("id"));
                    System.out.println(rs.getInt("id"));
                }
            }

            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

            com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient(com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX);
            webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
            webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
            webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering

            int count = 0;
            for (Object i : iterators) {

                count++;
                try {
                    String url = "https://csgostash.com/skin/" + i;
                    com.gargoylesoftware.htmlunit.html.HtmlPage page = webClient.getPage(url);

                    Thread.sleep(1000);

                    String name = page.getTitleText().replace(" - CS:GO Stash", "");
                    List<com.gargoylesoftware.htmlunit.html.DomElement> Items_min = page.getByXPath("//*[contains(@class, 'marker-wrapper wear-min-value')]");
                    List<com.gargoylesoftware.htmlunit.html.DomElement> Items_max = page.getByXPath("//*[contains(@class, 'marker-wrapper wear-max-value')]");
                    List<com.gargoylesoftware.htmlunit.html.DomElement> Items_name = page.getByXPath("//*[contains(@class, 'img-responsive center-block main-skin-img margin-top-sm margin-bot-sm')]");

                    try {
                        String xml_min = Items_min.get(0).asXml();
                        String xml_max = Items_max.get(0).asXml();

                        Document document_min = new SAXReader().read(new StringReader(xml_min));
                        String min = document_min.valueOf("/div/@data-wearmin");

                        Document document_max = new SAXReader().read(new StringReader(xml_max));
                        String max = document_max.valueOf("/div/@data-wearmax");

                        //System.out.println(name + " " + min + " " + max);

                        String[] infos = new String[3];
                        //put in map
                        infos[0] = "" + i;
                        infos[1] = min;
                        infos[2] = max;
                        mapWears.put(name, infos);
                    } catch (IndexOutOfBoundsException e) {
                        String[] infos = new String[3];
                        //put in map
                        infos[0] = "" + i;
                        infos[1] = "0";
                        infos[2] = "1";
                        mapWears.put(name, infos);
                    }
                } catch (FailingHttpStatusCodeException e) {
                    e.getStatusCode();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
                    System.out.println(i);
                    if (count % 100 == 0 || (i).equals(Integer.MAX_VALUE)) {
                        count = 0;
                        for (String key : mapWears.keySet()) {
                            pstmt.setString(1, key); //name
                            pstmt.setInt(2, Integer.parseInt(mapWears.get(key)[0])); //i
                            pstmt.setDouble(3, Double.parseDouble(mapWears.get(key)[1])); //min
                            pstmt.setDouble(4, Double.parseDouble(mapWears.get(key)[2])); //max
                            pstmt.addBatch();
                        }
                        mapWears.clear();

                        int[] updateCounts = pstmt.executeBatch();
                        int amountInserts = IntStream.of(updateCounts).sum();
                        if (amountInserts != 0) {
                            logger.info(amountInserts + " items were inserted!");
                        }
                        conn.commit();
                    }

                }
            }
        }

    }

    @Override
    public void insertOverviewRow(double steam_balance, double steam_sales_value, double skinbaron_balance) throws Exception {

        double smurf_inv_value;
        double skinbaron_open_sale_wert;
        double steam_inv_value;
        double skinbaron_inv_value;

        try (Connection conn = getConnection(); Statement stmt2 = conn.createStatement(); ResultSet rs2 = stmt2.executeQuery("with smurf as \n" +
                "(select round(cast(x.smurf_inv_wert as numeric),2) as smurf_inv_value from (select t.smurf_inv_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as smurf_inv_wert\n" +
                "\tfrom steam.inventory_with_prices si where inv_type = 'smurf' ) t) x),\n" +
                "skinbaron_inv as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as skinbaron_inv_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam.inventory_with_prices si where inv_type = 'skinbaron' ) t) w),\n" +
                "steam_inv as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as steam_inv_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam.inventory_with_prices si where inv_type = 'steam' or inv_type like 'storage%' ) t) w),\n" +
                "skinbaron_open_sales as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as skinbaron_open_sales_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam.inventory_with_prices si where inv_type = '" + Constants.INV_TYPE_SKINBARON_SALES + "' ) t) w)\n" +
                "select smurf.*,skinbaron_open_sales.*,steam_inv.*,skinbaron_inv.* from smurf\n" +
                "inner join skinbaron_inv on 1=1\n" +
                "inner join steam_inv on 1=1\n" +
                "inner join skinbaron_open_sales on 1=1")) {

            rs2.next();

            smurf_inv_value = rs2.getDouble("smurf_inv_value");
            logger.info("Smurf Inventory Value: " + smurf_inv_value);
            skinbaron_open_sale_wert = rs2.getDouble("skinbaron_open_sales_value");
            logger.info("Skinbaron open Sales Value: " + skinbaron_open_sale_wert);
            steam_inv_value = rs2.getDouble("steam_inv_value");
            logger.info("Steam Inventory Value: " + steam_inv_value);
            skinbaron_inv_value = rs2.getDouble("skinbaron_inv_value");
            logger.info("Skinbaron Inventory Value: " + skinbaron_inv_value);
        }

        double sum_rare_items;

        try (Connection conn = getConnection(); Statement stmt3 = conn.createStatement(); ResultSet rs3 = stmt3.executeQuery("select sum(zusatz_wert) as sum_rare_items from steam.rare_skins;")) {
            rs3.next();
            sum_rare_items = rs3.getDouble("sum_rare_items");
        }

        executeDDL("delete from steam.overview where \"DATE\"=current_date");

        String sql = "INSERT INTO steam.overview(smurf_inv_value,skinbaron_open_sales,steam_inv_value,skinbaron_inv_value,rare_items_value,steam_balance,steam_open_sales,skinbaron_balance) "
                + "VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, smurf_inv_value);
            pstmt.setDouble(2, skinbaron_open_sale_wert);
            pstmt.setDouble(3, steam_inv_value);
            pstmt.setDouble(4, skinbaron_inv_value);
            pstmt.setDouble(5, sum_rare_items);
            pstmt.setDouble(6, steam_balance);
            pstmt.setDouble(7, steam_sales_value);
            pstmt.setDouble(8, skinbaron_balance);

            pstmt.execute();
            conn.commit();
        }
    }

    @Override
    public void deleteSkinbaronId(String id) throws Exception {
        executeDDL("delete from steam.skinbaron_items where id='" + id + "'");
    }

    //TODO take raw data
    /**
     * only 10 Inserts per Item
     * Since a name doesn't make the items unique, the avg/etc will be computed on DB
     *
     * @param json Skinbaron response
     */
    @Override
    public void insertNewestSales(String json) throws Exception {

        String sql = "Insert into steam.skinbaron_newest_sold_items_tmp (name,price,wear,datesold,doppler_phase) values (?,?,?,?,?)";

        JSONArray array = (new JSONObject(json)).getJSONArray("newestSales30Days");
        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Object o : array) {
                if (o instanceof JSONObject) {
                    pstmt.setString(1, ((JSONObject) o).getString("itemName"));
                    pstmt.setDouble(2, ((JSONObject) o).getDouble("price"));
                    pstmt.setDouble(3, ((JSONObject) o).getDouble("wear"));
                    pstmt.setString(4, ((JSONObject) o).getString("dateSold"));
                    if (((JSONObject) o).has("dopplerPhase")) {
                        pstmt.setString(5, ((JSONObject) o).getString("dopplerPhase"));
                    } else {
                        pstmt.setString(5, null);
                    }
                    pstmt.addBatch();
                }
            }

            int[] updateCounts = pstmt.executeBatch();
            int amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted!");
            }
            connection.commit();
        }
        //TODO Upsert
        executeDDL("INSERT INTO steam.skinbaron_newest_sold_items (\"name\", doppler_phase, avg_price, min_price, max_price, amount, insert_date)\n" +
                "select\n" +
                "\ts.\"name\",\n" +
                "\ts.doppler_phase,\n" +
                "\tROUND(avg(s.price),2) as avg_price,\n" +
                "\tmin(s.price) as min_price,\n" +
                "\tmax(s.price) as max_price,\n" +
                "\tcount(*) as amount,\n" +
                "\tcurrent_date as insert_date\n" +
                "from steam.skinbaron_newest_sold_items_tmp s\n" +
                "group by s.\"name\" ,s.doppler_phase;");
        executeDDL("TRUNCATE TABLE steam.skinbaron_newest_sold_items_tmp");
        executeDDL("delete from steam.skinbaron_newest_sold_items where insert_date != CURRENT_DATE");
    }

    @Override
    public void insertSkinbaronSales(String id, String classid, String last_updated, String list_time, double price, String assetid, String name, String contextid) throws Exception {

        String sql = "INSERT INTO steam.skinbaron_sales (id, name, classid, last_updated, list_time, price, assetid,contextid) VALUES(?,?,?,?,?,?,?,?);";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, classid);
            pstmt.setString(4, last_updated);
            pstmt.setString(5, list_time);
            pstmt.setDouble(6, price);
            pstmt.setString(7, assetid);
            pstmt.setString(8, contextid);

            pstmt.execute();

            connection.commit();
        }

        logger.info("1 item was inserted into table skinbaron_sales!");
    }

    @Override
    public void deleteSkinbaronSalesTable() throws Exception {
        executeDDL("TRUNCATE TABLE steam.skinbaron_sales;");
    }

    @Override
    public void addSkinbaronInventoryItems(JsonNode payload) throws SQLException {

        executeDDL("TRUNCATE TABLE steam.skinbaron_inventory;");
        String sql = "INSERT INTO steam.skinbaron_inventory (id, name, tradeLockHourseLeft) VALUES(?,?,?);";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            JSONArray array = new JSONArray(payload.toString());

            logger.debug("Length of Input JSON Array: " + array.length());

            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);

                logger.debug("JSON Object with index "+i+" is \n" + o);
                String name = o.getString("marketHashName");
                int id = o.getInt("id");
                int tradeLockHoursLeft = 0;
                if (o.has("tradeLockHoursLeft")) {
                    tradeLockHoursLeft = o.getInt("tradeLockHoursLeft");
                }
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setInt(3, tradeLockHoursLeft);
                pstmt.addBatch();

            }

            int amountInserts;
            int[] updateCounts = pstmt.executeBatch();
            amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted into skinbaron inventory!");
            }
            connection.commit();
        }
    }
}
