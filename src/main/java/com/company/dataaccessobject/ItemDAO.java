package com.company.dataaccessobject;

import com.company.model.Item;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.SQLException;

public interface ItemDAO {
    void init() throws Exception;

    String addSkinbaronItem(SkinbaronItem item) throws Exception;

    void addSteamPrice(SteamPrice price) throws Exception;

    org.json.JSONArray getItemsToBuy() throws Exception;

    int getHighestSteamIteration() throws Exception;

    void initHightestSteamIteration() throws Exception;

    void setHighestSteamIteration(int iteration) throws Exception;

    void addInventoryItem(String itemname,int amount,String inventorytype) throws Exception;

    Item getItem(String ItemName);

    void deleteInventoryItems() throws Exception;

    void cleanUp() throws Exception;

    void crawlWearValues() throws Exception;

    void crawlItemInformations() throws Exception;

    String getLastSkinbaronId() throws Exception;

    void deleteNonExistingSkinbaronItems(String ItemName, double price) throws Exception;

    void insertSoldSkinbaronItem(String itemId, String name, double price, String classid, String last_updated, String instanceid, String list_time, String assetid, String txid, double commission) throws Exception;

    String getLastSoldSkinbaronId() throws Exception;

    void insertOverviewRow(double steam_balance,double steam_sales_value, double skinbaron_balance) throws Exception;

    void deleteSkinbaronId(String id) throws Exception;

    void insertNewestSales(String json) throws Exception;
}
