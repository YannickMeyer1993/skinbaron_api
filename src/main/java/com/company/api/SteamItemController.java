package com.company.api;

import com.company.model.InventoryItem;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.company.service.InsertItemsService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SteamItemController {
    private final InsertItemsService insertItemsService;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SteamItemController.class);

    @Autowired
    public SteamItemController(InsertItemsService insertItemsService) {
        this.insertItemsService = insertItemsService;
    }

    @PostMapping
    public void addNewSkinbaronItem(@RequestBody SkinbaronItem item) throws Exception {
        insertItemsService.addNewSkinbaronItem(item);
    }


    @RequestMapping("api/v1/AddSteamPrice")
    @PostMapping
    public void addNewSteamPrice(@RequestBody SteamPrice price) throws Exception {
        insertItemsService.addNewSteamPrice(price);
    }

    @RequestMapping("api/v1/AddInventoryItem")
    @PostMapping
    public void addInventoryItem(@RequestBody InventoryItem item) throws Exception {
        insertItemsService.addInventoryItem(item);
    }

    @RequestMapping("api/v1/DeleteInventoryItems")
    @PostMapping
    public void DeleteInventoryItems() throws Exception {
        insertItemsService.deleteInventoryItems();
    }
}
