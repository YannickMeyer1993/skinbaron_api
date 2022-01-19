package com.company.entrypoints;

import junit.framework.TestCase;
import org.json.JSONObject;

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

    public void testRequestInsertSoldSkinbaronItem() throws Exception {
        String json = "{\"classid\":\"533226233\",\"last_updated\":1642596533,\"instanceid\":\"143865972\",\"list_time\":1642493665,\"price\":1.6,\"assetid\":\"24420146865\",\"appid\":730,\"name\":\"Name Tag\",\"txid\":\"PR7PTTEZ8224\",\"commission\":0.24,\"id\":\"TEST\",\"state\":4}\n";
        requestInsertSoldSkinbaronItem(new JSONObject(json));
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_sold_items where id='TEST'"));
        executeDDL("delete from steam.skinbaron_sold_items where id='TEST'");
    }

    public void testGetLastSoldSkinbaronId() throws Exception {
        String json = "{\"classid\":\"533226233\",\"last_updated\":1642596533,\"instanceid\":\"143865972\",\"list_time\":1642493665,\"price\":1.6,\"assetid\":\"24420146865\",\"appid\":730,\"name\":\"Name Tag\",\"txid\":\"PR7PTTEZ8224\",\"commission\":0.24,\"id\":\"TEST\",\"state\":4}\n";
        requestInsertSoldSkinbaronItem(new JSONObject(json));
        assertEquals(getLastSoldSkinbaronId(), "TEST");
        executeDDL("delete from steam.skinbaron_items where id='TEST'");
    }

    public void testGetSoldItems() throws Exception {
        setUpClass();
        getSoldItems();
    }
}