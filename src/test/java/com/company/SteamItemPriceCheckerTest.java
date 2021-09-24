package com.company;

import junit.framework.TestCase;

import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;

public class SteamItemPriceCheckerTest extends TestCase {

    public void testGetSteamPriceForGivenName() throws Exception {
        String itemName = "UMP-45 | Fade (Factory New)";
        assertTrue(getSteamPriceForGivenName(itemName)>0.0);
    }
}