package com.company.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

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

    public SkinbaronItem(@JsonProperty("id") String id,
                         @JsonProperty("price") double price,
                         @JsonProperty("name") String name,
                         @JsonProperty("sticker")String stickers,
                         @JsonProperty("wear") double wear) {
        this.id = id;
        this.price = new Price(java.sql.Date.valueOf(LocalDate.now()),price,name);
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
