package com.company.dataaccessobject;

import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.company.common.LoggingHelper.setUpClass;

/**
 * Constructor initializes all item master data
 */
public interface ItemDAO {

    /**
     * @param items List of Skinbaron Items that will be added to the underlying data structure
     * @return if the last ID of the Skinbaron Item is new, then this ID, else empty String
     * @throws Exception
     */
    String addSkinbaronItems(List<SkinbaronItem> items) throws Exception;

    /**
     * processes a Skinbaron item. Adds the price to the data structure.
     * @param item contains the information that will be added.
     * @return if the ID of the Skinbaron Item is new, then this ID, else empty String
     * @throws Exception
     */
    String addSkinbaronItem(SkinbaronItem item) throws Exception;

    /**
     * processes a new Steam Price. Adds the price to the data structure.
     * @param price contains the information that will be added
     * @throws Exception
     */
    void addSteamPrice(SteamPrice price) throws Exception;

    /**
     *
     * @return JSON Array of Skinbaron Ids that are profitable to buy
     * @throws Exception
     */
    org.json.JSONArray getItemsToBuy() throws Exception;


    /**
     * runs a clean up of the data structure to improve performance and delete useless information
     * @throws Exception
     */
    void cleanUp() throws Exception;

    /**
     * get last Skinbaron Id that was inserted to the data structure.
     * @return Id of last inserted Skinbaron Item
     * @throws Exception
     */
    String getLastSkinbaronId() throws Exception;

    /**
     * clean up method to delete all Skinbaron Item Information for given name and price lower than @param price
     * @param ItemName String Item Name
     * @param price threshold for Skinbaron price. All below that threshold get deleted.
     * @throws Exception
     */
    void deleteNonExistingSkinbaronItems(String ItemName, double price) throws Exception;

    /**
     * adds a sold Skinbaron Item to the data strucure
     * @param payload
     * @throws Exception
     */
    void insertSoldSkinbaronItem( JsonNode payload) throws Exception;

    /**
     * gets the id of the last sold Skinbaron Item to call the Skinbaron API
     * @return
     * @throws Exception
     */
    String getLastSoldSkinbaronId() throws Exception;

    /**
     * adds a line to the history of the monetary value of all owned items
     * @param steam_balance
     * @param steam_sales_value
     * @param skinbaron_balance
     * @throws Exception
     */
    void insertOverviewRow(double steam_balance,double steam_sales_value, double skinbaron_balance) throws Exception;

    /**
     * deleted a Skinbaron Item from the data structure.
     * @param id of the Skinbaron Item
     * @throws Exception
     */
    void deleteSkinbaronId(String id) throws Exception;

    /**
     * adds information of a sold item on the platform to the data structure.
     * @param json
     * @throws Exception
     */
    void insertNewestSales(String json) throws Exception;

    /**
     * adds all open Sales on the Platform to the data structure.
     * @param payload
     * @throws Exception
     */
    void insertSkinbaronSales(JsonNode payload) throws Exception;

    /**
     * in case of cleaning up the data structure, use this function to delete all open Sales from the data structure.
     * @throws Exception
     */
    void deleteSkinbaronSalesTable() throws Exception;

    /**
     * adds all Items from the Skinbaron Inventory to the Inventory data structure.
     * @param payload
     * @throws SQLException
     */
    void addSkinbaronInventoryItems(JsonNode payload) throws SQLException;

    /**
     * adds all Items from the Steam Inventory to the Inventory data structure.
     * @param payload
     * @throws Exception
     */
    void addInventoryItems(JsonNode payload) throws Exception;

    /**
     * adds Buff Prices to the data struture.
     * @param payload
     * @throws Exception
     */
    void insertBuffPrices(JSONArray payload) throws Exception;

    /**
     * crawls all remaining Buff Ids and adds them to the data structure.
     * @return
     * @throws SQLException
     */
    String getBuffIds() throws SQLException;

    /**
     * crawls all Item Collection and adds them to the reference list in the data structure.
     * @throws Exception
     */
    void insertCollections() throws Exception;

    /**
     * adds information of item name and lowest price on Skinbaron to the data structure.
     * @param payload
     * @throws Exception
     */
    void insertPriceList(JsonNode payload) throws Exception;

