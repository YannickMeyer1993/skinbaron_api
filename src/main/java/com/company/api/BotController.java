package com.company.api;

import com.company.service.MaintainItemsService;

public class BotController {
    private final MaintainItemsService maintainItemsService;

    public BotController(MaintainItemsService maintainItemsService) {
        this.maintainItemsService = maintainItemsService;
    }

    public void addItems() {
        maintainItemsService.init();

    }
}
