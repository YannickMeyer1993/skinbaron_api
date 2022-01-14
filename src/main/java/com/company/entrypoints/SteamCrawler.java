package com.company.entrypoints;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;

import static com.company.common.CurrencyHelper.getConversionRateToEuro;
import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PostgresHelper.getConnection;
import static com.company.entrypoints.SkinbaronCrawler.getBalance;

//TODO 0 zurückgeben, wenn null kommt
public class SteamCrawler {

    private static Double conversionRateUSDinEUR;
    private static final Logger logger = LoggerFactory.getLogger(SteamCrawler.class);

    private final static int MAX_ITERATION = 1600;
    private static final String UrlPost = "http://localhost:8080/api/v1/AddSteamPrice";

    public static void main(String[] args) throws Exception {

        setUpClass(); //disable Logging

        conversionRateUSDinEUR = getConversionRateToEuro("USD");

        int iteration = getHighestSteamIteration()+1;

        InventoryCrawler invcrawler = new InventoryCrawler();
        invcrawler.run();

        logger.warn("Decimal separator is comma!");
        Scanner sc= new Scanner(System.in);
        logger.warn("Enter current steam balance: ");
        double steam_balance = sc.nextDouble();
        logger.warn("Enter current steam sales value: ");
        double steam_sales_value = sc.nextDouble();

        getItemPricesInventory();

        double skinbaron_balance = getBalance();

        insertOverviewRow(steam_balance, steam_sales_value, skinbaron_balance );

        logger.info("Starting with iteration: " + iteration);

        int wait_counter = 0;
        boolean iteration_successfull = false;
        while (iteration < MAX_ITERATION || !iteration_successfull) {
            try {
                logger.info("Waiting for " + wait_counter * 10 + " seconds");
                Thread.sleep((long) wait_counter * 10000);

                iteration_successfull = getItemsforSteamPageNumber(iteration);
                if (iteration_successfull) {
                    setHighestSteamIteration(iteration);
                    iteration++;
                }

                wait_counter = 2;

            } catch (Exception e) {
                logger.info(e.getMessage());
                wait_counter++;
            }
        }
        logger.info("Reached maximum iteration.");
    }

    static void setHighestSteamIteration(int iteration) {
        String url = "http://localhost:8080/api/v1/SetHightestSteamIteration";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("iteration", iteration);

        HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }

    public static @NotNull Boolean getItemsforSteamPageNumber(int pageNumber) throws Exception {

       logger.info("Iteration: " + pageNumber);

        if (pageNumber % 50 == 0) {
            conversionRateUSDinEUR = getConversionRateToEuro("USD");
            logger.info("Conversion Factor from USD to EUR: " + conversionRateUSDinEUR);
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

       logger.info("There are " + Items.size() + " Items on the Steam Page no. " + pageNumber);

        if (Items.size() == 0) {
            return false;
        }

        for (DomElement item : Items) {
            String item_xml = item.asXml();

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

                price_eur = Double.parseDouble(df.format(conversionRateUSDinEUR * price_source / 100).replace(",", "."));
            } else if (3 == currencyId) {
                price_eur = Double.parseDouble(df.format(price_source / 100).replace(",", "."));
            } else {
                return false;
            }
            requestInsertNewSteamprice(name,price_eur,quantity);

        } //End of for each Item
        return true;
    }

    public static void requestInsertNewSteamprice(String name,Double price, int quantity) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("itemname", name);
        JsonObject.put("price", price);
        JsonObject.put("quantity",quantity);

        HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(UrlPost, request, String.class);
    }

    public static double getSteamPriceForGivenName(String hash_name) throws Exception {

        conversionRateUSDinEUR = getConversionRateToEuro("USD");

        double return_price = 0.0;
        boolean item_found = false;

        String url = "https://steamcommunity.com/market/search?q="+java.net.URLDecoder.decode(hash_name, "UTF-8")+"&appid=730#p1_default_desc";

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);

        List<DomElement> Items = page.getByXPath("//*[contains(@class, 'market_listing_row market_recent_listing_row market_listing_searchresult')]");

        for (DomElement element : Items) {
            String item_xml = element.asXml();
            Document document = new SAXReader().read(new StringReader(item_xml));

            String name = document.valueOf("/div/@data-hash-name");
            int quantity = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-qty"));
            double price = Double.parseDouble(document.valueOf("/div/div/div/span/span/@data-price"));
            int currency = Integer.parseInt(document.valueOf("/div/div/div/span/span/@data-currency"));

            if (name == null) {
                throw new Exception("Fehlerhafte Ergebnisse für Skin: " + hash_name);
            }

            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            df.setRoundingMode(java.math.RoundingMode.HALF_DOWN);

            double price_euro;
            if (1 == currency) {
                price_euro = Double.parseDouble(df.format(conversionRateUSDinEUR * price / 100).replace(",", "."));
            } else if (3 == currency) {
                price_euro = Double.parseDouble(df.format(price / 100).replace(",", "."));
            } else {
                throw new Exception("Currency nicht USD oder EUR");
            }

            item_found = item_found || hash_name.equals(name);

            if (hash_name.equals(name)){
                return_price = price_euro;
            }

            requestInsertNewSteamprice(name,price_euro,quantity);
        }

        if (!item_found){
            requestInsertNewSteamprice(hash_name,0d,0); //does not exist
            return_price = 0d;
        }

        logger.info("Item \""+hash_name+"\" costs "+return_price+" Euro.");

        Thread.sleep((long) 20*1000);
        return return_price;
    }

    public static int getHighestSteamIteration() throws Exception {
        String url = "http://localhost:8080/api/v1/GetHightestSteamIteration";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        ResponseEntity<String> responseEntityStr = restTemplate.getForEntity( url,String.class);

        if (responseEntityStr.getBody() == null) {
            throw new Exception("Response from "+url+" is null.");
        }
        return Integer.parseInt((responseEntityStr.getBody()));
    }

    public static void getItemPricesInventory() throws Exception {
        try(Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select distinct name from steam.inventory_with_prices s\n" +
                " where \"date\" != current_date  order by name;")) {
            String name;
            while (rs.next()) {
                name = rs.getString("name");
                getSteamPriceForGivenName(name);
            }
        }
    }

    public static void insertOverviewRow(double steam_balance, double steam_sales_value, double skinbaron_balance) {

        String url = "http://localhost:8080/api/v1/SetOverview";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("steambalance", steam_balance);
        JsonObject.put("steamopensales", steam_sales_value);
        JsonObject.put("skinbaronbalance",skinbaron_balance);

        HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }
}