    /**
     * @return Map of Item Name as Key and String[weapon,collection,quality,name_without_exterior]
     * @throws IOException
     * @throws InterruptedException
     */
    default Map<String, String[]> crawlItemsFromCsgoExchange() throws IOException, InterruptedException {
        setUpClass();

        Map<String, String[]> map = new HashMap<>();

        String url = "https://csgo.exchange/prices/";

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(1000);

        List<DomElement> Items = page.getByXPath("//*[contains(@class, 'cItem')]");

        System.out.println("Item Information on csgo.exchhange will be scawled!");

        for (DomElement item : Items) {
            String item_xml = item.asXml();

            String name = item.getFirstChild().asNormalizedText();

            if ("".equals(name) || name.contains("Souvenir Souvenir") || name.contains("Sealed Graffiti")) {
                continue;
            }


            String weapon = item.getAttribute("data-weapon");
            String collection = item.getAttribute("data-collection");
            String quality = item.getAttribute("data-quality");

            Double vn_price = !"0.00".equals(item.getAttribute("data-vn").trim()) ? Double.parseDouble(item.getAttribute("data-vn")) : null;
            Double bs_price = !"0.00".equals(item.getAttribute("data-bs").trim()) ? Double.parseDouble(item.getAttribute("data-bs")) : null;
            Double ww_price = !"0.00".equals(item.getAttribute("data-ww").trim()) ? Double.parseDouble(item.getAttribute("data-ww")) : null;
            Double ft_price = !"0.00".equals(item.getAttribute("data-ft").trim()) ? Double.parseDouble(item.getAttribute("data-ft")) : null;
            Double mw_price = !"0.00".equals(item.getAttribute("data-mw").trim()) ? Double.parseDouble(item.getAttribute("data-mw")) : null;
            Double fn_price = !"0.00".equals(item.getAttribute("data-fn").trim()) ? Double.parseDouble(item.getAttribute("data-fn")) : null;

            if (vn_price == null && fn_price == null && mw_price == null && ft_price == null && ww_price == null && bs_price == null) {
                continue;
            }

            if (name.contains("StatTrak")) {
                name = name.replace("StatTrak", "StatTrak\u2122");
            }

            if (name.contains("/")) {
                name = name.replace("/", "-");
            }

            //Knife
            if ("Covert".equals(quality) && (weapon.contains("Knife") || weapon.contains("Bayonet") || weapon.contains("Shadow Daggers") || weapon.contains("Karambit") || "".equals(weapon))) {
                name = "\u2605 " + name;
            }

            //Gloves
            if (name.contains("Gloves") || name.contains("Hand Wraps")) {
                name = "\u2605 " + name;
            }

            name = name.replaceAll(" {2}", " ");


            String[] infos = new String[4];
            if (vn_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name, infos);
            }
            if (bs_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Battle-Scarred)", infos);
            }
            if (ww_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Well-Worn)", infos);
            }
            if (ft_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Field-Tested)", infos);
            }
            if (mw_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Minimal Wear)", infos);
            }
            if (fn_price != null) {
                infos[0] = weapon;
                infos[1] = collection;
                infos[2] = quality;
                infos[3] = name.replace("StatTrak\u2122 ", "");
                map.put(name + " (Factory New)", infos);
            }
        }

        return map;

    }

    default Map<String, String[]> crawlWearValuesFromCsgoStash(ArrayList<Integer> missingIds) throws Exception {

        Map<String, String[]> mapWears = new HashMap<>();

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

        com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient(com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(true); // enable javascript
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false); //even if there is error in js continue
        webClient.waitForBackgroundJavaScriptStartingBefore(1000000);
        webClient.waitForBackgroundJavaScript(10000000); // important! wait when javascript finishes rendering

        int count = 0;
        for (Integer i : missingIds) {

            count++;
            try {
                String url = "https://csgostash.com/skin/" + i;
                com.gargoylesoftware.htmlunit.html.HtmlPage page = webClient.getPage(url);

                Thread.sleep(1000);

                String name = page.getTitleText().replace(" - CS:GO Stash", "");
                List<com.gargoylesoftware.htmlunit.html.DomElement> Items_min = page.getByXPath("//*[contains(@class, 'marker-wrapper wear-min-value')]");
                List<com.gargoylesoftware.htmlunit.html.DomElement> Items_max = page.getByXPath("//*[contains(@class, 'marker-wrapper wear-max-value')]");
                List<com.gargoylesoftware.htmlunit.html.DomElement> Items_name = page.getByXPath("//*[contains(@class, 'img-responsive center-block main-skin-img margin-top-sm margin-bot-sm')]");

                try {
                    String xml_min = Items_min.get(0).asXml();
                    String xml_max = Items_max.get(0).asXml();

                    Document document_min = new SAXReader().read(new StringReader(xml_min));
                    String min = document_min.valueOf("/div/@data-wearmin");

                    Document document_max = new SAXReader().read(new StringReader(xml_max));
                    String max = document_max.valueOf("/div/@data-wearmax");

                    //System.out.println(name + " " + min + " " + max);

                    String[] infos = new String[3];
                    //put in map
                    infos[0] = "" + i;
                    infos[1] = min;
                    infos[2] = max;
                    mapWears.put(name, infos);
                } catch (IndexOutOfBoundsException e) {
                    String[] infos = new String[3];
                    //put in map
                    infos[0] = "" + i;
                    infos[1] = "0";
                    infos[2] = "1";
                    mapWears.put(name, infos);
                }
            } catch (FailingHttpStatusCodeException e) {
                e.getStatusCode();
            }
        }

        return mapWears;
    }

}
