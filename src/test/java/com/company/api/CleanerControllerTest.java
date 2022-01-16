package com.company.api;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;

public class CleanerControllerTest {

    @Test
    @Ignore
    public void testCleanUp() throws Exception {
        String url = "http://localhost:8080/api/v1/cleanup";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();

        HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);

        //could fail if search is running
        Assert.assertTrue(checkIfResultsetIsEmpty("Select * from steam.skinbaron_items where name like 'Sealed Graffiti%'"));
    }
}