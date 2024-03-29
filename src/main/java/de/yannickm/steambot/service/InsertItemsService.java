package de.yannickm.steambot.service;

import de.yannickm.steambot.dataaccessobject.ItemDAO;
import de.yannickm.steambot.model.SkinbaronItem;
import de.yannickm.steambot.model.SteamPrice;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class InsertItemsService {

    private final ItemDAO itemdao;

    @Autowired
    public InsertItemsService(@Qualifier("postgres") ItemDAO itemdao) {
        this.itemdao = itemdao;
    }

    public void addNewSteamPrices(JsonNode payload) throws Exception {
        itemdao.addSteamPrices(payload);
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

    public void insertPriceList(JsonNode payload) throws Exception {
        itemdao.insertPriceList(payload);
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
