package com.company.entrypoints;

import junit.framework.TestCase;

import java.util.UUID;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static com.company.entrypoints.SkinbaronCrawler.requestInsertSkinbaronItem;

public class SkinbaronCrawlerTest extends TestCase {

    public void testrequestInsertSkinbaronItem() throws Exception {
        UUID uuid = UUID.randomUUID();
        requestInsertSkinbaronItem(uuid.toString(),"Name",2d,"Keine",0.2333d);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
        executeDDL("delete from steam.skinbaron_items where id='"+uuid+"'");

    }

    public void testSearch() {
        //TODO testen
    }
}