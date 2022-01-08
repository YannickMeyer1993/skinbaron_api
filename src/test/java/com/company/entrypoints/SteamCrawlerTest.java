package com.company.entrypoints;

import com.company.dataaccessobject.PostgresDAO;
import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static com.company.entrypoints.InventoryCrawler.getItemsFromSteamHTTP;
import static com.company.entrypoints.SteamCrawler.*;

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

    public void testExtractItemsFromJSON() throws Exception {
        Scanner sc = new Scanner(new File("src/test/resources/entrypoints/SteamCrawlerTest/InventoryJSON.json"));
        String testInventoryJSON = sc.nextLine();

        HashMap<String, Integer> map = getItemsFromSteamHTTP(testInventoryJSON);

        assert(map.size()>0);

        assertEquals((int)map.get("Sticker | Fnatic (Holo) | Cologne 2014"),4);

    }

    public void testGetHighestSteamIteration() throws Exception {
        executeDDL("delete from steam.steam_iteration where \"date\" = CURRENT_DATE;");
        assertEquals(getHighestSteamIteration(),0);
    }

    public void testSetHighestSteamIteration() throws Exception {
        int old = getHighestSteamIteration();
        setHighestSteamIteration(10);
        assertEquals(getHighestSteamIteration(),10);
        setHighestSteamIteration(old);
    }
}