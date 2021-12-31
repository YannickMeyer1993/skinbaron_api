package com.company;

import junit.framework.TestCase;

import java.sql.Connection;

import static com.company.Overview.getItemPricesInventory;
import static com.company.helper.getConnection;

public class OverviewTest extends TestCase {

    public void testGetItemPricesInventory() throws Exception {

        try(Connection conn = getConnection()) {
            getItemPricesInventory(conn);
        }
    }
}