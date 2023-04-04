package de.yannickm.steambot.model;

import java.util.Date;

/**
 * low level class price. Consists of price and timestamp
 */
public class Price {
    private Double price=0.0;
    private final Date timestamp;
    private String ItemName;


    public Price(Date timestamp, Double price, String itemName) {
        this.timestamp = timestamp;
        this.price = price;
        ItemName = itemName;
    }

    public Price(Date timestamp, Double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public Double getValue() {
        return price!=null?price:0;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getItemName() {
        return ItemName;
    }
}
