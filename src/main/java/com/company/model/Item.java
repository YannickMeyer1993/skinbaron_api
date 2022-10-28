package com.company.model;

import java.util.ArrayList;
import java.util.List;

import static com.company.common.Constants.*;

public class Item {
    private final String name;

    private final ItemCollection collection;

    private Price CurrentSteamPrice = null;
    private Price CheapestSkinbaronPrice = null;

    private final List<SkinbaronItem> SkinbaronItemList = new ArrayList<>();
    private final List<SteamPrice> SteamPriceList = new ArrayList<>();

    public Item(String ItemName, ItemCollection collection) {
        this.name = ItemName;
        this.collection = collection;
    }

    public String getName() {
        return name;
    }

    public Price getSteamPrice() {
        return CurrentSteamPrice;
    }

    /**
    sets the current steam price
    triggered if there is a new steam price
     */
    public void setSteamPrice(Price steamPrice) {
        CurrentSteamPrice = steamPrice;
    }

    public Price getSkinbaronPrice() {
        return CheapestSkinbaronPrice;
    }

    /**
    sets the lowest current Skinbaron price
    triggered after change in SkinbaronItem list
     */
    public void setSkinbaronPrice(Price price) {
        if (CheapestSkinbaronPrice==null) {
            CheapestSkinbaronPrice = price;
            return;
        }

        for (SkinbaronItem item: SkinbaronItemList) {
            if (item.getPrice().getValue() < CheapestSkinbaronPrice.getValue()) {
                CheapestSkinbaronPrice = item.getPrice();
            }
        }
    }

    public ItemCollection getCollection() {
        return collection;
    }

    public String toString() {

        String result = "";
        result = result.concat("Item Name: "+name)
                .concat("\nCollection Name: "+collection.getName())
                .concat("\nSteam Price: "+CurrentSteamPrice.getValue())
                .concat("\nSkinbaron Price: "+CheapestSkinbaronPrice.getValue());
        return result;
    }

    public List<SkinbaronItem> getSkinbaronItemList() {
        return SkinbaronItemList;
    }

    public List<SteamPrice> getSteamPriceList() {
        return SteamPriceList;
    }

    public void addSkinbaronItemToList(SkinbaronItem item) {
        SkinbaronItemList.add(item);
    }

    public void addSteamPricetoList(SteamPrice price) {
        SteamPriceList.add(price);
    }

    public void deleteSkinbaronItemFromList(SkinbaronItem item) {
        SkinbaronItemList.remove(item);
    }

    public void deleteSteamPriceFromList(SteamPrice price) {
        SteamPriceList.remove(price);
    }
}
