package com.company;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.company.SteamCrawler.*;
import static com.company.common.readPasswordFromFile;

public class SteamCrawlerTest extends TestCase {

    public void testSetIterationCounter() throws FileNotFoundException, SQLException {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        System.out.println(Date.valueOf("2000-01-01").toString());
        setIterationCounter(conn,0);

        conn.close();

    }

    @SuppressWarnings("EmptyMethod")
    public void testExtractValuesFromJSON() {
        //TODO
    }

    public void testUpdateItemPricesLongNotSeen() throws Exception {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        updateItemPricesLongNotSeen(conn);
    }

    public void testUpdateItemPrices0Euro() throws Exception {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        updateItemPrices0Euro(conn);
    }
}