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

import java.io.IOException;
import java.io.StringReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.company.common.CurrencyHelper.getConversionRateToEuro;
import static com.company.common.LoggingHelper.setUpClass;

//TODO multplie threads
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
        JSONArray array = getBuffIds();

        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            if (o.getBoolean("has_exterior")) {
                getBuffItemWithExterior(o.getInt("id"));
            } else if (!o.getBoolean("has_exterior")) {
                getBuffItemNoExterior(o.getInt("id"));
            } else {
                throw new Exception("No information about exterior given.");
            }
        }
    }

    public static void getBuffItemNoExterior(int id) throws InterruptedException, IOException, DocumentException {

        logger.info("Buff Id: "+id);

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        String url = "https://buff.163.com/market/goods?goods_id=" + id;

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
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
            return;
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
    }

    public static void getBuffItemWithExterior(int id) throws Exception {

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        String url = "https://buff.163.com/market/goods?goods_id=" + id;

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
            //if (name_xml.valueOf("span") == null) {
                //hash_name = name_xml.valueOf("span").trim();
            //}

        }

        List<com.gargoylesoftware.htmlunit.html.DomElement> Items = page.getByXPath("//*[contains(@class, 'relative-goods')]");

        JSONArray array = new JSONArray();

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
            JSONObject o = new JSONObject();
            o.put("id", goodsId);
            o.put("price_euro", price_euro);
            o.put("has_exterior", true);

            array.put(o);
        }
        insertBuffPrices(array);
    }

    public static void getNewBuffItems() {
        //TODO Iterate over Number 0-1000000
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

