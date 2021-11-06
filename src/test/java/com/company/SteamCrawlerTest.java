package com.company;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

import static com.company.SteamCrawler.*;
import static com.company.common.getConnection;

public class SteamCrawlerTest extends TestCase {

    public void testSetIterationCounter() throws FileNotFoundException, SQLException {
        try(Connection conn = getConnection()) {
            setIterationCounter(conn, 0);
        }
    }

    public void testUpdateItemPricesLongNotSeen() throws Exception {
        Connection conn = getConnection();

        updateItemPricesLongNotSeen(conn);
    }

    public void testUpdateItemPrices0Euro() throws Exception {
        Connection conn = getConnection();

        updateItemPrices0Euro(conn);
    }

    public void testGetInventory() throws Exception {
        Connection conn = getConnection();
        getItemsfromInventory(conn,"https://steamcommunity.com/inventory/76561198286004569/730/2?count=2000","steam");
    }

    public void testExtractItemsFromJSON() throws Exception {
        Scanner sc = new Scanner(new File("src/test/resources/SteamCrawler/InventoryJSON.json"));
        String testInventoryJSON = sc.nextLine();

        HashMap<String, Integer> map = getItemsFromSteamHTTP(testInventoryJSON);

        assert(map.size()>0);

        assertEquals((int)map.get("Sticker | Fnatic (Holo) | Cologne 2014"),4);

    }
}