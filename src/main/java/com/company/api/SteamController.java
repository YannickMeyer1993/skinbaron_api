package com.company.api;

import com.company.service.CleanUpService;
import com.company.service.SteamService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SteamController {

    private final SteamService steamService;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    public SteamController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping(value="api/v1/GetHightestSteamIteration",produces = MediaType.TEXT_PLAIN_VALUE)
    public String getHighestSteamIteration() throws Exception {
       return ""+steamService.getHighestSteamIteration();
    }

}
