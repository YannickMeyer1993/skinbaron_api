package de.yannickm.steambot.entrypoints;

import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;
import static de.yannickm.steambot.common.PostgresExecutor.checkIfResultsetIsEmpty;
import static de.yannickm.steambot.common.PostgresExecutor.executeDDL;
import static de.yannickm.steambot.entrypoints.InventoryCrawler.getItemsFromSteamHTTP;
import static de.yannickm.steambot.entrypoints.SteamAPI.requestInsertNewSteamprice;
import static de.yannickm.steambot.entrypoints.SteamAPI.*;

public class SteamAPITest extends TestCase {

    public void testRequestNewSteamprice() throws Exception {
        UUID uuid = UUID.randomUUID();
        requestInsertNewSteamprice(uuid.toString(), 0d, 1000,-100);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.steam_prices where name='" + uuid + "'"));
        executeDDL("delete from steam.steam_prices where name='" + uuid + "' and start_index = -100");
    }

    public void testGetSteamPriceForGivenName() throws Exception {
        double price = SteamAPI.getSteamPriceForGivenName("UMP | Fade (Factory New)");
        assertEquals(price, 0.0);
    }

    public void testGetSteamPriceForGivenNameNegative() throws Exception {
        double price = SteamAPI.getSteamPriceForGivenName("UMP-45 | Fade (Factory New)");
        assertTrue(price> 0.0);
    }

    public void testExtractItemsFromJSON() throws Exception {
        Scanner sc = new Scanner(new File("src/test/resources/entrypoints/SteamAPITest/InventoryJSON.json"));
        String testInventoryJSON = sc.nextLine();

        HashMap<String, Integer> map = getItemsFromSteamHTTP(testInventoryJSON);

        assert(map.size()>0);

        assertEquals((int)map.get("Sticker | Fnatic (Holo) | Cologne 2014"),4);

    }


    public void testTestGetSteamPriceForGivenName() throws Exception {
        setUpClass();
        double price = SteamAPI.getSteamPriceForGivenName("Name Tag");
        assertTrue(price> 1.5);
    }

    public void testRequestSearch() throws Exception {
        setUpClass();
        requestSearch(2000);
    }
}