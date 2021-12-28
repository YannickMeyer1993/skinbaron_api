package com.company.model;

public class ItemCollection {

    private final String name;
    private final boolean cool;

    public ItemCollection(String name, boolean cool) {
        this.name = name;
        this.cool = cool;
    }

    public String getName() {
        return name;
    }

    public boolean is_cool() {
        return cool;
    }
}
