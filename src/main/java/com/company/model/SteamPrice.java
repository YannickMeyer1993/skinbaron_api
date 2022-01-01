package com.company.model;

import java.time.LocalDate;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SteamPrice extends Price{

    private Integer quantity;

    public SteamPrice(@JsonProperty("itemname") String ItemName,
                      Date timestamp,
                      @JsonProperty("price")Double price,
                      @JsonProperty("quantity") Integer quantity) {
        super((timestamp!=null?timestamp: java.sql.Date.valueOf(LocalDate.now())),
                price,
                ItemName);
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
