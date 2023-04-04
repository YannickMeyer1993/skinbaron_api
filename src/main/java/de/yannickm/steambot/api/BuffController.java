package de.yannickm.steambot.api;

import de.yannickm.steambot.service.BuffService;
import org.json.JSONArray;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class BuffController {

    private final BuffService buffService;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BuffController.class);

    @Autowired
    public BuffController(BuffService buffService) {
        this.buffService = buffService;
    }

    @RequestMapping("api/v1/InsertBuffPrices")
    @PostMapping
    public void insertBuffPrices(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) throws Exception {
        buffService.insertBuffPrices(new JSONArray(payload.toString()));
    }

    @GetMapping(value="api/v1/GetBuffIds",produces = MediaType.TEXT_PLAIN_VALUE)
    public String GetBuffIds() throws Exception {
        return buffService.getBuffIds();
    }
}
