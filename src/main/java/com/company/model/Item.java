package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private final String name;

    private ItemCollection collection;

    private Price SteamPrice = null;
    private Price SkinbaronPrice = null;

    private List<SkinbaronItem> SkinbaronItemList = new ArrayList<>();
    private List<SteamPrice> SteamPriceList = new ArrayList<>();

    public Item(String ItemName) {
        this.name = ItemName;
    }

    public String getName() {
        return name;
    }

    public Price getSteamPrice() {
        return SteamPrice;
    }

    /*
    sets the current steam price
    triggered if there is a new steam price
     */
    public void setSteamPrice(Price steamPrice) {
        SteamPrice = steamPrice;
    }

    public Price getSkinbaronPrice() {
        return SkinbaronPrice;
    }

    /*
    sets the lowest current Skinbaron price
    triggered after change in SkinbaronItem list
     */
    public void setSkinbaronPrice(Price price) {
        if (SkinbaronPrice==null) {
            SkinbaronPrice = price;
            return;
        }

        for (SkinbaronItem item: SkinbaronItemList) {
            if (item.getPrice().getValue() < SkinbaronPrice.getValue()) {
                SkinbaronPrice = item.getPrice();
            }
        }
    }

    public ItemCollection getCollection() {
        return collection;
    }

    public void setCollection(ItemCollection collection) {
        this.collection = collection;
    }

    public String toString() {

        StringBuilder result = new StringBuilder();
        result.append("Item Name: "+name)
                .append("\nCollection Name: "+collection.getName())
                .append("\nSteam Price: "+SteamPrice.getValue())
                .append("\nSkinbaron Price: "+SkinbaronPrice.getValue());
        return result.toString();
    }
}
