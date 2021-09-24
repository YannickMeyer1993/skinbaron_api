package com.company;

import junit.framework.TestCase;

import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;

public class SteamItemPriceCheckerTest extends TestCase {

    public void testGetSteamPriceForGivenNamePositive() throws Exception {
        String itemName = "UMP-45 | Fade (Factory New)";
        assertTrue(getSteamPriceForGivenName(itemName)>0.0);
    }

    public void testGetSteamPriceForGivenNameNegative() throws Exception {
        String itemName = "Test Item Never will be found";
        assertTrue(getSteamPriceForGivenName(itemName)==0.0);
    }
}