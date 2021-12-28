package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private final String name;

    private ItemCollection collection;

    private Double SteamPrice = null;
    private Double SkinbaronPrice = null;

    private List<SkinbaronItem> SkinbaronItemList = new ArrayList<>();
    private List<SteamPrice> SteamPriceList = new ArrayList<>();

    public Item(String ItemName) {
        this.name = ItemName;
    }

    public String getName() {
        return name;
    }

    public Double getSteamPrice() {
        return SteamPrice;
    }

    /*
    sets the current steam price
    triggered if there is a new steam price
     */
    public void setSteamPrice(Double steamPrice) {
        SteamPrice = steamPrice;
    }

    public Double getSkinbaronPrice() {
        return SkinbaronPrice;
    }

    /*
    sets the lowest current Skinbaron price
    triggered after change in SkinbaronItem list
     */
    public void setSkinbaronPrice(Double price) {
        if (SkinbaronPrice==null) {
            SkinbaronPrice = price;
            return;
        }

        for (SkinbaronItem item: SkinbaronItemList) {
            if (item.getPrice() < SkinbaronPrice) {
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
}
