package com.company.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.company.common.Constants.*;

public class Item {
    private final String name;

    private ItemCollection collection;

    private Price currentSteamPrice = null;
    private Price cheapestSkinbaronPrice = null;

    //Map of Item Name as Key and String[name,weapon,collection,quality,name_without_exterior]
    private String weaponType;
    private String quality;
    private String nameWithoutExterior;

    private final List<SkinbaronItem> skinbaronItemList = new ArrayList<>();
    private final List<SteamPrice> steamPriceList = new ArrayList<>();

    public Item(String itemName, ItemCollection collection,String weaponType,String quality,String nameWithoutExterior) {
        this.name = itemName;
        this.collection = collection;
        this.weaponType = weaponType;
        this.quality = quality;
        this.nameWithoutExterior = nameWithoutExterior;
    }

    public void print() {
        System.out.println(getAsJson().toString());
    }

    public JSONObject getAsJson() {
        JSONObject item = new JSONObject();
        item.put("name",name);
        item.put("collection",collection.getName());
        if (currentSteamPrice!=null) {
            item.put("currentSteamPrice", currentSteamPrice.getValue());
        }
        if (cheapestSkinbaronPrice!=null) {
            item.put("cheapestSkinbaronPrice", cheapestSkinbaronPrice.getValue());
        }
        item.put("skinbaronItemList",new JSONArray(skinbaronItemList));
        item.put("steamPriceList",new JSONArray(steamPriceList));

        return item;
    }

    public String getName() {
        return name;
    }

    public Price getSteamPrice() {
        return currentSteamPrice;
    }

    /**
    sets the current steam price
    triggered if there is a new steam price
     */
    private void setSteamPrice(Price steamPrice) {
        currentSteamPrice = steamPrice;
    }

    public Price getSkinbaronPrice() {
        return cheapestSkinbaronPrice;
    }

    /**
    sets the lowest current Skinbaron price
    triggered after change in SkinbaronItem list
     */
    private void setSkinbaronPrice(Price price) {
        if (cheapestSkinbaronPrice==null) {
            cheapestSkinbaronPrice = price;
            return;
        }

        for (SkinbaronItem item: skinbaronItemList) {
            if (item.getPrice().getValue() < cheapestSkinbaronPrice.getValue()) {
                cheapestSkinbaronPrice = item.getPrice();
            }
        }
    }

    public ItemCollection getCollection() {
        return collection;
    }

    public List<SkinbaronItem> getSkinbaronItemList() {
        return skinbaronItemList;
    }

    public List<SteamPrice> getSteamPriceList() {
        return steamPriceList;
    }

    public void addSkinbaronItemToList(SkinbaronItem item) {
        skinbaronItemList.add(item);

        setSkinbaronPrice(item.getPrice());
    }

    public void addSteamPricetoList(SteamPrice price) {
        steamPriceList.add(price);
        setSteamPrice(price);
    }

    public void deleteSkinbaronItemFromList(SkinbaronItem item) {
        skinbaronItemList.remove(item);
    }

    public void deleteSteamPriceFromList(SteamPrice price) {
        steamPriceList.remove(price);
    }
}
