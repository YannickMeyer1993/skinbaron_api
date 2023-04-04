package de.yannickm.steambot.entrypoints;

import junit.framework.TestCase;
import org.json.JSONArray;

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;
import static de.yannickm.steambot.common.PostgresExecutor.checkIfResultsetIsEmpty;
import static de.yannickm.steambot.common.PostgresExecutor.executeDDL;
import static de.yannickm.steambot.entrypoints.InventoryCrawler.*;

public class InventoryCrawlerTest extends TestCase {

    JSONArray inventory = new JSONArray();

    public void testSendRequestInsertInventoryItem() throws Exception {

        insertItemIntoInventory("AWP | Dragon Lore (Factory New)",1,"Test Inv");
        insertInventory();
        assertFalse(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'AWP | Dragon Lore (Factory New)' and inv_type='Test Inv'"));
        executeDDL("DELETE FROM steam.inventory where name = 'AWP | Dragon Lore (Factory New)'");
    }

    public void testGetSkinbaronSales() throws Exception {
        getSkinbaronSalesForInventory(inventory);
    }

    public void testGetSkinbaronOpenSalesJSONAray() throws Exception {
        System.out.println(getSkinbaronOpenSalesJSONAray().length());
    }

    public void testGetSkinbaronSalesForTable() throws Exception {
        getSkinbaronSalesForTable();
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_sales;"));
    }

    public void testGetSkinbaronInventory() throws Exception {
        getSkinbaronInventory(inventory);
    }

    public void testAddInventoryItemNegative() throws Exception {
        insertItemIntoInventory("Gibt es nicht",1,"Test Inv");
        insertInventory();
        assertTrue(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'Gibt es nicht' and inv_type='Test Inv'"));

    }

    public void testGetItemPricesInventory() throws Exception {
        setUpClass();
        getItemPricesInventory();
    }
}