package com.company.model;

import java.util.Date;

public class SteamPrice extends Price{

    private Integer quantity;

    public SteamPrice(Date timestamp, Double price,Integer quantity) {
        super(timestamp,price);
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
