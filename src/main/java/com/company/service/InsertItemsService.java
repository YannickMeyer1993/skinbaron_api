package com.company.service;

import com.company.dataaccessobject.ItemDAO;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.SQLException;

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

    public void addInventoryItems(JsonNode payload) throws Exception {
        itemdao.addInventoryItems(payload);
    }


    public String getLastSkinbaronId() throws Exception {
        return itemdao.getLastSkinbaronId();
    }

    public void deleteNonExistingSkinbaronItems(String ItemName, double price) throws Exception {
        itemdao.deleteNonExistingSkinbaronItems(ItemName,price);
    }

    public void insertSoldSkinbaronItem(JsonNode payload) throws Exception {
        itemdao.insertSoldSkinbaronItem(payload);
    }

    public String getLastSoldSkinbaronId() throws Exception {
        return itemdao.getLastSoldSkinbaronId();
    }

    public void insertOverviewRow(double steam_balance, double steam_sales_value, double skinbaron_balance) throws Exception {
        itemdao.insertOverviewRow(steam_balance,steam_sales_value,skinbaron_balance);
    }

    public void deleteSkinbaronId(String id) throws Exception {
        itemdao.deleteSkinbaronId(id);
    }

    public JSONArray getItemsToBuy() throws Exception {
        return itemdao.getItemsToBuy();
    }

    public void insertNewestSales(String payload) throws Exception {
        itemdao.insertNewestSales(payload);
    }

    public void insertSkinbaronSales(JsonNode payload) throws Exception {
        itemdao.insertSkinbaronSales(payload);
    }

    public void deleteSkinbaronSales() throws Exception {
        itemdao.deleteSkinbaronSalesTable();
    }

    public void addSkinbaronInventoryItems(JsonNode payload) throws SQLException {
        itemdao.addSkinbaronInventoryItems(payload);
    }
}
