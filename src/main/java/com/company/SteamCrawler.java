package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import static com.company.common.readPasswordFromFile;


public class SteamCrawler {

    private static int MAX_ITERATION = 400;
    private static int iteration;
    private static double conversionFromUSDtoEUR;


    static {
        try {
            conversionFromUSDtoEUR = CurrencyConverter.getUSDinEURO(new Double(1));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // write your code here
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select highest_iteration_steam+1 as iteration from steam_item_sale.overview where \"DATE\" = CURRENT_DATE;");

        if (!rs.next()) //Start of today
        {
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
                System.out.println("Steam Scrawling will be started at Iteration 1.");
                int rowsAffected = pstmt.executeUpdate();
                iteration = 1;
            }

        } //End Start of the Day
        else
        {
            iteration = rs.getInt("iteration"); //rs.next() was called above
        }

        System.out.println("Starte mit Iteration  "+ iteration);

        while (iteration < MAX_ITERATION) {
            Boolean works = getItemsforSteamPageNumber(conn, iteration);
            setIterationCounter(conn,  iteration);
            conn.commit();

            if (works){
                iteration++;

            }
            Thread.sleep(30 * 1000);

        }

        System.out.println("Maximale Iterationsanzahl erreicht. Programm wird beendet.");

        rs.close();
        stmt.close();
        conn.commit();
        conn.close();
    }


    public static void setIterationCounter(Connection conn,  int i) {
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


    public static Boolean getItemsforSteamPageNumber(Connection conn, int pageNumber) throws Exception {
        System.out.println("Iteration: " + pageNumber);

        if (pageNumber % 50 == 0) {
            conversionFromUSDtoEUR = CurrencyConverter.getUSDinEURO(1.0);
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

        for (DomElement item : Items) {
            String item_xml = item.asXml();

            //System.out.println(item_xml);

            Document document = new SAXReader().read(new StringReader(item_xml));
            String name = document.valueOf("/div/@data-hash-name");

            int quantity = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-qty"));
            int price_source = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-price"));
            int currencyId = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-currency"));

            System.out.println("Item Name = " + name);
            System.out.println("Quantity = " + quantity);
            System.out.println("Preis in USD cents = " + price_source);
            System.out.println("");

            if (name == null) {
                throw new Exception("Fehlerhafte Ergebnisse für Iteration: " + pageNumber);
            }

            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            df.setRoundingMode(java.math.RoundingMode.HALF_UP);

            Double price_eur = Double.valueOf(-1);

            if (1 == currencyId) {

                price_eur = Double.parseDouble(df.format(conversionFromUSDtoEUR * price_source / 100).replace(",", "."));
            } else if (3 == currencyId) {
                price_eur = Double.parseDouble(df.format(price_source / 100).replace(",", "."));
            } else {
                throw new Exception("CurrencyId nicht USD oder EUR");
            }

            String SQLinsert = "INSERT INTO steam_item_sale.steam_item_prices(name,quantity,price_euro) "
                    + "VALUES(?,?,?)";

            try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, price_eur);

                //System.out.println(pstmt);
                int rowsAffected = pstmt.executeUpdate();
            }
            conn.commit();

        } //End of for each Item


        return true;
    }


}

