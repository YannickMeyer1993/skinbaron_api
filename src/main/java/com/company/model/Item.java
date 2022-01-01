package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private final String name;

    private ItemCollection collection;

    private Price SteamPrice = null;
    private Price SkinbaronPrice = null;
    private List<String> exists_in_inventory = null;

    private final List<SkinbaronItem> SkinbaronItemList = new ArrayList<>();
    private final List<SteamPrice> SteamPriceList = new ArrayList<>();

    public Item(String ItemName) {
        this.name = ItemName;
    }

    public String getName() {
        return name;
    }

    public Price getSteamPrice() {
        return SteamPrice;
    }

    /**
    sets the current steam price
    triggered if there is a new steam price
     */
    public void setSteamPrice(Price steamPrice) {
        SteamPrice = steamPrice;
    }

    public Price getSkinbaronPrice() {
        return SkinbaronPrice;
    }

    /**
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

        String result = "";
        result = result.concat("Item Name: "+name)
                .concat("\nCollection Name: "+collection.getName())
                .concat("\nSteam Price: "+SteamPrice.getValue())
                .concat("\nSkinbaron Price: "+SkinbaronPrice.getValue());
        return result;
    }

    /**
     * Inventory Type must be one of steam, storage, skinbaron, skinbaron sales, smurf
     */
    public void addToInventory(String InventoryType){
        List<String> AllowedInventoryTypes = new ArrayList<>();
        AllowedInventoryTypes.add("steam");
        AllowedInventoryTypes.add("storage");
        AllowedInventoryTypes.add("skinbaron");
        AllowedInventoryTypes.add("skinbaron sales");
        AllowedInventoryTypes.add("smurf");
        if (AllowedInventoryTypes.contains(InventoryType)) {
            throw new IllegalArgumentException("Unknown Inventory Type");
        }
        exists_in_inventory.add(InventoryType);

    }

    public void clearInventory() {
        exists_in_inventory = null;
    }
}
