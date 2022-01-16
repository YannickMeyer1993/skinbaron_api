package com.company.entrypoints;

import junit.framework.TestCase;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;

public class InventoryCrawlerTest extends TestCase {

    public void testTestSendRequestInsertInventoryItem() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        crawler.sendRequestInsertInventoryItem("AWP | Dragon Lore (Factory New)",1,"Test Inv");
        assertFalse(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'AWP | Dragon Lore (Factory New)' and inv_type='Test Inv'"));
        executeDDL("DELETE FROM steam.inventory where name = 'AWP | Dragon Lore (Factory New)'");
    }

    public void testClearInventory() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        crawler.clearInventory();
        assertTrue(checkIfResultsetIsEmpty("select * from steam.inventory where still_there = true"));

    }

    public void testGetSkinbaronSales() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        crawler.getSkinbaronSalesForInventory();
    }

    public void testGetSkinbaronOpenSalesJSONAray() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        System.out.println(crawler.getSkinbaronOpenSalesJSONAray().length());
    }

    public void testClearSkinbaronSales() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        crawler.clearSkinbaronSales();
        assertTrue(checkIfResultsetIsEmpty("select * from steam.skinbaron_sales;"));
    }

    public void testGetSkinbaronSalesForTable() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        crawler.clearSkinbaronSales();
        crawler.getSkinbaronSalesForTable();
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_sales;"));
    }

    public void testGetSkinbaronInventory() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        crawler.getSkinbaronInventory();
    }
}