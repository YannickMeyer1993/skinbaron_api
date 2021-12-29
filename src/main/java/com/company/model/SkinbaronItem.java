package com.company.model;

public class SkinbaronItem {
    private final String id;
    private Price price;

    public String getId() {
        return id;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public SkinbaronItem(String id, Price price) {
        this.id = id;
        this.price = price;
    }
}
