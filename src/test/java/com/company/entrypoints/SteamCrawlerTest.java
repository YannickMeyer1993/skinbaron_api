package com.company.entrypoints;

import junit.framework.TestCase;

import java.util.UUID;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static com.company.entrypoints.SteamCrawler.requestInsertNewSteamprice;

public class SteamCrawlerTest extends TestCase {

    public void testRequestNewSteamprice() throws Exception {
        UUID uuid = UUID.randomUUID();
        requestInsertNewSteamprice(uuid.toString(), 0d, 1000);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.steam_prices where name='" + uuid + "'"));
        executeDDL("delete from steam.steam_prices where name='" + uuid + "'");
    }

    public void testGetSteamPriceForGivenName() throws Exception {
        double price = SteamCrawler.getSteamPriceForGivenName("UMP | Fade (Factory New)");
        assertEquals(price, 0.0);
    }

    public void testGetSteamPriceForGivenNameNegative() throws Exception {
        double price = SteamCrawler.getSteamPriceForGivenName("UMP-45 | Fade (Factory New)");
        assertTrue(price> 0.0);
    }
}