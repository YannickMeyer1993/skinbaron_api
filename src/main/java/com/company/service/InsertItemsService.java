package com.company.service;

import com.company.dataaccessobject.ItemDAO;
import com.company.model.Item;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;

@Service
public class InsertItemsService {

    //TODO update Item Object

    private final ItemDAO itemdao;
    //Map with all items
    private final HashMap<String, Item> capitalCities = new HashMap<String, Item>();

    @Autowired
    public InsertItemsService(@Qualifier("postgres") ItemDAO itemdao) {
        this.itemdao = itemdao;
    }

    public void addNewSteamPrice(@RequestBody SteamPrice price) throws Exception {
        itemdao.addSteamPrice(price);
    }

    public void addNewSkinbaronItem(SkinbaronItem skinbaronitem) throws Exception {
        itemdao.addSkinbaronItem(skinbaronitem);
    }

}
