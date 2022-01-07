package com.company.api;

import com.company.model.InventoryItem;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.company.service.InsertItemsService;
import org.eclipse.jetty.http.MetaData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ItemController {
    private final InsertItemsService insertItemsService;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ItemController.class);

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

    @RequestMapping("AddInventoryItem")
    @PostMapping
    public void addInventoryItem(@RequestBody InventoryItem item) throws Exception {
        insertItemsService.addInventoryItem(item);
    }

    @RequestMapping("DeleteNonExistingSkinbaronItems")
    @PostMapping
    public void deleteNonExistingSkinbaronItems(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {

        String ItemName = payload.get("ItemName").textValue();
        double price = Double.parseDouble(payload.get("price").textValue());

        insertItemsService.deleteNonExistingSkinbaronItems(ItemName,price);
    }

    @RequestMapping("InsertSoldSkinbaronItem")
    @PostMapping
    public void insertSoldSkinbaronItem(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {

        double price = Double.parseDouble(payload.get("price").asText());
        String itemId = payload.get("itemId").textValue();
        String name = payload.get("name").textValue();
        String classid = payload.get("classid").textValue();
        String last_updated = payload.get("last_updated").textValue();
        String instanceid = payload.get("instanceid").textValue();
        String list_time = payload.get("list_time").textValue();
        String assetid = payload.get("assetid").textValue();
        String txid = payload.get("txid").textValue();
        double commission = Double.parseDouble(payload.get("commission").asText());

        insertItemsService.insertSoldSkinbaronItem(itemId,name, price, classid, last_updated, instanceid, list_time, assetid, txid, commission);
    }

    @RequestMapping("DeleteInventoryItems")
    @PostMapping
    public void deleteInventoryItems() throws Exception {
        insertItemsService.deleteInventoryItems();
    }

    @GetMapping(value="lastSkinbaronId", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLastSkinbaronId() throws Exception {
        return insertItemsService.getLastSkinbaronId();
    }
}
