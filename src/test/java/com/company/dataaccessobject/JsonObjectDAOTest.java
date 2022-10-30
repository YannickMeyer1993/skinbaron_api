package com.company.dataaccessobject;

import com.company.model.SkinbaronItem;
import junit.framework.TestCase;

public class JsonObjectDAOTest extends TestCase {
    JsonObjectDAO dao;

    public void testConstructor() throws Exception{
        //test init of DAO
        dao = new JsonObjectDAO();
    }

    public void testAddSkinbaronItems() {
    }

    public void testAddSkinbaronItem() throws Exception {
        SkinbaronItem item = new SkinbaronItem("id",10.0,"Test Item","",0.22,"","");
        dao.addSkinbaronItem(item);
        assertEquals(dao.getLastSkinbaronId(),"id");
    }

    public void testAddSteamPrice() {
    }

    public void testGetItemsToBuy() {
    }

    public void testCleanUp() {
    }

    public void testCrawlWearValues() {
    }

    public void testGetLastSkinbaronId() {
    }

    public void testDeleteNonExistingSkinbaronItems() {
    }

    public void testInsertSoldSkinbaronItem() {
    }

    public void testGetLastSoldSkinbaronId() {
    }

    public void testInsertOverviewRow() {
    }

    public void testDeleteSkinbaronId() {
    }

    public void testInsertNewestSales() {
    }

    public void testInsertSkinbaronSales() {
    }

    public void testDeleteSkinbaronSalesTable() {
    }

    public void testAddSkinbaronInventoryItems() {
    }

    public void testAddInventoryItems() {
    }

    public void testInsertBuffPrices() {
    }

    public void testGetBuffIds() {
    }

    public void testInsertCollections() {
    }

    public void testInsertPriceList() {
    }

    public void testCrawlItemsFromCsgoExchange() throws Exception{
        dao.crawlItemsFromCsgoExchange();
    }
}