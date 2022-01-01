package com.company.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


public class SteamItemControllerTest extends TestCase {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JSONObject personJsonObject;

    @BeforeClass
    public static void runBeforeAllTestMethods() {
        String url = "http://localhost:8082/api/v1/AddSteamPrice";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject personJsonObject = new JSONObject();
        personJsonObject.put("id", 1);
        personJsonObject.put("name", "John");
    }


    public void testAddNewSkinbaronItem() {
    }

    public void testAddNewSteamPrice() {
        HttpEntity<String> request =
                new HttpEntity<String>(personJsonObject.toString(), headers);

        String personResultAsJsonStr =
                restTemplate.postForObject(createPersonUrl, request, String.class);
        JsonNode root = objectMapper.readTree(personResultAsJsonStr);

        assertNotNull(personResultAsJsonStr);
        assertNotNull(root);
        assertNotNull(root.path("name").asText());
    }
}