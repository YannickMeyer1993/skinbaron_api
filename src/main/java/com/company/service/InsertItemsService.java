package com.company.service;

import com.company.dataaccessobject.ItemDAO;
import com.company.model.InventoryItem;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class InsertItemsService {

    private final ItemDAO itemdao;

    @Autowired
    public InsertItemsService(@Qualifier("postgres") ItemDAO itemdao) {
        this.itemdao = itemdao;
    }

    public void addNewSteamPrice(@RequestBody SteamPrice price) throws Exception {
        itemdao.addSteamPrice(price);
    }

    public String addNewSkinbaronItem(@RequestBody SkinbaronItem skinbaronitem) throws Exception {
        return itemdao.addSkinbaronItem(skinbaronitem);
    }

    public void addInventoryItem(@RequestBody InventoryItem item) throws Exception {
        itemdao.addInventoryItem(item);
    }

    public void deleteInventoryItems() throws Exception {
        itemdao.deleteInventoryItems();
    }
}
