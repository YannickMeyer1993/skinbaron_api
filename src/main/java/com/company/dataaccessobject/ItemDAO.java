package com.company.dataaccessobject;

import com.company.model.Item;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.SQLException;

public interface ItemDAO {
    void init() throws Exception;

    String addSkinbaronItem(SkinbaronItem item) throws Exception;

    void addSteamPrice(SteamPrice price) throws Exception;

    org.json.JSONArray getItemsToBuy() throws Exception;

    int getHighestSteamIteration() throws Exception;

    void initHightestSteamIteration() throws Exception;

    void setHighestSteamIteration(int iteration) throws Exception;

    Item getItem(String ItemName);

    void cleanUp() throws Exception;

    void crawlWearValues() throws Exception;

    void crawlItemInformations() throws Exception;

    String getLastSkinbaronId() throws Exception;

    void deleteNonExistingSkinbaronItems(String ItemName, double price) throws Exception;

    void insertSoldSkinbaronItem( JsonNode payyload) throws Exception;

    String getLastSoldSkinbaronId() throws Exception;

    void insertOverviewRow(double steam_balance,double steam_sales_value, double skinbaron_balance) throws Exception;

    void deleteSkinbaronId(String id) throws Exception;

    void insertNewestSales(String json) throws Exception;

    void insertSkinbaronSales(JsonNode payload) throws Exception;

    void deleteSkinbaronSalesTable() throws Exception;

    void addSkinbaronInventoryItems(JsonNode payload) throws SQLException;

    void addInventoryItems(JsonNode payload) throws Exception;
}
