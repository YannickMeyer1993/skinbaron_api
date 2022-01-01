package com.company.api;

import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import com.company.service.InsertItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.logging.Logger;


@RestController
public class SteamItemController {
    private final InsertItemsService insertItemsService;

    private final static Logger LOGGER = Logger.getLogger(SteamItemController.class.getName());


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
}
