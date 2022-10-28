package com.company.api;

import com.company.service.SteamService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class SteamController {

    private final SteamService steamService;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SteamController.class);

    @Autowired
    public SteamController(SteamService steamService) {
        this.steamService = steamService;
    }

    @RequestMapping("api/v1/SetOverview")
    @PostMapping
    public void setOverview(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {

        double steambalance = Double.parseDouble(payload.get("steambalance").toPrettyString());
        double steamopensales = Double.parseDouble(payload.get("steamopensales").toPrettyString());
        double skinbaronbalance = Double.parseDouble(payload.get("skinbaronbalance").toPrettyString());

        steamService.setOverview(steambalance,steamopensales,skinbaronbalance);
    }

}
