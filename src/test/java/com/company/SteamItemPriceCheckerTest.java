package com.company;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.common.readPasswordFromFile;

public class SteamItemPriceCheckerTest extends TestCase {

    public void testGetSteamPriceForGivenNamePositive() throws Exception {

        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        String itemName = "UMP-45 | Fade (Factory New)";
        assertTrue(getSteamPriceForGivenName(itemName,conn)>0.0);
    }

    public void testGetSteamPriceForGivenNameNegative() throws Exception {

        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        String itemName = "Test Item Never will be found";
        assertTrue(getSteamPriceForGivenName(itemName,conn)==0.0);
    }
}