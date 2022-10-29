package com.company.dataaccessobject;

import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PostgresHelper.*;
import static com.company.entrypoints.SteamAPI.getSteamPriceForGivenName;

public class PostgresDAOTest extends TestCase {

    public void testInit() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        assertFalse(checkIfResultsetIsEmpty("select * from information_schema.schemata s where schema_name = 'steam';"));
        assertFalse(checkIfResultsetIsEmpty("select * from information_schema.\"tables\" t \n" +
                "where table_name = 'steam_prices' and table_schema = 'steam';"));
        assertFalse(checkIfResultsetIsEmpty("select * from information_schema.\"tables\" t \n" +
                "where table_name = 'skinbaron_items' and table_schema = 'steam';"));
        assertTrue(checkIfResultsetIsEmpty("select name,count(*) from steam.steam_avg_prices sap\n" +
                "        group by name having count(*) > 1;"));
    }

    public void testAddSkinbaronItem() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        String ItemName = "Item Name";
        SkinbaronItem item = new SkinbaronItem("testTestAddSkinbaronItem",0d,ItemName,"",0d, "","");
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

    public void testinsertItemInformations() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.insertItemInformations();
        assertFalse(checkIfResultsetIsEmpty("select * from steam.item_informations"));
    }

    public void testcleanUp() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.cleanUp();
    }

    public void testTestAddSkinbaronItem() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        SkinbaronItem item = new SkinbaronItem("FakeID",3d,"AWP Drachlore","Keine",0.12345d, "","");
        dao.addSkinbaronItem(item);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='FakeID' and name='AWP Drachlore'"));
        executeDDL("delete from steam.skinbaron_items where id='FakeID' and name='AWP Drachlore'");
    }

    public void testInsertOverviewRow() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        dao.insertOverviewRow(3232d,2332d,323d);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.overview where steam_balance=3232 and steam_open_sales=2332 and skinbaron_balance=323;"));
        executeDDL("delete from steam.overview where \"DATE\"=current_date");
    }

    public void testViewCurrentSteamPrices() throws Exception {
        setUpClass();
        executeDDL("delete from steam.steam_prices where name = 'Sticker Capsule' and \"date\"= CURRENT_DATE");
        double result = getSteamPriceForGivenName("Sticker Capsule");

        assertFalse(checkIfResultsetIsEmpty("select * from steam.steam_current_prices where name = 'Sticker Capsule' and \"date\"= CURRENT_DATE and price_euro="+result));

        try (Connection connection = getConnection(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select count(*),name from steam.steam_current_prices group by name having count(*) > 1")) {
            assertFalse(rs.next());
        }
    }

    public void testViewCurrentBuffPrices() throws Exception {
        setUpClass();

        try (Connection connection = getConnection(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select count(*) from steam.item_informations");
             Statement st2 = connection.createStatement();
             ResultSet rs2 = st2.executeQuery("select count(*) from steam.buff_current_prices")) {
            rs.next();
            rs2.next();
            int count = rs.getInt("count");
            int count2 = rs2.getInt("count");
            assertTrue(count >= count2);
        }

        try (Connection connection = getConnection(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select count(*),id from steam.buff_current_prices group by id having count(*) > 1")) {
            assertFalse(rs.next());
        }
    }

    public void testViewInventoryWithPrices() throws Exception {
        try (Connection connection = getConnection(); Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select sum(amount) from steam.inventory where still_there = true");
             Statement st2 = connection.createStatement();
             ResultSet rs2 = st2.executeQuery("select sum(amount) from steam.inventory_current_prices")) {
            rs.next();
            rs2.next();
            int count = rs.getInt("sum");
            int count2 = rs2.getInt("sum");
            assertEquals(count, count2);
        }
    }

    public void testTestGetItemsToBuy() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        System.out.println(dao.getItemsToBuy());
    }

    public void testInsertNewestSales() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        String jsonResult = "{\n" +
                "\t\"newestSales30Days\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"itemName\": \"Operation Riptide Case\",\n" +
                "\t\t\t\"price\": 0.46,\n" +
                "\t\t\t\"wear\": 0,\n" +
                "\t\t\t\"dateSold\": \"2022-01-11\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"itemName\": \"Operation Riptide Case\",\n" +
                "\t\t\t\"price\": 0.65,\n" +
                "\t\t\t\"wear\": 0,\n" +
                "\t\t\t\"dateSold\": \"2022-01-11\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"itemName\": \"Operation Riptide Case\",\n" +
                "\t\t\t\"price\": 0.65,\n" +
                "\t\t\t\"wear\": 0,\n" +
                "\t\t\t\"dateSold\": \"2022-01-11\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";

        dao.insertNewestSales(jsonResult);
    }

    public void testInsertNewestSalesDoppler() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        String jsonResult = "{\n" +
                "\t\"newestSales30Days\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"itemName\": \"★ TEST DOPPLER\",\n" +
                "\t\t\t\"price\": 390.0,\n" +
                "\t\t\t\"wear\": 0.032865744,\n" +
                "\t\t\t\"dopplerPhase\": \"doppler-phase2\",\n" +
                "\t\t\t\"dateSold\": \"2022-01-12\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";

        dao.insertNewestSales(jsonResult);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_newest_sold_items where name='★ TEST DOPPLER'"));
        executeDDL("delete from steam.skinbaron_newest_sold_items where name='★ TEST DOPPLER' and doppler_phase='doppler-phase2'");
    }

    public void testInsertBuffPrices() throws Exception {
        String json = "[{\"id\":1222332233,\"price_euro\":4,\"has_exterior\":false}]";
        PostgresDAO dao = new PostgresDAO();
        JSONArray insert = new JSONArray(json);
        dao.insertBuffPrices(insert);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.buff_prices where id = 1222332233"));
        executeDDL("delete from steam.buff_prices where id = 1222332233");
    }

    public void testGetBuffIds() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        JSONArray array = new JSONArray(dao.getBuffIds());

        System.out.println(array.length());

        assertTrue(array.length()>=0);
    }

    public void testInsertCollections() throws Exception {
        setUpClass();
        PostgresDAO dao = new PostgresDAO();
        dao.insertCollections();

    }
}