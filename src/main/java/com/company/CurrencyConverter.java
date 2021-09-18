package com.company;

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
public class CurrencyConverter {

    /**
     * @throws IOException
     * @throws FailingHttpStatusCodeException
     * @throws MalformedURLException
     * @return gives conversion rate from USD to EUR
     */
    public static Double getUSDinEURO(Double value) throws IOException,FailingHttpStatusCodeException,MalformedURLException{

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF); //ignore useless warnings
        String url_USBtoEUR = "https://www.finanzen.net/waehrungsrechner/euro_us-dollar";
        WebClient webClient_USDtoEUR = new WebClient(com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX);
        webClient_USDtoEUR.getOptions().setJavaScriptEnabled(false);
        webClient_USDtoEUR.getOptions().setCssEnabled(true);
        webClient_USDtoEUR.getOptions().setThrowExceptionOnScriptError(false);
        webClient_USDtoEUR.waitForBackgroundJavaScript(10000);
        com.gargoylesoftware.htmlunit.html.HtmlPage page_USDtoEUR;

        try
        {
            page_USDtoEUR = webClient_USDtoEUR.getPage(url_USBtoEUR);
            String line_USBtoEUR = "";

            List<DomElement> Items_USBtoEUR = page_USDtoEUR.getByXPath("//*[contains(@id, 'currency-second-display')]");
            for (DomElement item_USBtoEUR: Items_USBtoEUR)
            {
                line_USBtoEUR = item_USBtoEUR.getFirstChild().asNormalizedText();
            }
            webClient_USDtoEUR.close();

            return Double.parseDouble(line_USBtoEUR.replace("1 USD = ","").replace(" EUR","").replace(",", "."));
        }
        catch (FailingHttpStatusCodeException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @throws IOException
     * @throws MalformedURLException
     * @return gives conversion rate from RMB to EUR
     */
    public static Double getRMBinEURO(Double value) {

            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
            String url_USBtoEUR = "https://www.finanzen.net/waehrungsrechner/euro_chinesicher-renminbi-yuan";
            WebClient webClient_RMBtoEUR = new WebClient(com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX);
            webClient_RMBtoEUR.getOptions().setJavaScriptEnabled(false);
            webClient_RMBtoEUR.getOptions().setCssEnabled(true);
            webClient_RMBtoEUR.getOptions().setThrowExceptionOnScriptError(false);
            webClient_RMBtoEUR.waitForBackgroundJavaScript(20000);
            com.gargoylesoftware.htmlunit.html.HtmlPage page_RMBtoEUR;

            try {
                page_RMBtoEUR = webClient_RMBtoEUR.getPage(url_USBtoEUR);
                String line_RMBtoEUR = "";

                List<DomElement> Items_RMBtoEUR = page_RMBtoEUR.getByXPath("//*[contains(@id, 'currency-second-display')]");
                for (DomElement item_RMBtoEUR: Items_RMBtoEUR)
                {
                    line_RMBtoEUR = item_RMBtoEUR.getFirstChild().asNormalizedText();

                }
                webClient_RMBtoEUR.close();

                return Double.parseDouble(line_RMBtoEUR.replace("1 CNY = ","").replace(" EUR","").replace(",", "."));
            }
            catch (FailingHttpStatusCodeException e)
            {
                e.printStackTrace();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
    }

}
