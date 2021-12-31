package com.company.model;

import java.util.Date;

public class Price {
    private final Double price;
    private final Date timestamp;
        private final String ItemName;


    public Price(Date timestamp, Double price, String itemName) {
        this.timestamp = timestamp;
        this.price = price;
        ItemName = itemName;
    }

    public Double getValue() {
        return price;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getItemName() {
        return ItemName;
    }
}
