package com.company.model;

public class SkinbaronItem {
    private final String id;
    private Price price;
    private final String name;
    private final String Stickers;
    private final double wear;

    public String getId() {
        return id;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public SkinbaronItem(String id, Price price, String name, String stickers, double wear) {
        this.id = id;
        this.price = price;
        this.name = name;
        Stickers = stickers;
        this.wear = wear;
    }

    public String getName() {
        return name;
    }

    public String getStickers() {
        return Stickers;
    }

    public double getWear() {
        return wear;
    }
}
