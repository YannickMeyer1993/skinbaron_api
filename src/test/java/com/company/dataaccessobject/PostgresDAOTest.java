package com.company.dataaccessobject;

import com.company.model.InventoryItem;
import com.company.model.Price;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import junit.framework.TestCase;

import java.io.IOException;
import java.sql.SQLException;

import static com.company.common.PostgresHelper.*;

public class PostgresDAOTest extends TestCase {

    public void testInit() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.init();
        assertFalse(checkIfResultsetIsEmpty("select * from information_schema.schemata s where schema_name = 'steam';"));
        assertFalse(checkIfResultsetIsEmpty("select * from information_schema.\"tables\" t \n" +
                "where table_name = 'steam_prices' and table_schema = 'steam';"));
        assertFalse(checkIfResultsetIsEmpty("select * from information_schema.\"tables\" t \n" +
                "where table_name = 'skinbaron_items' and table_schema = 'steam';"));

    }

    public void testGetItemsToBuy() {
    }

    public void testAddSkinbaronItem() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        String ItemName = "Item Name";
        SkinbaronItem item = new SkinbaronItem("testTestAddSkinbaronItem",0d,ItemName,"",0d);
        dao.addSkinbaronItem(item);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id = 'testTestAddSkinbaronItem'"));
        executeDDL("DELETE from steam.skinbaron_items where id = 'testTestAddSkinbaronItem'");
    }

    public void testAddSteamPrice() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        String ItemName = "Item Name";
        SteamPrice price = new SteamPrice(ItemName,null,10d,10);
        dao.addSteamPrice(price);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.steam_prices where name = 'Item Name'"));
        executeDDL("DELETE from steam.steam_prices where name = 'Item Name'");
    }

    public void testSteamIteration() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.init();
        int iteration = dao.getHighestSteamIteration();
        System.out.println(iteration);
        dao.initHightestSteamIteration();
        dao.setHighestSteamIteration(iteration);

        assertEquals(dao.getHighestSteamIteration(),iteration);
    }

    public void testAddInventoryItem() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        InventoryItem item = new InventoryItem("AWP | Dragon Lore (Factory New)","Test Inv");
        dao.addInventoryItem(item);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'AWP | Dragon Lore (Factory New)' and inv_type='Test Inv'"));
        executeDDL("DELETE FROM steam.inventory where name = 'AWP | Dragon Lore (Factory New)'");
    }

    public void testAddInventoryItemNegative() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        InventoryItem item = new InventoryItem("Wrong Name","Test Inv");
        assertTrue(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'Wrong Name'"));
    }

    public void testCrawlItemInformations() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.crawlItemInformations();
        assertFalse(checkIfResultsetIsEmpty("select * from steam.item_informations"));
    }

    public void testcleanUp() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.cleanUp();
    }

    public void testTestAddSkinbaronItem() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        SkinbaronItem item = new SkinbaronItem("FakeID",3d,"AWP Drachlore","Keine",0.12345d);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='FakeID' and name='AWP Drachlore'"));
        executeDDL("delete from steam.skinbaron_items where id='FakeID' and name='AWP Drachlore'");
    }

    public void testGetHighestSteamIteration() throws Exception {
        executeDDL("delete from steam.steam_iteration where \"date\" = CURRENT_DATE;");
        //new day
        PostgresDAO dao = new PostgresDAO();
        int result = dao.getHighestSteamIteration();
        assertEquals(result,0);
    }

    public void testInsertOverviewRow() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.insertOverviewRow(3232d,2332d,323d);
        //executeDDL("delete from steam.overview where \"DATE\"=current_date");
    }

    public void testInventoryWithPricesAndCurrentSteamPrices() throws Exception {
        //TODO
    }
}