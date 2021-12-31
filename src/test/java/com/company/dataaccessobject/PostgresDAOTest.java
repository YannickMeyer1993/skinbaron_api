package com.company.dataaccessobject;

import com.company.model.Price;
import com.company.model.SkinbaronItem;
import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    public void testAddSkinbaronItem() {
    }

    public void testAddSteamPrice() {
    }

    public void testGetItemsToBuy() {
    }

    public void testTestAddSkinbaronItem() throws Exception {
        PostgresDAO dao = new PostgresDAO();
        SkinbaronItem item = new SkinbaronItem("testTestAddSkinbaronItem",new Price(null,0d),"Item Name","Keine Sticker",0d);
        dao.addSkinbaronItem(item);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id = 'testTestAddSkinbaronItem'"));
        executeDDL("DELETE from steam.skinbaron_items where id = 'testTestAddSkinbaronItem'");
    }
}