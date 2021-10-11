package com.company;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

public class SteamItemPriceCheckerTest extends TestCase {

    public void testGetSteamPriceForGivenNamePositive() throws Exception {

        Connection conn = getConnection();

        String itemName = "UMP-45 | Fade (Factory New)";
        assertTrue(getSteamPriceForGivenName(itemName,conn)>0.0);
    }

    public void testGetSteamPriceForGivenNameNegative() throws Exception {

        Connection conn = getConnection();

        String itemName = "Test Item Never will be found";
        assertEquals(0.0, getSteamPriceForGivenName(itemName, conn));
    }
}