package com.company.dataaccessobject;

import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;

import java.sql.SQLException;
import java.util.List;

public interface ItemDAO {

    /**
     * needs to be called in the beginning to initialize the data structure.
     * @throws Exception
     */
    void init() throws Exception;

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
     * gets lower and upper bound of wear values of every Steam Item and adds them to the data structure.
     * @throws Exception
     */
    void crawlWearValues() throws Exception;

    /**
     * gets additional information about every Steam Item like Collection and Quality and adds it to the data structure.
     * @throws Exception
     */
    void crawlItemInformations() throws Exception;

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
}
