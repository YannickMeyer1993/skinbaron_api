package com.company.dataaccessobject;

import com.company.model.*;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.IntStream;

import static com.company.common.PostgresHelper.*;

@Repository("postgres")
public class PostgresDAO implements ItemDAO {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(PostgresDAO.class);
    String resourcePath = "src/main/resources/PostgresDAO/";

    public PostgresDAO() throws Exception {
        init();
    }

    @Override
    public void init() throws Exception {
        executeDDLfromPath(resourcePath + "0_schema.sql");
        executeDDLfromPath(resourcePath + "1_table_skinbaron_items.sql");
        executeDDLfromPath(resourcePath + "1_table_steam_item_prices.sql");
        executeDDLfromPath(resourcePath + "1_steam_iteration.sql");
        executeDDLfromPath(resourcePath + "1_table_inventory.sql");
        executeDDLfromPath(resourcePath + "1_table_item_informations.sql");


        if (checkIfResultsetIsEmpty("select * from steam.item_informations")) {
            crawlItemInformations();
            crawlWearValues();
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
                "    to_be_upserted (id,name,price,stickers,wear) AS (\n" +
                "        VALUES\n" +
                "            (?,?,?,?,?)\n" +
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
                "INSERT INTO steam.skinbaron_items\n" +
                "    SELECT * FROM to_be_upserted\n" +
                "    WHERE id NOT IN (SELECT id FROM updated);";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            for (SkinbaronItem item : items) {
                pstmt.setString(1, item.getId());
                pstmt.setString(2, item.getName());
                pstmt.setDouble(3, item.getPrice().getValue());
                pstmt.setString(4, item.getStickers());
                pstmt.setDouble(5, item.getWear());
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

        return (amountInserts==1?last_id:"");
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
    public String[] getItemsToBuy() {
        return new String[0];
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
    public void addInventoryItem(InventoryItem item) throws Exception {

        String sql = "select name from steam_item_sale.item_informations where name =?;";
        String SQLInsert = "INSERT INTO steam.inventory(inv_type,name,still_there) "
                + "VALUES(?,?,true)";

        try (Connection connection = getConnection();
             PreparedStatement pstmt1 = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmt2 = connection.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt1.setString(1, item.getItemName());

            ResultSet rs = pstmt1.executeQuery();
            if (!rs.next()) {
                return;
            }

            pstmt2.setString(1, item.getInventoryType());
            pstmt2.setString(2, item.getItemName());
            pstmt2.execute();
            connection.commit();
        }

        logger.info("Item \""+ item.getItemName() +"\" was inserted to inventory.");
    }

    @Override
    public Item getItem(String ItemName) {
        ItemCollection collection = new ItemCollection("",false);
        return new Item(ItemName, collection);
    }

    @Override
    public void deleteInventoryItems() throws Exception {
        executeDDL("TRUNCATE TABLE steam.inventory");
    }

    @Override
    public void cleanUp() throws Exception {
        executeDDLfromPath(resourcePath+"cleanUp.sql");
    }

    @Override
    public void crawlItemInformations() throws Exception {

        Map<String,  String[]> map = new HashMap<>();

        executeDDL("TRUNCATE TABLE steam_item_sale.item_informations;");

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

            if ("".equals(name) || name.contains("Souvenir Souvenir") || name.contains("Sealed Graffiti"))
            {
                continue;
            }


            String weapon=item.getAttribute("data-weapon");
            String collection=item.getAttribute("data-collection");
            String quality=item.getAttribute("data-quality");

            Double vn_price = !"0.00".equals(item.getAttribute("data-vn").trim()) ? Double.parseDouble(item.getAttribute("data-vn")) : null;
            Double bs_price = !"0.00".equals(item.getAttribute("data-bs").trim()) ? Double.parseDouble(item.getAttribute("data-bs")) : null;
            Double ww_price = !"0.00".equals(item.getAttribute("data-ww").trim()) ? Double.parseDouble(item.getAttribute("data-ww")) : null;
            Double ft_price = !"0.00".equals(item.getAttribute("data-ft").trim()) ? Double.parseDouble(item.getAttribute("data-ft")) : null;
            Double mw_price = !"0.00".equals(item.getAttribute("data-mw").trim()) ? Double.parseDouble(item.getAttribute("data-mw")) : null;
            Double fn_price = !"0.00".equals(item.getAttribute("data-fn").trim()) ? Double.parseDouble(item.getAttribute("data-fn")) : null;

            if (vn_price == null && fn_price == null && mw_price == null && ft_price == null && ww_price == null && bs_price == null){
                continue;
            }

            if (name.contains("StatTrak"))
            {
                name = name.replace("StatTrak","StatTrak\u2122");
            }

            if (name.contains("/"))
            {
                name = name.replace("/","-");
            }

            //Knife
            if ("Covert".equals(quality)&&(weapon.contains("Knife") || weapon.contains("Bayonet") || weapon.contains("Shadow Daggers") || weapon.contains("Karambit") ||"".equals(weapon)))
            {
                name = "\u2605 "+name;
            }

            //Gloves
            if (name.contains("Gloves") || name.contains("Hand Wraps"))
            {
                name = "\u2605 "+name;
            }

            name = name.replaceAll(" {2}"," ");


            String[] infos = new String[4];
            if (vn_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ","");
                map.put(name,infos);
            }
            if (bs_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ","");
                map.put(name+" (Battle-Scarred)",infos);
            }
            if (ww_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ","");
                map.put(name+" (Well-Worn)",infos);
            }
            if (ft_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ","");
                map.put(name+" (Field-Tested)",infos);
            }
            if (mw_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ","");
                map.put(name+" (Minimal Wear)",infos);
            }
            if (fn_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ","");
                map.put(name+" (Factory New)",infos);
            }
        }

        String SQLinsert = "INSERT INTO steam.item_informations(name,weapon,collection,quality,name_without_exterior) "
                + "VALUES(?,?,?,?,?)";

        try (Connection connection = getConnection();PreparedStatement pstmt = connection.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {

            for (String key : map.keySet()){
                pstmt.setString(1,key); //name
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
    public void crawlWearValues() throws Exception {

        Map<String,  String[]> mapWears = new HashMap<>();

        int max_iteration = 20000;

        String SQLinsert = "INSERT INTO steam_item_sale.item_wears(name,id,min_wear,max_wear) "
                + "VALUES(?,?,?,?)";

        try(Connection conn = getConnection()) {

            Collection<Integer> iterators = new HashSet<>();

            for (int i = 250; i <= max_iteration; i++) {
                iterators.add(i);
            }
            iterators.add(Integer.MAX_VALUE);

            //select all ids and excute the complement
            try(Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery("select id from steam_item_sale.item_wears")) {

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
                        System.out.println(updateCounts.length + " were inserted!");
                        conn.commit();
                    }

                }
            }
        }

    }
}
