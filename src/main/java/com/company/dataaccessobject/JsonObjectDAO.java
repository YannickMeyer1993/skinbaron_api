package com.company.dataaccessobject;

import com.company.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonObjectDAO implements ItemDAO {
    private Inventory inventory = new Inventory();
    private SkinbaronSales skinbaronSales = new SkinbaronSales();
    private HashMap<String, Item> itemController = new HashMap<>();
    private final String RESOURCE_PATH = "src/main/resources/JsonObjectDAO";

    public JsonObjectDAO() throws Exception {

        //crawl all needed information
        insertItemInformations();
        crawlWearValues();

        //TODO read all json information from disk

    }

    @Override
    public String addSkinbaronItems(List<SkinbaronItem> items) throws Exception {
        return null;
    }

    @Override
    public String addSkinbaronItem(SkinbaronItem item) throws Exception {
        return null;
    }

    @Override
    public void addSteamPrice(SteamPrice price) throws Exception {

    }

    @Override
    public JSONArray getItemsToBuy() throws Exception {
        return null;
    }

    @Override
    public void cleanUp() throws Exception {

    }

    private void crawlWearValues() throws Exception {

    }

    private void insertItemInformations() throws Exception {
        Map<String, String[]> map = crawlItemsFromCsgoExchange();

        for (String name: map.keySet()) {
            if (!itemController.containsKey(name)) {
                String[] receivedItem = map.get(name);

                Item item = new Item(name,new ItemCollection(receivedItem[1],false),receivedItem[0],receivedItem[2],receivedItem[3]);
                itemController.put(name,item);
            }
        }

        for (String item: itemController.keySet()) {
            String nameJsonFile = RESOURCE_PATH+"/"+java.net.URLEncoder.encode(item, "UTF-8").replace("*","")+".json";
            File f = new File(nameJsonFile);
            if(f.exists() && !f.isDirectory()) {
                continue;
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(nameJsonFile))) {
                out.write(itemController.get(item).getAsJson().toString());
                System.out.println("JSON File for "+item+" has been created.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getLastSkinbaronId() throws Exception {
        return null;
    }

    @Override
    public void deleteNonExistingSkinbaronItems(String ItemName, double price) throws Exception {

    }

    @Override
    public void insertSoldSkinbaronItem(JsonNode payload) throws Exception {

    }

    @Override
    public String getLastSoldSkinbaronId() throws Exception {
        return null;
    }

    @Override
    public void insertOverviewRow(double steam_balance, double steam_sales_value, double skinbaron_balance) throws Exception {

    }

    @Override
    public void deleteSkinbaronId(String id) throws Exception {

    }

    @Override
    public void insertNewestSales(String json) throws Exception {

    }

    @Override
    public void insertSkinbaronSales(JsonNode payload) throws Exception {

    }

    @Override
    public void deleteSkinbaronSalesTable() throws Exception {

    }

    @Override
    public void addSkinbaronInventoryItems(JsonNode payload) throws SQLException {

    }

    @Override
    public void addInventoryItems(JsonNode payload) throws Exception {

    }

    @Override
    public void insertBuffPrices(JSONArray payload) throws Exception {

    }

    @Override
    public String getBuffIds() throws SQLException {
        return null;
    }

    @Override
    public void insertCollections() throws Exception {

    }

    @Override
    public void insertPriceList(JsonNode payload) throws Exception {

    }
}
