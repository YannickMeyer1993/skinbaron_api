package com.company.model;

import java.time.LocalDate;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains Steam information about the name, price, quantity and a timestamp for given Item
 */
public class SteamPrice extends Price{

    private Integer quantity;
    private Integer startIndex;

    public SteamPrice(@JsonProperty("itemname") String ItemName,
                      Date timestamp,
                      @JsonProperty("price") Double price,
                      @JsonProperty("quantity") Integer quantity,
                      @JsonProperty("steamstartindex") Integer startIndex) {
        super((timestamp!=null?timestamp: java.sql.Date.valueOf(LocalDate.now())), price, ItemName);
        this.quantity = quantity;
        this.startIndex=startIndex;
    }

    /**
     * Getter for Quantity
     * @return quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Setter for Quantity
     * @param quantity set this as class variable quantity
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getStartIndex() {
        return this.startIndex;
    }

}
