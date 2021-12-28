package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private final String name;
    private final Integer BuffId;

    private Boolean hasExterior =null;

    private Double SteamPrice = null;
    private Double SkinbaronPrice = null;
    private Double BuffPrice = null;

    private List<SkinbaronItem> SkinbaronItemList = new ArrayList();
    private List<SteamPrice> SteamPriceList = new ArrayList();
    private List<BuffPrice> BuffPriceList = new ArrayList();

    public Item(String ItemName,int id) {
        this.name = ItemName;
        this.BuffId = id;
    }

    public Item(String ItemName) {
        this.name = ItemName;
        this.BuffId = null;
    }

    public String getName() {
        return name;
    }

    public Integer getBuffId() {
        return BuffId;
    }

    public Double getSteamPrice() {
        return SteamPrice;
    }

    /*
    sets the current steam price
    triggered if there is a new steam price
     */
    public void setSteamPrice(Double steamPrice) {
        SteamPrice = steamPrice;
    }

    public Double getSkinbaronPrice() {
        return SkinbaronPrice;
    }

    /*
    sets the lowest current Skinbaron price
    triggered after change in SkinbaronItem list
     */
    public void setSkinbaronPrice(Double price) {
        if (SkinbaronPrice==null) {
            SkinbaronPrice = price;
            return;
        }

        for (SkinbaronItem item: SkinbaronItemList) {
            if (item.getPrice() < SkinbaronPrice) {
                SkinbaronPrice = item.getPrice();
            }
        }
    }

    public Double getBuffPrice() {
        return BuffPrice;
    }

    /*
    sets the current buff price
    triggered if there is a new buff price
    */
    public void setBuffPrice(Double buffPrice) {
        BuffPrice = buffPrice;
    }

    public Boolean getHasExterior() {
        return hasExterior;
    }

    public void setHasExterior(Boolean has_exterior) {
        hasExterior = has_exterior;
    }
}
