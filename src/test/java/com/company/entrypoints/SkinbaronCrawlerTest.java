package com.company.entrypoints;

import junit.framework.TestCase;

import java.util.UUID;

import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static com.company.entrypoints.SkinbaronCrawler.Search;
import static com.company.entrypoints.SkinbaronCrawler.requestInsertSkinbaronItem;

public class SkinbaronCrawlerTest extends TestCase {

    public void testrequestInsertSkinbaronItem() throws Exception {
        UUID uuid = UUID.randomUUID();
        requestInsertSkinbaronItem(uuid.toString(),"Name",2d,"Keine",0.2333d);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
        executeDDL("delete from steam.skinbaron_items where id='"+uuid+"'");

    }

    public void testSearch() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        String after_saleid = "";
        //get an existing id

        String[] result = Search( secret, after_saleid,1);
        assertTrue(Integer.parseInt(result[0])>=0);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+result[1]+"'"));
        executeDDL("delete from steam.skinbaron_items where id='"+result[1]+"'");
    }
}