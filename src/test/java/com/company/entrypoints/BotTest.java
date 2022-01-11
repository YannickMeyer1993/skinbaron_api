package com.company.entrypoints;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.entrypoints.Bot.*;
import static com.company.entrypoints.SkinbaronCrawler.requestInsertSkinbaronItem;

public class BotTest extends TestCase {

    public void testDeleteNonExistingSkinbaronItems() throws Exception {
        UUID uuid = UUID.randomUUID();
        requestInsertSkinbaronItem(uuid.toString(),"Name",2d,"Keine",0.2333d);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
        deleteNonExistingSkinbaronItems("Name",2d);
        assertTrue(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
    }

    public void testRequestDeleteSkinbaronId() {
        UUID uuid = UUID.randomUUID();
        requestDeleteSkinbaronId(uuid.toString());
    }

    public void testBuyItem() throws Exception {
        setUpClass();
        String uuid = UUID.randomUUID().toString();
        requestInsertSkinbaronItem(uuid,"NAME",3.11,"",0.123456);
        buyItem(uuid,3.11,5d);
        assertTrue(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
    }

    public void testGetItemsToBuy() {
        JSONArray array = getItemsToBuy();
        for (Object o: array) {
            if (o instanceof JSONObject) {
                System.out.println(o);
            }
        }
    }

    //TODO
    public void testTestBuyItem() {
        // StatTrak™ Negev | Loudmouth (Field-Tested) 27d551f1-43e6-4e32-9951-8cd7716508c3 1.22
        // Test that this get deleted
    }
}