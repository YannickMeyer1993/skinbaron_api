package com.company.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;
import static junit.framework.TestCase.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @MockBean
    ItemController steamItemController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAddNewSteamPrice() throws Exception {
        String url = "http://localhost:8080/api/v1/AddSteamPrice";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();
        UUID uuid = UUID.randomUUID();

        JsonObject.put("itemname", uuid.toString());
        JsonObject.put("price", 3d);
        JsonObject.put("quantity",2);
        
        HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);

        assertFalse(checkIfResultsetIsEmpty("Select * from steam.steam_prices where name='"+uuid+"'"));
        executeDDL("DELETE FROM steam.steam_prices where name='"+uuid+"'");

    }

    @Test
    public void testAddNewSkinbaronItem() throws Exception {
        String url = "http://localhost:8080/api/v1/AddSkinbaronItem";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject();
        UUID uuid = UUID.randomUUID();

        JsonObject.put("id",uuid);
        JsonObject.put("price",3d);
        JsonObject.put("name","Drachenlore");
        JsonObject.put("sticker","Keine");
        JsonObject.put("wear",0.1221133d);

        HttpEntity<String> request = new HttpEntity<>(JsonObject.toString(), headers);

        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(url, request, String.class);

        System.out.println("TEST"+responseEntityStr.getBody());

        assertFalse(checkIfResultsetIsEmpty("Select * from steam.skinbaron_items where id='"+uuid+"'"));
        executeDDL("DELETE FROM steam.skinbaron_items where id='"+uuid+"'");

    }
}