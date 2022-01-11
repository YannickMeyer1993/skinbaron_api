package com.company.entrypoints;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import static com.company.common.PasswordHelper.readPasswordFromFile;

public class ToBeTested {

    private final static Logger logger = LoggerFactory.getLogger(ToBeTested.class);

    //TODO

    /**
     * Shows the last 10 sold items from the last 30 days
     * @param itemName Market Hash Name in steam
     * @throws Exception
     */
    public static void getNewestSales30Days(String itemName) throws Exception {

        logger.info("Skinbaron API GetNewestSales30Days has been called.");

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String url = "https://api.skinbaron.de/GetNewestSales30Days";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-requested-with", "XMLHttpRequest");
        headers.set("Accept", "application/json");

        boolean statTrak = itemName.contains("StatTrak");
        boolean souvenir = itemName.contains("Souvenir");

        JSONObject JsonObject = new JSONObject();
        JsonObject.put("apikey",secret);
        JsonObject.put("itemName",itemName);
        JsonObject.put("statTrak",statTrak);
        JsonObject.put("souvenir",souvenir);
        //JsonObject.put("dopplerPhase",false); //We don't get info about the phase within the search function

        System.out.println(JsonObject);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        String result = restTemplate.postForObject(url, request, String.class);

        System.out.println(result);

        JSONObject resultJson = new JSONObject(result);

        if (resultJson.has("message")) {
            System.out.println("Result: " + resultJson.get("message"));
            throw new Exception((String) resultJson.get("message"));
        }
    }

}
