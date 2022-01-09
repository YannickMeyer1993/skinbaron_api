package com.company.dataaccessobject;

import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PostgresHelper.*;
import static com.company.entrypoints.SteamCrawler.getSteamPriceForGivenName;

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
        dao.addInventoryItem("AWP | Dragon Lore (Factory New)",1,"Test Inv");
        assertFalse(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'AWP | Dragon Lore (Factory New)' and inv_type='Test Inv'"));
        executeDDL("DELETE FROM steam.inventory where name = 'AWP | Dragon Lore (Factory New)'");
    }

    public void testAddInventoryItemNegative() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.addInventoryItem("Wrong Name",1,"Test Inv");
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
        assertFalse(checkIfResultsetIsEmpty("select * from steam.overview where steam_balance=3232 and steam_open_sales=2332 and skinbaron_balance=323;"));
        executeDDL("delete from steam.overview where \"DATE\"=current_date");
    }

    public void testViewCurrentSteamPrices() throws Exception {
        setUpClass();
        double result = getSteamPriceForGivenName("Sticker Capsule");

        //Could be wrong if price changes within a day and after the second run because minimum
        assertFalse(checkIfResultsetIsEmpty("select * from steam.steam_current_prices where name = 'Sticker Capsule' and \"date\"= CURRENT_DATE and price_euro="+result));

        try (Connection connection = getConnection(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select count(*) from steam.item_informations");
             Statement st2 = connection.createStatement();
             ResultSet rs2 = st2.executeQuery("select count(*) from steam.steam_current_prices")) {
            rs.next();
            rs2.next();
            int count = rs.getInt("count");
            int count2 = rs2.getInt("count");
            assertTrue(count <= count2);
        }

        try (Connection connection = getConnection(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select count(*),name from steam.steam_current_prices group by name having count(*) > 1")) {
            assertFalse(rs.next());
        }
    }

    public void testViewInventoryWithPrices() throws Exception {
        try (Connection connection = getConnection(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select sum(amount) from steam.inventory where still_there = true");
             Statement st2 = connection.createStatement();
             ResultSet rs2 = st2.executeQuery("select sum(amount) from steam.inventory_with_prices")) {
            rs.next();
            rs2.next();
            int count = rs.getInt("sum");
            int count2 = rs2.getInt("sum");
            assertEquals(count, count2);
        }
    }
}