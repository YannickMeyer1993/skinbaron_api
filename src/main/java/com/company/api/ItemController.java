package com.company.api;

import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.company.service.InsertItemsService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/v1")
public class ItemController {
    private final InsertItemsService insertItemsService;

    @Autowired
    public ItemController(InsertItemsService insertItemsService) {
        this.insertItemsService = insertItemsService;
    }

    /**
     * @param item Skinbaron Item to insert
     * @return the id if it's not known yet else null
     * @throws Exception don't care
     */
    @ResponseBody
    @PostMapping("AddSkinbaronItem")
    public String addNewSkinbaronItem(@RequestBody SkinbaronItem item) throws Exception {
        return insertItemsService.addNewSkinbaronItem(item);
    }

    @RequestMapping("AddSteamPrice")
    @PostMapping
    public void addNewSteamPrice(@RequestBody SteamPrice price) throws Exception {
        insertItemsService.addNewSteamPrice(price);
    }

    @RequestMapping("AddInventoryItems")
    @PostMapping
    public void addInventoryItems(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {
        insertItemsService.addInventoryItems(payload);
    }

    @RequestMapping("AddSkinbaronInventoryItems")
    @PostMapping
    public void addSkinbaronInventoryItems(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws SQLException {

        insertItemsService.addSkinbaronInventoryItems(payload);
    }

    @RequestMapping("DeleteNonExistingSkinbaronItems")
    @PostMapping
    public void deleteNonExistingSkinbaronItems(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {

        String ItemName = payload.get("ItemName").textValue();
        double price = Double.parseDouble(payload.get("price").textValue());

        insertItemsService.deleteNonExistingSkinbaronItems(ItemName,price);
    }

    //TODO batch Inserts
    @RequestMapping("InsertSoldSkinbaronItem")
    @PostMapping
    public void insertSoldSkinbaronItem(@RequestBody JsonNode payload) throws Exception {
        insertItemsService.insertSoldSkinbaronItem(payload);
    }

    @RequestMapping("DeleteSkinbaronSales")
    @PostMapping
    public void deleteSkinbaronSales() throws Exception {
        insertItemsService.deleteSkinbaronSales();
    }

    @GetMapping(value="lastSkinbaronId", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLastSkinbaronId() throws Exception {
        return insertItemsService.getLastSkinbaronId();
    }

    @GetMapping(value="GetItemsToBuy", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getItemsToBuy() throws Exception {
        return insertItemsService.getItemsToBuy().toString();
    }

    @GetMapping(value="lastSoldSkinbaronId", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLastSoldSkinbaronId() throws Exception {
        return insertItemsService.getLastSoldSkinbaronId();
    }

    @RequestMapping("InsertOverviewRow")
    @PostMapping
    public void insertOverviewRow(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {

        double price = Double.parseDouble(payload.get("price").asText());

        double steam_balance = Double.parseDouble(payload.get("steam_balance").asText());
        double steam_sales_value = Double.parseDouble(payload.get("steam_sales_value").asText());
        double skinbaron_balance = Double.parseDouble(payload.get("price").asText());

        insertItemsService.insertOverviewRow(steam_balance,steam_sales_value,skinbaron_balance);
    }

    @RequestMapping("InsertNewestSales")
    @PostMapping
    public void insertNewestSales(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {
        insertItemsService.insertNewestSales(payload.toString());
    }

    @RequestMapping("DeleteSkinbaronId")
    @PostMapping
    public void deleteSkinbaronId(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {

        String id = payload.get("id").asText();

        insertItemsService.deleteSkinbaronId(id);
    }

    //TODO batch Inserts
    //TODO alles nach hinten schieben
    @RequestMapping("InsertSkinbaronSales")
    @PostMapping
    public void insertSkinbaronSales(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {

        String id = payload.get("id").asText();
        String classid = payload.get("classid").asText();
        String last_updated = payload.get("last_updated").asText();
        String list_time = payload.get("list_time").asText();
        double price = Double.parseDouble(payload.get("price").asText());
        String assetid = payload.get("assetid").asText();
        String name = payload.get("name").asText();
        String contextid = null;
        if (payload.has("contextid")) {
            contextid = payload.get("contextid").asText();
        }

        insertItemsService.insertSkinbaronSales(id,classid,last_updated,list_time,price,assetid,name,contextid);
    }
}
