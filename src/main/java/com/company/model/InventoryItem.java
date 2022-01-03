package com.company.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InventoryItem {
    public String getItemName() {
        return ItemName;
    }

    private final String ItemName;

    public String getInventoryType() {
        return InventoryType;
    }

    private final String InventoryType;

    public InventoryItem(@JsonProperty("itemname") String itemName, @JsonProperty("inventorytype") String inventoryType) {
        ItemName = itemName;
        InventoryType = inventoryType;
    }
}
