package com.company.model;

import junit.framework.TestCase;

import java.sql.Timestamp;

public class ItemTest extends TestCase {

    public void testToString() {
        Item item = new Item("Kniff");
        item.setCollection(new ItemCollection("Cobble",false));
        item.setSteamPrice(new Price(new Timestamp(System.currentTimeMillis()) ,1d));
        item.setSkinbaronPrice(new Price(new Timestamp(System.currentTimeMillis()),2d));

        assertEquals(item.toString(),"Item Name: Kniff\n" +
                "Collection Name: Cobble\n" +
                "Steam Price: 1.0\n" +
                "Skinbaron Price: 2.0");
    }
}