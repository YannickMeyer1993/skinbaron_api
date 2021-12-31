package com.company.common;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/*
 * converts USD to EUR s
 *
*/
public class CurrencyHelper {

    /**
     * @param Currency CNY or USD
     * @return gives conversion rate from Currency to EUR
     */
    public static Double getConversionRateToEuro(String Currency) throws Exception {

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);

        String url;
        switch (Currency) {
            case "USD":
                url = "https://www.finanzen.net/waehrungsrechner/euro_us-dollar";
                break;
            case "CNY":
                url = "https://www.finanzen.net/waehrungsrechner/euro_chinesicher-renminbi-yuan";
                break;
            default:
                throw new IllegalArgumentException("Currency must be RMB or USD.");

        }

        try (WebClient webClient = new WebClient(com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX))
        {
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.waitForBackgroundJavaScript(10000);
            com.gargoylesoftware.htmlunit.html.HtmlPage page_USDtoEUR;
            page_USDtoEUR = webClient.getPage(url);
            String line = "";

            List<DomElement> Items_USBtoEUR = page_USDtoEUR.getByXPath("//*[contains(@id, 'currency-second-display')]");
            for (DomElement item_USBtoEUR: Items_USBtoEUR)
            {
                try {
                    line = item_USBtoEUR.getFirstChild().asNormalizedText();
                }
                catch (NullPointerException e){
                    throw new Exception("Kein Wert für Währung gefunden!");
                }
            }

            return Double.parseDouble(line.replace("1 "+Currency+" = ","").replace(" EUR","").replace(",", "."));
        }
        catch (FailingHttpStatusCodeException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
