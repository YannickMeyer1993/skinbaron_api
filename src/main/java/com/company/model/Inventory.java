package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final List<InventoryItem> InventoryList;

    public Inventory() {
        InventoryList = new ArrayList<>();
    }

    public void add(InventoryItem item) {
        InventoryList.add(item);
    }

    public List<InventoryItem> getInventoryList() {
        return InventoryList;
    }

    public void clearInventory() {
        InventoryList.clear();
    }

}
