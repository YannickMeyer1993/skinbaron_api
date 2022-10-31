package com.company.entrypoints;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.util.List;

import static com.company.common.CurrencyHelper.getConversionRateToEuro;
import static com.company.common.LoggingHelper.setUpClass;

public class SteamAPI {
    private static final Logger logger = LoggerFactory.getLogger(SteamAPI.class);
    private static final String UrlPost = "http://localhost:8080/api/v1/AddSteamPrice";
    private static Double conversionRateUSDinEUR;

    public static void main(String[] args) throws Exception {
        setUpClass(); //disable Logging
        Boolean repeat = true;
        int start = 0;
        //TODO start with last successfull index of current date, else =0

        while (repeat) {
            try {
                repeat = requestSearch(start);
                Thread.sleep(3000);
                start+=100;
            } catch (Exception e) {
                logger.error("Retry for Index: "+start);
                Thread.sleep(7000);
            }
        }
    }

    static Boolean requestSearch(int start) throws Exception {
        HttpPost httpPost = new HttpPost("https://steamcommunity.com/market/search/render/?search_descriptions=0&sort_column=name&sort_dir=desc&appid=730&norender=1&count=500&currency=3&start="+start);
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

        if (!resultJson.has("success") || resultJson.getInt("total_count")==0) {
            throw new Exception(resultJson.toString());
        }

        if (start > resultJson.getInt("total_count")) {
            logger.info("End reached.");
            return false;
        }

        logger.info("Success: "+resultJson.getBoolean("success"));
        logger.info("Start: "+resultJson.getInt("start"));
        logger.info("Page Size: "+resultJson.getInt("pagesize"));
        logger.info("Total Count: "+resultJson.getInt("total_count"));
        logger.info("Count within JSON: "+((JSONArray)resultJson.get("results")).length());

        JSONArray insertArray = new JSONArray();

        int add_to_start = 0;
        for (Object o: ((JSONArray)resultJson.get("results"))) {
            if (o instanceof JSONObject) {
                String name = ((JSONObject) o).getString("hash_name");
                int quantity = ((JSONObject) o).getInt("sell_listings");
                Double sell_price = Double.valueOf(((JSONObject) o).getString("sell_price_text").substring(1).replace(",",""));
                Double price = Double.valueOf(((JSONObject) o).getString("sale_price_text")
                        .substring(1) //currency symbol
                        .replace(",","") //thousand
                );

                JSONObject o1 = new JSONObject();
                o1.put("itemname",name);
                o1.put("quantity",quantity);
                //o1.put("sell_price",sell_price); //what is this value?
                o1.put("price",price);

                int steamStartIndex = start+add_to_start;
                o1.put("start_parameter",steamStartIndex);

                insertArray.put(o1);

                //TODO batch insert over insertArray
                requestInsertNewSteamprice(name,price,quantity,steamStartIndex);

            }
            add_to_start++;
        }

        //logger.info(insertArray.toString());
        return true;
    }

    public static void requestInsertNewSteamprice(String name, Double price, int quantity, Integer steamStartIndex) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        JsonObject.put("itemname", name);
        JsonObject.put("price", price);
        JsonObject.put("quantity",quantity);
        JsonObject.put("steamstartindex",steamStartIndex);

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

            requestInsertNewSteamprice(name,price_euro,quantity,null);
        }

        if (!item_found){
            requestInsertNewSteamprice(hash_name,0d,0,null); //does not exist
            return_price = 0d;
        }

        logger.info("Item \""+hash_name+"\" costs "+return_price+" Euro.");

        Thread.sleep((long) 20*1000);
        return return_price;
    }
}
