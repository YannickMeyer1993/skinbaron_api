package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.AbstractNode;

import java.io.IOException;
import java.io.StringReader;
import java.math.RoundingMode;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.company.common.readPasswordFromFile;

public class BuffCrawler {

    private static double conversionFromRMBtoEUR;

    static {
        try {
            conversionFromRMBtoEUR = CurrencyConverter.getRMBinEURO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, SQLException, DocumentException, InterruptedException {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        String SQLUpsert = "WITH\n" +
                "    to_be_upserted (id,price_euro,timestamp,success,name) AS (\n" +
                "        VALUES\n" +
                "            (?,?,?,?,?,?)\n" +
                "    ),\n" +
                "    updated AS (\n" +
                "        UPDATE\n" +
                "            steam_item_sale.buff_item_prices s\n" +
                "        SET\n" +
                "            price = to_be_upserted.price::numeric\n" +
                "            ,timestamp = to_be_upserted.price::numeric\n" +
                "        FROM\n" +
                "            to_be_upserted\n" +
                "        WHERE\n" +
                "            s.id = to_be_upserted.id\n" +
                "        RETURNING s.id\n" +
                "    )\n" +
                "INSERT INTO steam_item_sale.buff_item_prices\n" +
                "    SELECT * FROM to_be_upserted\n" +
                "    WHERE id NOT IN (SELECT id FROM updated);";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select id from (select\n" +
                "\trank() over ( partition by weapon_name order by timestamp ) as ranking,\n" +
                "\tid,\n" +
                "\ttimestamp,\n" +
                "\tweapon_name\n" +
                "from\n" +
                "\tsteam_item_sale.buff_item_prices bip\n" +
                "where DATE_PART('day', now()-timestamp )>1 and name not like 'Souvenir%' and name not like 'Sealed Graffiti%'\n" +
                "group by\n" +
                "\tweapon_name,id\n" +
                "order by\n" +
                "\tweapon_name) sub\n" +
                "where sub.ranking =1\n" +
                "order by id");

        while (rs.next()) {
            getBuffItem(conn, 38586);//rs.getInt("id"));
            break;
        }

        rs.close();
        stmt.close();
        conn.close();

    }

    private static void getBuffItem(Connection conn, int id) throws IOException, InterruptedException, DocumentException {

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        List<String[]> result = new ArrayList<String[]>();

        String url = "https://buff.163.com/market/goods?goods_id=" + id;

        System.out.println(url);

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(1000);

        List<com.gargoylesoftware.htmlunit.html.DomElement> Items = page.getByXPath("//*[contains(@class, 'relative-goods')]");

        for (DomNode element : Items.get(0).getChildNodes()){
            //System.out.println(element.asXml().trim());
            String goodsId = null;

            String item_xml =  element.asXml().trim();

            Document document = null;
            try {
                document = new SAXReader().read(new StringReader(item_xml));
                goodsId = document.valueOf("a/data-goods-id");
                String price_rmb = document.valueOf("/a/.").replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").split("¥")[1];

                //goodsId = goodsId!=null&&!"".equals(goodsId)?goodsId:""+id;

                System.out.println(goodsId);
                System.out.println(price_rmb);
            } catch (DocumentException e){
                //Do nothing
            }
        }
    }
}

