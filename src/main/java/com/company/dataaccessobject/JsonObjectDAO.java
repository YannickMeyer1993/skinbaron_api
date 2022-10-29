package com.company.dataaccessobject;

import com.company.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;

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
        init();
    }

    public void print() throws Exception {
        //print inventory as JSON

        //print Skinbaron Sales as JSON

        //print all items in itemController as JSON
        for (String name: itemController.keySet()) {
            itemController.get(name).print();
        }
    }

    @Override
    public void init() throws Exception {

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

    @Override
    public void crawlWearValues() throws Exception {

    }

    @Override
    public void insertItemInformations() throws Exception {
        Map<String, String[]> map = crawlItemsFromCsgoExchange();

        int i = 0;
        for (String name: map.keySet()) {
            if (!itemController.containsKey(name)) {
                String[] receivedItem = map.get(name);
                System.out.println(i);
                i++;
                //Item item = new Item(name,new ItemCollection(receivedItem[2],false),receivedItem[1],receivedItem[3],receivedItem[4]);
                //itemController.put(name,item);
                break;
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
