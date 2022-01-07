package com.company.service;

import com.company.dataaccessobject.ItemDAO;
import com.company.model.InventoryItem;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    public String getLastSkinbaronId() throws Exception {
        return itemdao.getLastSkinbaronId();
    }

    public void deleteNonExistingSkinbaronItems(String ItemName, double price) throws Exception {
        itemdao.deleteNonExistingSkinbaronItems(ItemName,price);
    }

    public void insertSoldSkinbaronItem(String itemId, String name, double price, String classid, String last_updated, String instanceid, String list_time, String assetid, String txid, double commission) throws Exception {
        itemdao.insertSoldSkinbaronItem(itemId,name, price, classid, last_updated, instanceid, list_time, assetid, txid, commission);
    }
}
