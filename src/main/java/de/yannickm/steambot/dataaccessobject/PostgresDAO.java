package de.yannickm.steambot.dataaccessobject;

import de.yannickm.steambot.common.Constants;
import de.yannickm.steambot.common.PostgresExecutor;
import de.yannickm.steambot.model.SkinbaronItem;
import de.yannickm.steambot.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.stream.IntStream;

import static de.yannickm.steambot.common.PostgresExecutor.*;

@Repository("postgres")
public class PostgresDAO implements ItemDAO {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PostgresDAO.class);
    final String resourcePath = "src/main/resources/PostgresDAO/";

    public PostgresDAO() throws Exception {
        executeDDLsfromDirectory(resourcePath + "init/");

        if (checkIfResultsetIsEmpty("select * from steam.item_informations")) {
            insertItemInformations();
        }
        //crawlWearValues();
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

    public void addSteamPrices(JsonNode payload) throws Exception {
        String SQLUpsert = "WITH\n" +
                "    to_be_upserted (name, quantity, price_euro, start_index) AS (\n" +
                "        VALUES\n" +
                "            (?,?,?,?)\n" +
                "    ),\n" +
                "    updated AS (\n" +
                "        UPDATE\n" +
                "            steam.steam_prices s\n" +
                "        SET\n" +
                "            price_euro = to_be_upserted.price_euro::numeric\n" +
                "        FROM\n" +
                "            to_be_upserted\n" +
                "        WHERE\n" +
                "            s.name = to_be_upserted.name and s.\"date\"=CURRENT_DATE\n" +
                "        RETURNING s.name\n" +
                "    )\n" +
                "INSERT INTO steam.steam_prices (name, quantity, price_euro, start_index)\n" +
                "    SELECT name, quantity, price_euro, start_index FROM to_be_upserted\n" +
                "    WHERE name NOT IN (SELECT name FROM updated);";


        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            for (Object item : new JSONArray(payload.toString())) {
                if (item instanceof JSONObject) {
                    pstmt.setString(1, ((JSONObject) item).getString("itemname"));
                    pstmt.setInt(2, ((JSONObject) item).getInt("quantity"));
                    pstmt.setDouble(3, ((JSONObject) item).getDouble("price"));
                    pstmt.setInt(4,((JSONObject) item).getInt("start_parameter"));
                }
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
        String query = "select steam_price_is_new,skinbaron_price,steam_price, name,skinbaron_ids,buff_price,buff_price_is_new,has_exterior,buff_id from steam.skinbaron_buyable_items order by rati desc";

        org.json.JSONArray array = new JSONArray();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                JSONObject JsonObject = new JSONObject();
                JsonObject.put("steam_price_is_new", rs.getBoolean("steam_price_is_new"));
                JsonObject.put("skinbaron_ids", rs.getString("skinbaron_ids"));
                JsonObject.put("name", rs.getString("name"));
                JsonObject.put("skinbaron_price", rs.getDouble("skinbaron_price"));
                JsonObject.put("steam_price", rs.getDouble("steam_price"));
                JsonObject.put("buff_price_is_new", rs.getBoolean("buff_price_is_new"));
                JsonObject.put("buff_price", rs.getDouble("buff_price"));
                JsonObject.put("has_exterior",rs.getBoolean("has_exterior"));
                JsonObject.put("buff_id",rs.getInt("buff_id"));

                array.put(JsonObject);
            }
            System.out.println(array);
        }
        return array;
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
    public void insertBuffPrices(JSONArray array) throws Exception {
        String SQLUpsert = "INSERT INTO steam.buff_prices (id,price_euro,name,has_exterior)\n" +
                "    VALUES (?,?,?,?);";

        try (Connection connection = getConnection();PreparedStatement pstmt = connection.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            for (int i=0;i<array.length();i++) {
                JSONObject o = (JSONObject) array.get(i);

                pstmt.setInt(1, o.getInt("id"));
                pstmt.setDouble(2, o.getDouble("price_euro"));
                if (o.has("name")) {
                    pstmt.setString(3,o.getString("name"));
                } else {
                    pstmt.setString(3,null);
                }
                pstmt.setBoolean(4,o.getBoolean("has_exterior"));

                pstmt.addBatch();
            }

            int amountInserts;
            int[] updateCounts = pstmt.executeBatch();
            amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted!");
            }

            connection.commit();
        }

        PostgresExecutor.executeDDL("update steam.buff_prices s\n" +
                "set \"name\" = t.\"name\"\n" +
                "from (select distinct id,\"name\" from steam.buff_prices where \"name\" is not null) t\n" +
                "where s.\"name\" is null and s.id =t.id;");

    }

    @Override
    public String getBuffIds() throws SQLException {
        JSONArray result = new JSONArray();

        try(Connection connection = getConnection();Statement st = connection.createStatement();ResultSet rs = st.executeQuery("select b.* from steam.buff_current_prices b\n" +
                "inner join steam.item_informations ii using(name)\n" +
                "where \"name\" not like '★%' and DATE(insert_timestamp) != current_date \n" +
                "order by insert_timestamp desc;")) {
            while (rs.next()) {
                JSONObject o = new JSONObject();
                o.put("id",rs.getInt("id"));
                o.put("has_exterior",rs.getBoolean("has_exterior"));
                o.put("name",rs.getString("name"));
                result.put(o);
            }
        }
        return result.toString();
    }

    @Override
    public void insertCollections() throws Exception {
        executeDDL("truncate table steam.collections");

        List<String> list = new ArrayList<>();

        String url = "https://csgo.exchange/collection/";
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(1000);

        List<DomElement> Items = page.getByXPath("//*[contains(@class, 'vItem')]");

        for (DomElement item : Items) {
            String name = item.getAttribute("data-custom");
            list.add(name);
        }

        String sql = "insert into steam.collections (name) values (?);";
        try (Connection connection = getConnection();PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (String s : list) {
                pstmt.setString(1, s);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            connection.commit();
        }
    }

    @Override
    public void insertPriceList(JsonNode payload) throws Exception {
        executeDDL("truncate table steam.skinbaron_pricelist");

        String sql = "Insert into steam.skinbaron_pricelist (name,price,dopplerphase) values (?,?,?)";

        JSONArray array = new JSONArray(payload.get("map").toString());
        //logger.info(String.valueOf(array.length()));

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Object o : array) {
                if (o instanceof JSONObject) {
                    if (!((JSONObject) o).has("marketHashName")) {
                        //logger.info(((JSONObject) o).toString());
                        continue;
                    }
                    pstmt.setString(1, ((JSONObject) o).getString("marketHashName"));
                    pstmt.setDouble(2, ((JSONObject) o).getDouble("lowestPrice"));
                    if (((JSONObject) o).has("dopplerClassName")) {
                        pstmt.setString(3, ((JSONObject) o).getString("dopplerClassName"));
                    } else {
                        pstmt.setString(3, null);
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
    }

    @Override
    public void cleanUp() throws Exception {
        executeDDLfromPath(resourcePath + "cleanUp.sql");
    }

    private void insertItemInformations() throws Exception {

        Map<String, String[]> map = crawlItemsFromCsgoExchange();

        executeDDL("TRUNCATE TABLE steam.item_informations;");

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

    @Override
    public void insertSoldSkinbaronItem(JsonNode payload) throws Exception {
        String classid = payload.get("classid").asText();
        String last_updated = payload.get("last_updated").asText();
        String instanceid = payload.get("instanceid").asText();
        String list_time = payload.get("list_time").asText();
        double price = Double.parseDouble(payload.get("price").asText());
        String assetid = payload.get("assetid").asText();
        String name = payload.get("name").asText();
        String txid = payload.get("txid").asText();
        double commission = Double.parseDouble(payload.get("commission").asText());
        String itemId = payload.get("id").asText();

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

    private ArrayList<Integer> getItemsWithMissingWears() throws Exception {
        int max_iteration = 20000;
        ArrayList<Integer> iterators;

        try (Connection conn = getConnection()) {
            iterators = new ArrayList<>();

            for (int i = 250; i <= max_iteration; i++) {
                iterators.add(i);
            }
            //iterators.add(Integer.MAX_VALUE);

            //select all ids and execute the complement
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select id from steam.item_wears")) {
                while (rs.next()) {
                    iterators.remove(iterators.indexOf(rs.getInt("id")));
                }
            }
        }
        return iterators;
    }

    private void insertWearValues(Map<String, String[]> mapWears) throws Exception {
        String SQLinsert = "INSERT INTO steam.item_wears(name,id,min_wear,max_wear) "
                + "VALUES(?,?,?,?)";

        try (Connection conn = getConnection();PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {

            for (String key : mapWears.keySet()) {
                pstmt.setString(1, key); //name
                pstmt.setInt(2, Integer.parseInt(mapWears.get(key)[0])); //i
                pstmt.setDouble(3, Double.parseDouble(mapWears.get(key)[1])); //min
                pstmt.setDouble(4, Double.parseDouble(mapWears.get(key)[2])); //max
                pstmt.addBatch();
            }

            int[] updateCounts = pstmt.executeBatch();
            int amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted!");
            }
            conn.commit();
        }
    }

    private void crawlWearValues() throws Exception {
        while (getItemsWithMissingWears().size() > 0) {
            insertWearValues(crawlWearValuesFromCsgoStash(getItemsWithMissingWears(), 10));
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
                "\tfrom steam.inventory_current_prices si where inv_type = 'smurf' ) t) x),\n" +
                "skinbaron_inv as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as skinbaron_inv_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam.inventory_current_prices si where inv_type = 'skinbaron' ) t) w),\n" +
                "steam_inv as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as steam_inv_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam.inventory_current_prices si where inv_type = 'steam' or inv_type like 'storage%' ) t) w),\n" +
                "skinbaron_open_sales as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as skinbaron_open_sales_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam.inventory_current_prices si where inv_type = '" + Constants.INV_TYPE_SKINBARON_SALES + "' ) t) w)\n" +
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

    /**
     * only 10 Inserts per Item
     * Since a name doesn't make the items unique, the avg/etc will be computed on DB
     *
     * @param json Skinbaron response
     */
    @Override
    public void insertNewestSales(String json) throws Exception {

        String sql = "Insert into steam.skinbaron_newest_sold_items (name,price,wear,datesold,doppler_phase) values (?,?,?,?,?)";

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
    }

    @Override
    public void insertSkinbaronSales(JsonNode payload) throws Exception {

        String sql = "INSERT INTO steam.skinbaron_sales (id, name, classid, last_updated, list_time, price, assetid,contextid) VALUES(?,?,?,?,?,?,?,?);";

        executeDDL("truncate table steam.skinbaron_sales;");

        JSONArray resultArray = new JSONArray(payload.toString());
        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject jObject = resultArray.getJSONObject(i);
                logger.info(jObject.toString());

                String id = jObject.getString("id");
                String classid = jObject.getString("classid");
                int last_updated = jObject.getInt("last_updated");
                int list_time = jObject.getInt("list_time");
                double price = jObject.getDouble("price");
                String assetid = jObject.getString("assetid");
                String name = jObject.getString("name");
                String contextid = null;
                if (jObject.has("contextid")) {
                    contextid = jObject.getString("contextid");
                }

                pstmt.setString(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, classid);
                pstmt.setInt(4, last_updated);
                pstmt.setInt(5, list_time);
                pstmt.setDouble(6, price);
                pstmt.setString(7, assetid);
                pstmt.setString(8, contextid);

                pstmt.addBatch();
            }

            int amountInserts;
            int[] updateCounts = pstmt.executeBatch();
            amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted into skinbaron sales table!");
            }
            connection.commit();
        }
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
