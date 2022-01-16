package com.company.api;

import com.company.service.CleanUpService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CleanerController {

    private final CleanUpService cleanUpService;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    public CleanerController(CleanUpService cleanUpService) {
        this.cleanUpService = cleanUpService;
    }

    @RequestMapping("api/v1/cleanup")
    @PostMapping
    public void cleanUp() throws Exception {
        cleanUpService.cleanUp();
    }
}
