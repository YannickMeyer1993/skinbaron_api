package com.company.model;

import junit.framework.TestCase;

import java.sql.Timestamp;

public class ItemTest extends TestCase {

    public void testToString() {
        String ItemName = "Kniff";

        Item item = new Item(ItemName, new ItemCollection("Cobble",false));
        item.setSteamPrice(new Price(new Timestamp(System.currentTimeMillis()) ,1d, ItemName));
        item.setSkinbaronPrice(new Price(new Timestamp(System.currentTimeMillis()),2d, ItemName));

        assertEquals(item.toString(),"Item Name: Kniff\n" +
                "Collection Name: Cobble\n" +
                "Steam Price: 1.0\n" +
                "Skinbaron Price: 2.0");
    }
}