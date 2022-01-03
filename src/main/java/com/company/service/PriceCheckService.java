package com.company.service;

import com.company.entrypoints.SteamCrawler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import static com.company.common.CurrencyHelper.getConversionRateToEuro;

public class PriceCheckService {
    private static Double conversionRateUSDinEUR;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(PriceCheckService.class);

    public double getSteamPriceForGivenName(String hash_name) throws Exception {

        conversionRateUSDinEUR = getConversionRateToEuro("USD");

        double return_price = 0.0;
        boolean item_found = false;

        String url = "https://steamcommunity.com/market/search?q="+java.net.URLDecoder.decode(hash_name, "UTF-8")+"#p1_default_desc";

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

                //Code erstellt anhand des Eingabe- und Ausgabeschemas
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

                //TODO send request to add Steam Price
            }

            if (!item_found){
                //TODO send request to add Steam Price
                return_price = 0.0;
            }

        logger.info("Item \""+hash_name+"\" costs "+return_price+" Euro.");
        Thread.sleep((long) 20*1000);
        return return_price;
    }
}
