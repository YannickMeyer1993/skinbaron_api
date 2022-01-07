package com.company.entrypoints;

import junit.framework.TestCase;

import java.util.UUID;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.entrypoints.Bot.deleteNonExistingSkinbaronItems;
import static com.company.entrypoints.SkinbaronCrawler.requestInsertSkinbaronItem;

public class BotTest extends TestCase {

    public void testDeleteNonExistingSkinbaronItems() throws Exception {
        UUID uuid = UUID.randomUUID();
        requestInsertSkinbaronItem(uuid.toString(),"Name",2d,"Keine",0.2333d);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
        deleteNonExistingSkinbaronItems("Name",2d);
        assertTrue(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
    }
}