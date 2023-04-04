package de.yannickm.steambot.metrics;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Metric {
    private String key;
    private String value;
    private Timestamp ts;

    public Metric(String key,String value,Timestamp ts) {
        this.setKey(key);
        this.setValue(value);
        this.setTs(ts);
    }
}
