package com.company.model;

import java.util.Date;

public class Price {
    private final Double price;

    public Price(Date timestamp, Double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    private final Date timestamp;

    public Double getPrice() {
        return price;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
