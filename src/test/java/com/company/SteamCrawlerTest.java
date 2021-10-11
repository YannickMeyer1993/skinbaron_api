package com.company;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.company.SteamCrawler.*;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

public class SteamCrawlerTest extends TestCase {

    public void testSetIterationCounter() throws FileNotFoundException, SQLException {
        Connection conn = getConnection();

        System.out.println(Date.valueOf("2000-01-01").toString());
        setIterationCounter(conn,0);

        conn.close();

    }

    @SuppressWarnings("EmptyMethod")
    public void testExtractValuesFromJSON() {
        //TODO
    }

    public void testUpdateItemPricesLongNotSeen() throws Exception {
        Connection conn = getConnection();

        updateItemPricesLongNotSeen(conn);
    }

    public void testUpdateItemPrices0Euro() throws Exception {
        Connection conn = getConnection();

        updateItemPrices0Euro(conn);
    }
}