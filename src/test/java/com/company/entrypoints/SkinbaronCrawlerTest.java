package com.company.entrypoints;

import junit.framework.TestCase;

import java.util.UUID;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static com.company.entrypoints.SkinbaronCrawler.*;

public class SkinbaronCrawlerTest extends TestCase {

    public void testrequestInsertSkinbaronItem() throws Exception {
        UUID uuid = UUID.randomUUID();
        requestInsertSkinbaronItem(uuid.toString(),"Name",2d,"Keine",0.2333d,"","");
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

    public void testGetLastSkinbaronId() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        String after_saleid = "";
        //get an existing id

        String[] result = Search( secret, after_saleid,1);
        assertTrue(Integer.parseInt(result[0])>=0);

        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+result[1]+"'"));

        assertEquals(getLastSkinbaronId(), result[1]);
        executeDDL("delete from steam.skinbaron_items where id='"+result[1]+"'");


    }

    //TODO
    public void testRequestInsertSoldSkinbaronItem() throws Exception {
        //requestInsertSoldSkinbaronItem("testRequestInsertSoldSkinbaronItem","Operation Riptide Case",0,"4578724859","1641446620","519977179","1640260702","24258366051","PZZWWTZFS9FT",0);
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_sold_items where id='testRequestInsertSoldSkinbaronItem'"));
        executeDDL("delete from steam.skinbaron_sold_items where id='testRequestInsertSoldSkinbaronItem'");
    }

    //TODO
    public void testGetLastSoldSkinbaronId() throws Exception {
        //requestInsertSoldSkinbaronItem("testGetLastSoldSkinbaronId","Operation Case",0,"4578724859","1641446620","519977179","1640260702","24258366051","PZZWWTZFS9FT",0);

        assertEquals(getLastSoldSkinbaronId(), "testGetLastSoldSkinbaronId");
        executeDDL("delete from steam.skinbaron_items where id='testGetLastSoldSkinbaronId'");
    }

    public void testGetSoldItems() throws Exception {
        setUpClass();
        getSoldItems();
    }
}