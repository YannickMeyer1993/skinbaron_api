package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.applet2.AppletParameters;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static com.company.common.printBatchUpdateException;
import static com.company.common.readPasswordFromFile;

public class Deployment {

    private static Map<String,  String[]> map = new HashMap<String, String[]>();

    public static void crawItemInformations() throws IOException, InterruptedException, SQLException {

        String connstring = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(connstring, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        Statement st = conn.createStatement();
        st.execute("TRUNCATE TABLE steam_item_sale.item_informations2");
        st.close();

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

            if ("".equals(name) || name.indexOf("Souvenir Souvenir")!=-1 || name.indexOf("Sealed Graffiti")!=-1)
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

            if (name.indexOf("StatTrak")!=-1)
            {
                name = name.replace("StatTrak","StatTrak\u2122");
            }

            if (name.indexOf("/")!=-1)
            {
                name = name.replace("/","-");
            }

            //Knife
            if ("Covert".equals(quality)&&(weapon.indexOf("Knife")!=-1||weapon.indexOf("Bayonet")!=-1||weapon.indexOf("Shadow Daggers")!=-1||weapon.indexOf("Karambit")!=-1||"".equals(weapon)))
            {
                name = "\u2605 "+name;
            }

            //Gloves
            if (name.indexOf("Gloves")!=-1 || name.indexOf("Hand Wraps")!=-1)
            {
                name = "\u2605 "+name;
            }

            name = name.replaceAll("  "," ");

            //Denormalize

            //init Map
            String[] infos = new String[3];


            //insert into Map

            if (vn_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                map.put(name,infos);
            }
            if (bs_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                map.put(name+" (Battle-Scarred)",infos);
            }
            if (ww_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                map.put(name+" (Well-Worn)",infos);
            }
            if (ft_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                map.put(name+" (Field-Tested)",infos);
            }
            if (mw_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                map.put(name+" (Minimal Wear)",infos);
            }
            if (fn_price!=null){
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                map.put(name+" (Factory New)",infos);
            }
        }

        //Batch Load with Map
        String SQLinsert = "INSERT INTO steam_item_sale.item_informations2(name,weapon,collection,quality) "
                + "VALUES(?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {

            for (String key : map.keySet()){
                pstmt.setString(1,key); //name
                pstmt.setString(2, map.get(key)[0]); //weapon
                pstmt.setString(3, map.get(key)[1]); //collection
                pstmt.setString(4, map.get(key)[2]); //quality
                pstmt.addBatch();
            }

            int[] updateCounts = pstmt.executeBatch();
            System.out.println(updateCounts.length + " were inserted!");

            conn.commit();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}
