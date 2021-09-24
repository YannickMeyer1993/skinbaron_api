package com.company;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.company.Overview.getItemPricesInventory;
import static com.company.common.readPasswordFromFile;

public class OverviewTest extends TestCase {

    public void testGetItemPricesInventory() throws Exception {

        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        getItemPricesInventory(conn);

        conn.close();
    }
}