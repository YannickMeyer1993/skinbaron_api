package com.company.postgres.buff;

import com.company.old.CurrencyConverter;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;


import java.io.IOException;
import java.io.StringReader;
import java.math.RoundingMode;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.company.common.CurrencyHelper.getConversionRateToEuro;
import static com.company.old.helper.getConnection;

public class BuffCrawler {

    private static Double conversionRateRMBinEUR;

    public static void main(String[] args) throws Exception {

        conversionRateRMBinEUR = getConversionRateToEuro("RMB");

        String query = "select id from (select\n" +
                "\trank() over ( partition by weapon_name order by timestamp ) as ranking,\n" +
                "\tid,\n" +
                "\ttimestamp,\n" +
                "\tweapon_name\n" +
                "from\n" +
                "\tsteam_item_sale.buff_item_prices bip\n" +
                "where DATE_PART('day', now()-timestamp )>1 and name not like 'Souvenir%' and name not like 'Sealed Graffiti%' and price_euro<>0\n" +
                "group by\n" +
                "\tweapon_name,id,bip.timestamp\n" +
                "order by\n" +
                "\tweapon_name) sub\n" +
                "where sub.ranking =1\n" +
                "order by timestamp";

        try(Connection conn = getConnection();Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                try {
                    getBuffItem(conn, rs.getInt("id"));
                } catch (IndexOutOfBoundsException e) {
                    getBuffItemNoExterior(conn, rs.getInt("id"));
                }
            }
        }
    }

    static void getBuffItemNoExterior(Connection conn, int id) throws SQLException, InterruptedException, IOException, DocumentException {

        String SQLUpsert = "WITH\n" +
                "    to_be_upserted (id,price_euro,timestamp,has_enterior,name) AS (\n" +
                "        VALUES\n" +
                "            (?,?,current_timestamp,?,?)\n" +
                "    ),\n" +
                "    updated AS (\n" +
                "        UPDATE\n" +
                "            steam_item_sale.buff_item_prices s\n" +
                "        SET\n" +
                "            price_EURO = to_be_upserted.price_EURO::numeric\n" +
                "            ,timestamp = to_be_upserted.timestamp\n" +
                "        FROM\n" +
                "            to_be_upserted\n" +
                "        WHERE\n" +
                "            s.id = to_be_upserted.id\n" +
                "        RETURNING s.id\n" +
                "    )\n" +
                "INSERT INTO steam_item_sale.buff_item_prices\n" +
                "    SELECT * FROM to_be_upserted\n" +
                "    WHERE id NOT IN (SELECT id FROM updated);";

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        List<String[]> result = new ArrayList<>();

        String url = "https://buff.163.com/market/goods?goods_id=" + id;

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(4000); //needed to load page

        String hash_name = null;
        List<DomElement> names = page.getByXPath("//*[contains(@class, 'cru-goods')]");
        for (DomElement name : names) {
            Document name_xml = new SAXReader().read(new StringReader(name.asXml()));
            if (name_xml.valueOf("span") == null) {
                continue;
            }
            hash_name = name_xml.valueOf("span").trim();
        }

        System.out.println(hash_name);

        List<com.gargoylesoftware.htmlunit.html.DomElement> Items = page.getByXPath("//*[contains(@class, 'f_Strong')]");

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            double min_price_rmb = Double.MAX_VALUE;
            for (DomElement elem: Items){

                try {
                    double price_rmb = Double.parseDouble(elem.asNormalizedText().replace("¥ ","").trim());
                    if (min_price_rmb > price_rmb){
                        min_price_rmb = price_rmb;
                    }
                } catch (NumberFormatException e){
                    //do nothing
                }

            }

            if (min_price_rmb == Double.MAX_VALUE){ return;}

            double price_euro = Double.parseDouble(df.format(conversionRateRMBinEUR * min_price_rmb).replace(",","."));

            pstmt.setInt(1, id);
            pstmt.setDouble(2, price_euro);
            pstmt.setBoolean(3, false);
            pstmt.setString(4, hash_name);

            pstmt.execute();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        conn.commit();
    }

    static void getBuffItem(Connection conn, int id) throws IOException, InterruptedException, DocumentException, SQLException, IndexOutOfBoundsException {

        String SQLUpsert = "WITH\n" +
                "    to_be_upserted (id,price_euro,timestamp,has_enterior,name) AS (\n" +
                "        VALUES\n" +
                "            (?,?,current_timestamp,?,?)\n" +
                "    ),\n" +
                "    updated AS (\n" +
                "        UPDATE\n" +
                "            steam_item_sale.buff_item_prices s\n" +
                "        SET\n" +
                "            price_EURO = to_be_upserted.price_EURO::numeric\n" +
                "            ,timestamp = to_be_upserted.timestamp\n" +
                "        FROM\n" +
                "            to_be_upserted\n" +
                "        WHERE\n" +
                "            s.id = to_be_upserted.id\n" +
                "        RETURNING s.id\n" +
                "    )\n" +
                "INSERT INTO steam_item_sale.buff_item_prices (id,price_euro,timestamp,has_enterior,name)\n" +
                "    SELECT id,price_euro,timestamp,has_enterior,name FROM to_be_upserted\n" +
                "    WHERE id NOT IN (SELECT id FROM updated);";

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        List<String[]> result = new ArrayList<>();

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

        String hash_name = null;
        List<DomElement> names = page.getByXPath("//*[contains(@class, 'cru-goods')]");
        for (DomElement name : names) {
            Document name_xml = new SAXReader().read(new StringReader(name.asXml()));
            if (name_xml.valueOf("span") == null) {
                continue;
            }
            hash_name = name_xml.valueOf("span").trim();
        }

        List<com.gargoylesoftware.htmlunit.html.DomElement> Items = page.getByXPath("//*[contains(@class, 'relative-goods')]");

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            for (DomNode element : Items.get(0).getChildNodes()) {
                //System.out.println(element.asXml().trim());
                String goodsId;

                org.w3c.dom.NamedNodeMap map = element.getAttributes();

                if (map.getLength() == 0) {
                    continue;
                }

                try {
                    goodsId = map.getNamedItem("data-goodsid").getNodeValue();
                } catch (NullPointerException e) {
                    goodsId = "" + id;
                }

                String price_rmb = element.asNormalizedText().replaceAll(" ", "").split("¥")[1];

                System.out.print(goodsId + " ");
                System.out.print(price_rmb + " ");

                double price_euro = Double.parseDouble(df.format(conversionRateRMBinEUR * Double.parseDouble(price_rmb)).replace(",", "."));
                System.out.print(price_euro + " ");
                System.out.println();

                //id,price_euro,timestamp,success,name
                pstmt.setInt(1, Integer.parseInt(goodsId));
                pstmt.setDouble(2, price_euro);
                pstmt.setBoolean(3, true);
                if ((Integer.parseInt(goodsId)) == id) //name will only be inserted if there is no entry at all
                {
                    pstmt.setString(4, hash_name);
                } else {
                    pstmt.setString(4, "");
                }

                pstmt.addBatch();

            }
            System.out.println(pstmt.toString());
            int[] updateCounts = pstmt.executeBatch();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new SQLException();
        }
        conn.commit();
    }
}

