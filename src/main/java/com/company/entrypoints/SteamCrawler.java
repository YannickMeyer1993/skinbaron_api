package com.company.entrypoints;

import com.company.CurrencyConverter;
import com.company.common.CurrencyHelper;
import com.company.dataaccessobject.PostgresDAO;
import com.company.model.SteamPrice;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.slf4j.Logger;

import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.common.CurrencyHelper.getConversionRateToEuro;
import static com.company.common.PostgresHelper.getConnection;


public class SteamCrawler {

    private static Double conversionRateUSDinEUR;
    //private final static Logger logger = Logger.getLogger(SteamCrawler.class.getName());
    private static Logger logger = LoggerFactory.getLogger(SteamCrawler.class);

    private final static int MAX_ITERATION = 1600;
    private static final String UrlPost = "http://localhost:8080/api/v1/AddSteamPrice";

    public static void setUpClass() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.apache.http");
        root.setLevel(ch.qos.logback.classic.Level.ERROR);
        ch.qos.logback.classic.Logger root2 = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.gargoylesoftware.htmlunit");
        root2.setLevel(ch.qos.logback.classic.Level.ERROR);

        //...
    }

    public static void main(String[] args) throws Exception {

        setUpClass();

        //java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        //java.util.logging.Logger.getLogger("org.apache.http").setLevel(Level.OFF);

        PostgresDAO dao = new PostgresDAO();

        conversionRateUSDinEUR = getConversionRateToEuro("USD");

        int iteration = dao.getHighestSteamIteration()+1;

        logger.info("Starting with iteration: " + iteration);

        int wait_counter = 3;
        boolean iteration_successfull = false;
        while (iteration < MAX_ITERATION || !iteration_successfull) {
            try {
                logger.info("Waiting for " + Math.pow(2, wait_counter) + " seconds");
                Thread.sleep((long) (Math.pow(2, wait_counter) * 1000));

                iteration_successfull = getItemsforSteamPageNumber(iteration);
                if (iteration_successfull) {
                    dao.setHighestSteamIteration(iteration);
                    iteration++;
                }

                wait_counter = 3;

            } catch (Exception e) {
                wait_counter++;
            }
        }
        logger.info("Reached maximum iteration.");
    }

    public static @NotNull Boolean getItemsforSteamPageNumber(int pageNumber) throws Exception {
        Connection conn = getConnection();

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

       logger.info("There are " + Items.size() + " Items on the Steam Page no. " + pageNumber + "\n");

        if (Items.size() == 0) {
            return false;
        }

        for (DomElement item : Items) {
            String item_xml = item.asXml();

            //System.out.println(item_xml);

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

            SteamPrice price = new SteamPrice(name,Date.valueOf(LocalDate.now()),price_eur,quantity);
            //TODO over class SteamPrice

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject JsonObject = new JSONObject();

            JsonObject.put("itemname", name);
            JsonObject.put("price", price_eur);
            JsonObject.put("quantity",quantity);

            HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

            restTemplate.postForObject(UrlPost, request, String.class);

        } //End of for each Item
        return true;
    }
}


