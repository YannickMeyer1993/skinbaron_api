package com.company.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class SkinbaronItem {
    private final String id;
    private Price price;
    private final String name;
    private final String Stickers;
    private final double wear;
    private final String inspect;
    private final String sbinspect;

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
                         @JsonProperty("market_name") String name,
                         @JsonProperty("stickers") String stickers,
                         @JsonProperty("wear") double wear,
                         @JsonProperty("inspect") String inspect,
                         @JsonProperty("sbinspect") String sbinspect) {
        this.id = id;
        this.inspect = inspect;
        this.sbinspect = sbinspect;
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

    public String getInspect() {
        return inspect;
    }

    public String getSbinspect() {
        return sbinspect;
    }

}
