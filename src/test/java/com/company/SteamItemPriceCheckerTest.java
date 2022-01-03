package com.company;

import junit.framework.TestCase;

import java.sql.Connection;

import static com.company.old.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.old.helper.getConnection;

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