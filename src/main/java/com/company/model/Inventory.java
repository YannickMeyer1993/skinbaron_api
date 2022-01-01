package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final List<Item> InventoryList;

    public Inventory() {
        InventoryList = new ArrayList<>();
    }

    public void add(Item item) {
        InventoryList.add(item);
    }

    public List<Item> getInventoryList() {
        return InventoryList;
    }

    public void clearInventory() {
        InventoryList.clear();
    }

    public double getValue() {
        double value = 0d;
        for (Item item: InventoryList) {
            value = value + item.getSteamPrice().getValue();
        }
        return value;
    }
}
