package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import static com.company.common.*;

public class Deployment {

    private static Map<String,  String[]> map = new HashMap<>();
    private static Map<String,  String[]> mapWears = new HashMap<>();
    private static int max_iteration = 20000;

    public static void crawlItemInformations() throws IOException, InterruptedException, SQLException {

        Connection conn = getConnection();

        Statement st = conn.createStatement();
        st.execute("TRUNCATE TABLE steam_item_sale.item_informations");
        st.close();

        String url = "https://csgo.exchange/prices/";

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

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

            name = name.replaceAll("  "," ");

            //Denormalize

            //init element
            String[] infos = new String[4];


            //insert into Map

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

        //Batch Load with Map
        String SQLinsert = "INSERT INTO steam_item_sale.item_informations(name,weapon,collection,quality,name_without_exterior) "
                + "VALUES(?,?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {

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

            conn.commit();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static void crawlWearValues() throws IOException, SQLException, InterruptedException, DocumentException {
        String SQLinsert = "INSERT INTO steam_item_sale.item_wears(name,id,min_wear,max_wear) "
                + "VALUES(?,?,?,?)";

        Connection conn = getConnection();

        //TODO
        Collection<Integer> iterators = new HashSet<>();

        for (int i=250;i<=max_iteration;i++){
            iterators.add(i);
        }
        iterators.add(Integer.MAX_VALUE);

        //select all ids and excute the complement
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select id from steam_item_sale.item_wears");

        while(rs.next()){
            iterators.remove(rs.getInt("id"));
            System.out.println(rs.getInt("id"));
        }

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

        com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient(com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore (1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering

        int count = 0;
        for (Object i:iterators){

            count++;
            try{
                String url = "https://csgostash.com/skin/"+ i;
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
                    infos[0] = ""+ i;
                    infos[1] = min;
                    infos[2] = max;
                    mapWears.put(name,infos);
                }
                catch (IndexOutOfBoundsException e){
                    String[] infos = new String[3];
                    //put in map
                    infos[0] = ""+ i;
                    infos[1] = "0";
                    infos[2] = "1";
                    mapWears.put(name,infos);
                }
            }
            catch (FailingHttpStatusCodeException e){
                e.getStatusCode();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
                System.out.println(i);
                if (count % 100 == 0 || ((Integer)i).equals(Integer.MAX_VALUE)) {
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

        conn.close();

    }
}
