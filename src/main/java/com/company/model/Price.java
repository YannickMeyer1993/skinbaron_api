package com.company.model;

import java.util.Date;

public class Price {
    private final Double price;
    private final Date timestamp;


    public Price(Date timestamp, Double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public Double getValue() {
        return price;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
