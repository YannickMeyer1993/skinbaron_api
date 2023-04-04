package de.yannickm.steambot.entrypoints;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;
import static de.yannickm.steambot.common.PostgresExecutor.checkIfResultsetIsEmpty;
import static de.yannickm.steambot.entrypoints.Bot.*;
import static de.yannickm.steambot.entrypoints.SkinbaronCrawler.requestInsertSkinbaronItem;

public class BotTest extends TestCase {

    public void testDeleteNonExistingSkinbaronItems() throws Exception {
        UUID uuid = UUID.randomUUID();
        String json = "{\"img\":\"bild\",\"price\":1.54,\"appid\":730,\"sbinspect\":\"https://skinbaron.de/offers/show?offerUuid=4b8e92a2-fedd-48c6-b1cc-a2afc6eee35f\",\"stickers\":\"\",\"id\":\""+uuid+"\",\"market_name\":\"Name\"}";
        requestInsertSkinbaronItem(new JSONObject(json));
        assertFalse(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
        deleteNonExistingSkinbaronItems("Name",1.54);
        assertTrue(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='"+uuid+"'"));
    }

    public void testRequestDeleteSkinbaronId() {
        UUID uuid = UUID.randomUUID();
        requestDeleteSkinbaronId(uuid.toString());
    }

    public void testBuyItem() throws Exception {
        setUpClass();
        String uuid = UUID.randomUUID().toString();
        String json = "{\"img\":\"bild\",\"price\":3.11,\"appid\":730,\"sbinspect\":\"https://skinbaron.de/offers/show?offerUuid=4b8e92a2-fedd-48c6-b1cc-a2afc6eee35f\",\"stickers\":\"\",\"id\":\""+uuid+"\",\"market_name\":\"Name\"}";
        requestInsertSkinbaronItem(new JSONObject(json));
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

    public void testTestBuyItem() throws Exception {
        buyItem("27d551f1-43e6-4e32-9951-8cd7716508c3",1.22,2);
        assertTrue(checkIfResultsetIsEmpty("select * from steam.skinbaron_items where id='27d551f1-43e6-4e32-9951-8cd7716508c3'"));
    }

    public void testTestBuyItem1() throws Exception {
        buyItem("1cb8423b-4491-4e44-889e-a442d111bf1e",0.05,1);
    }
}