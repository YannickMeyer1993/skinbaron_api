package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static com.company.common.readPasswordFromFile;

public class SteamItemPriceChecker {

    private static double conversionFromUSDtoEUR;


    static {
        try {
            conversionFromUSDtoEUR = CurrencyConverter.getUSDinEURO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Double getSteamPriceForGivenName(String hash_name, Connection conn) throws Exception {

        Double return_price = 0.0;
        Boolean item_found = false;

        String SQLinsert = "INSERT INTO steam_item_sale.steam_item_prices(name,quantity,price_euro) "
                + "VALUES(?,?,?)";

        String url = "https://steamcommunity.com/market/search?q="+java.net.URLDecoder.decode(hash_name, "UTF-8")+"#p1_default_desc";

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);

        List<DomElement> Items = page.getByXPath("//*[contains(@class, 'market_listing_row market_recent_listing_row market_listing_searchresult')]");

        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            for (DomElement element : Items) {
                String item_xml = element.asXml();
                Document document = new SAXReader().read(new StringReader(item_xml));

                String name = document.valueOf("/div/@data-hash-name");
                int quantity = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-qty"));
                Double price = Double.parseDouble(document.valueOf("/div/div/div/span/span/@data-price"));
                int currency = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-currency"));

                //Code erstellt anhand des Eingabe- und Ausgabeschemas
                if (name == null) {
                    throw new Exception("Fehlerhafte Ergebnisse für Skin: " + hash_name);
                }


                java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
                df.setRoundingMode(java.math.RoundingMode.HALF_DOWN);

                Double price_euro;
                if (1 == currency) {
                    price_euro = Double.parseDouble(df.format(conversionFromUSDtoEUR * price / 100).replace(",", "."));
                } else if (3 == currency) {
                    price_euro = Double.parseDouble(df.format(price / 100).replace(",", "."));
                } else {
                    throw new Exception("Currency nicht USD oder EUR");
                }

                item_found = item_found || hash_name.equals(name);

                if (hash_name.equals(name)){
                    return_price = price_euro;
                }

                pstmt.setString(1, name);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, price_euro);
                pstmt.addBatch();
            }
            int[] updateCounts = pstmt.executeBatch();
            System.out.println(updateCounts.length + " items were inserted!");
            conn.commit();

            if (!item_found){
                pstmt.setString(1, hash_name);
                pstmt.setInt(2, 0);
                pstmt.setDouble(3, 0.0);
                int rowsAffected = pstmt.executeUpdate();
                conn.commit();
                return_price = 0.0;

            }
        }

        System.out.println("Item \""+hash_name+"\" costs "+return_price+" Euro.");
        Thread.sleep(20*1000);
        return return_price;
    }
}
