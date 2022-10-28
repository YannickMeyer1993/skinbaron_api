package com.company.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * consists of a Skinbaron Item with information about Id, price, name, stickers, wear, inspect link and steam inspect link
 */
public class SkinbaronItem {
    private final String ID;
    private Price price;
    private final String NAME;
    private final String Stickers;
    private final double wear;
    private final String inspect;
    private final String sbinspect;

    public String getId() {
        return ID;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public SkinbaronItem(@JsonProperty("id") String ID,
                         @JsonProperty("price") double price,
                         @JsonProperty("market_name") String NAME,
                         @JsonProperty("stickers") String stickers,
                         @JsonProperty("wear") double wear,
                         @JsonProperty("inspect") String inspect,
                         @JsonProperty("sbinspect") String sbinspect) {
        this.ID = ID;
        this.inspect = inspect;
        this.sbinspect = sbinspect;
        this.price = new Price(java.sql.Date.valueOf(LocalDate.now()),price,NAME);
        this.NAME = NAME;
        Stickers = stickers;
        this.wear = wear;
    }

    public String getName() {
        return NAME;
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
