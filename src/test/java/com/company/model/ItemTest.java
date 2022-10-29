package com.company.model;

import junit.framework.TestCase;

import java.sql.Timestamp;

public class ItemTest extends TestCase {

    public void testGetAsJson() {
        Item item = new Item("name",new ItemCollection("Collection",true),"","","");
        item.addSkinbaronItemToList(new SkinbaronItem("ferfef",2.0,"name","no",0.3,"",""));
        item.addSkinbaronItemToList(new SkinbaronItem("ferfef",3.0,"name","no",0.5,"",""));
        item.addSteamPricetoList(new SteamPrice("",null,2.0,3000));

        item.print();

    }
}