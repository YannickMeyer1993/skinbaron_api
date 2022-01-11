package com.company.entrypoints;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.company.common.PasswordHelper.readPasswordFromFile;

public class SkinbaronNewSalesCrawler {

    private final static Logger logger = LoggerFactory.getLogger(SkinbaronNewSalesCrawler.class);

    public static void main(String[] args) throws Exception {

        for (String name: getListforNewestSales()) {
            getNewestSales30Days(name);
        }

    }

    private static List<String> getListforNewestSales() {
        //TODO
        return new ArrayList<>();
    }

    /**
     * Shows the last 10 sold items from the last 30 days
     * @param name Market Hash Name in steam
     */
    public static void getNewestSales30Days(String name) throws Exception {

        logger.info("Skinbaron API GetNewestSales30Days has been called.");

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String url = "https://api.skinbaron.de/GetNewestSales30Days";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-requested-with", "XMLHttpRequest");
        headers.set("Accept", "application/json");

        boolean statTrak = name.contains("StatTrak");
        boolean souvenir = name.contains("Souvenir");

        JSONObject JsonObject = new JSONObject();
        JsonObject.put("apikey",secret);
        JsonObject.put("itemName",name);
        JsonObject.put("statTrak",statTrak);
        JsonObject.put("souvenir",souvenir);
        //JsonObject.put("dopplerPhase",false); //We don't get info about the phase within the search function

        System.out.println(JsonObject);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        String result = restTemplate.postForObject(url, request, String.class);

        System.out.println(result);
        requestInsertNewSales(result);

    }

    private static void requestInsertNewSales(String result) {
        String url = "http://localhost:8080/api/v1/InsertNewestSales";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject JsonObject = new JSONObject(result);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(JsonObject.toString(), headers);

        restTemplate.postForObject(url, request, String.class);
    }

}
