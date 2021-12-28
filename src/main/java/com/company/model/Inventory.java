package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Item> InventoryList = new ArrayList();

    public void add(Item item) {
        InventoryList.add(item);
    }

    public List<Item> getInventoryList() {
        return InventoryList;
    }

    public void clearInventory() {
        InventoryList.clear();
    }

    public Double getValue() {
        Double value = 0d;
        for (Item item: InventoryList) {
            value = value + item.getSteamPrice();
        }
        return value;
    }
}
