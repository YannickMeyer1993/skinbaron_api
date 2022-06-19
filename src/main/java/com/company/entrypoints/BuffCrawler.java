package com.company.entrypoints;

import com.company.api.ItemController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import static com.company.common.CurrencyHelper.getConversionRateToEuro;
import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PostgresHelper.getConnection;

public class BuffCrawler {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BuffCrawler.class);
    private static Double conversionRateRMBinEUR;

    static {
        try {
            setUpClass();
            conversionRateRMBinEUR = getConversionRateToEuro("CNY");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        setUpClass();

        //TODO Display count of unknown Buff Ids

        JSONArray array = getBuffIds();

        IntStream.range(0, array.length()).parallel().forEach(i -> {
            JSONObject o = array.getJSONObject(i);
            if (o.getBoolean("has_exterior")) {
                try {
                    getBuffItemWithExterior(o.getInt("id"));
                } catch (Exception e) {
                    logger.info("Error at Id: "+ o.getInt("id"));
                    e.printStackTrace();
                }
            } else if (!o.getBoolean("has_exterior")) {
                try {
                    getBuffItemNoExterior(o.getInt("id"));
                } catch (Exception e) {
                    logger.info("Error at Id: "+ o.getInt("id"));
                    e.printStackTrace();
                }
            } else {
                try {
                    throw new Exception("No information about exterior given.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static double getBuffItemNoExterior(int id) throws InterruptedException, IOException, DocumentException {

        logger.info("Buff Id: "+id);

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        String url = "https://buff.163.com/market/goods?goods_id=" + id;

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(100000);
        webClient.waitForBackgroundJavaScript(100000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(1000); //needed to load page

        String hash_name = null;
        List<DomElement> names = page.getByXPath("//*[contains(@class, 'cru-goods')]");
        for (DomElement name : names) {
            Document name_xml = new SAXReader().read(new StringReader(name.asXml()));
            if (name_xml.valueOf("span") == null) {
                continue;
            }
            hash_name = name_xml.valueOf("span").trim();
        }

        List<com.gargoylesoftware.htmlunit.html.DomElement> Items = page.getByXPath("//*[contains(@class, 'f_Strong')]");

        double min_price_rmb = Double.MAX_VALUE;
        //should be Items.size() = # on website - 1 for ref price
        for (DomElement elem : Items) {

            try {
                double price_rmb = Double.parseDouble(elem.asNormalizedText().replace("¥ ", "").trim());
                if (min_price_rmb > price_rmb) {
                    min_price_rmb = price_rmb;
                }
            } catch (NumberFormatException e) {
                //do nothing
            }

        }

        if (min_price_rmb == Double.MAX_VALUE) {
            min_price_rmb = 0;
        }

        double price_euro = Double.parseDouble(df.format(conversionRateRMBinEUR * min_price_rmb).replace(",", "."));

        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("price_euro", price_euro);
        o.put("has_exterior", false);
        o.put("name", hash_name);

        JSONArray array = new JSONArray();
        array.put(o);
        insertBuffPrices(array);

        return price_euro;
    }

    public static double getBuffItemWithExterior(int id) throws Exception {

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        String url = "https://buff.163.com/market/goods?goods_id=" + id;

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(100000);
        webClient.waitForBackgroundJavaScript(100000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(1000);

        String hash_name = null;
        List<DomElement> names = page.getByXPath("//*[contains(@class, 'cru-goods')]");
        for (DomElement name : names) {
            Document name_xml = new SAXReader().read(new StringReader(name.asXml()));
            if (name_xml.valueOf("span") != null) {
                hash_name = name_xml.valueOf("span").trim();
            }

        }

        List<com.gargoylesoftware.htmlunit.html.DomElement> Items = page.getByXPath("//*[contains(@class, 'relative-goods')]");

        JSONArray array = new JSONArray();
        double returnValue = 0;

        for (DomNode element : Items.get(0).getChildNodes()) {
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
            double price_euro;

            try {
                String price_rmb = element.asNormalizedText().replaceAll(" ", "").split("¥")[1];
                price_euro = Double.parseDouble(df.format(conversionRateRMBinEUR * Double.parseDouble(price_rmb)).replace(",", "."));
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.info("Buff Id "+goodsId+" has no price");
                price_euro = 0;
            }
            returnValue = price_euro;

            JSONObject o = new JSONObject();
            o.put("id", goodsId);
            o.put("price_euro", price_euro);
            o.put("has_exterior", true);

            if (goodsId.equals(String.valueOf(id))) { //name is only correct for main item
                o.put("name", hash_name);
            }

            array.put(o);
        }
        insertBuffPrices(array);
        return returnValue;
    }

    public static void getNewBuffIds() throws Exception {

        ArrayList l = new ArrayList();

        for (int i=887000;i<890000;i++) {
                l.add(i);
        }

        for (Object o: getBuffIds()) {
            if (o instanceof JSONObject) {
                if (l.contains(((JSONObject) o).getInt("id"))) {
                    l.remove(((JSONObject) o).getInt("id"));
                }
            }
        }

        logger.info("Size of ids that are tested: "+l.size());

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");

        IntStream.range(0, l.size()).parallel().forEach(i -> {
            int j = (int) l.get(i);
            try {
                try {
                    getBuffItemNoExterior(j);
                    logger.info("Got id: "+j);
                } catch (Exception e){
                    getBuffItemWithExterior(j);
                    logger.info("Got id: "+j);
                }
            } catch (Exception e) {
                logger.error("Id " + j + " is no item!");
            }

        });
    }

    /**
     * @return Get JSONArray with id,has_exterior in chronological order
     */
    public static JSONArray getBuffIds() {
        String url = "http://localhost:8080/api/v1/GetBuffIds";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntityStr = restTemplate.getForEntity(url, String.class);

        return new JSONArray((responseEntityStr.getBody()));
    }

    public static void insertBuffPrices(JSONArray array) {
        String url = "http://localhost:8080/api/v1/InsertBuffPrices";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(array.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }
}

