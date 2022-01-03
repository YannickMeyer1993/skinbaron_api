package com.company.service;

import com.company.SkinbaronAPI;
import com.company.entrypoints.SteamCrawler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static com.company.old.helper.readPasswordFromFile;

public class BuyService {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SkinbaronAPI.class);

    public static void buyItem(String name,  String itemId, Double price) throws Exception {

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String jsonInputString = "{\"apikey\": \"" + secret + "\",\"total\":" + price + ",\"saleids\":[\"" + itemId + "\"]}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/BuyItems");
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
        httpPost.setEntity(entity);
        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());

        try {
            JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

            if (resultJson.has("generalErrors")) {
                System.out.println(resultJson.get("generalErrors").toString());
                if ("[\"some offer(s) are already sold\"]".equals(resultJson.get("generalErrors").toString())
                        || "[\"count mismatch - maybe some offers have been sold or canceled or you provided wrong saleids\"]".equals(resultJson.get("generalErrors").toString())
                ) {
                    logger.info("Item is already gone.");
                }
                return;
            }

            if (!resultJson.has("items")) {
                logger.info("There was no json query 'result' found in:");
                logger.info(resultJson.toString());
                return;
            }

            //success
            logger.info("Item \"" + name + "\" was bought for " + price +".");
        } catch (Exception exp) {
            logger.info("Error while buying item "+name);
            logger.info("Received Message:");
            logger.info(result);
            throw new Exception();
        }
    }
}
