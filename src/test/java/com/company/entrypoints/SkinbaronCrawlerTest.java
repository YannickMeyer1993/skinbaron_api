package com.company.entrypoints;

import junit.framework.TestCase;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.UUID;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static com.company.entrypoints.SkinbaronCrawler.*;

public class SkinbaronCrawlerTest extends TestCase {

    public void testrequestInsertSkinbaronItem() throws Exception {
        UUID uuid = UUID.randomUUID();
        String json = "{\"img\":\"https://steamcommunity-a.akamaihd.net/economy/image/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXO9B9WLbU5oA9OA1_TRvahz93XbA0ma11T4On1L1Jjh6qQJWxBv4nvkdaPkvGkMbiDkz9UupFz3LvDp4qn2wf6ux070bHpuI4\",\"price\":1.54,\"appid\":730,\"sbinspect\":\"https://skinbaron.de/offers/show?offerUuid=4b8e92a2-fedd-48c6-b1cc-a2afc6eee35f\",\"stickers\":\"\",\"id\":\""+uuid+"\",\"market_name\":\"Music Kit | Scarlxrd, CHAIN$AW.LXADXUT.\"}";
        requestInsertSkinbaronItem(new JSONObject(json));
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
        executeDDL("delete from steam.skinbaron_items where id='"+uuid+"'");

    }

    public void testSearch() throws Exception {
        setUpClass();
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        String after_saleid = "";
        //get an existing id

        String[] result = Search( secret, after_saleid,1);
        System.out.println(Arrays.toString(result));
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

    public void testGetPriceList() throws Exception {
        setUpClass();
        getPriceList();
    }
}