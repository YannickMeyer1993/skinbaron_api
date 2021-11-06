package com.company;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.company.Overview.getItemPricesInventory;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

public class OverviewTest extends TestCase {

    public void testGetItemPricesInventory() throws Exception {

        try(Connection conn = getConnection()) {
            getItemPricesInventory(conn);
        }
    }
}