package com.company.entrypoints;

import junit.framework.TestCase;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static com.company.entrypoints.InventoryCrawler.*;

public class InventoryCrawlerTest extends TestCase {

    public void testSendRequestInsertInventoryItem() throws Exception {

        insertItemIntoInventory("AWP | Dragon Lore (Factory New)",1,"Test Inv");
        insertInventory();
        assertFalse(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'AWP | Dragon Lore (Factory New)' and inv_type='Test Inv'"));
        executeDDL("DELETE FROM steam.inventory where name = 'AWP | Dragon Lore (Factory New)'");
    }

    public void testGetSkinbaronSales() throws Exception {
        getSkinbaronSalesForInventory();
    }

    public void testGetSkinbaronOpenSalesJSONAray() throws Exception {
        System.out.println(getSkinbaronOpenSalesJSONAray().length());
    }

    public void testGetSkinbaronSalesForTable() throws Exception {
        getSkinbaronSalesForTable();
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_sales;"));
    }

    public void testGetSkinbaronInventory() throws Exception {
        getSkinbaronInventory();
    }

    public void testAddInventoryItemNegative() throws Exception {
        insertItemIntoInventory("Gibt es nicht",1,"Test Inv");
        insertInventory();
        assertTrue(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'Gibt es nicht' and inv_type='Test Inv'"));

    }
}