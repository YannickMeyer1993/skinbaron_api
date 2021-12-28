package com.company.model;

public class SkinbaronItem {
    private final String id;
    private Double price;

    public String getId() {
        return id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public SkinbaronItem(String id, Double price) {
        this.id = id;
        this.price = price;
    }
}
