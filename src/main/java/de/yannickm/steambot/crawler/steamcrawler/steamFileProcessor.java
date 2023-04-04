package de.yannickm.steambot.crawler.steamcrawler;


import de.yannickm.steambot.dao.filesystemdao.FileSystem;
import de.yannickm.steambot.dao.filesystemdao.LocalFileSystem;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;
import static de.yannickm.steambot.common.PostgresExecutor.getConnection;
import static de.yannickm.steambot.crawler.steamcrawler.steamCrawler.requestSearch;

public class steamFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(steamFileProcessor.class);
    private static final String UrlPost = "http://localhost:8080/api/v1/AddSteamPrices";
    private static FileSystem fileSystem;

    public static void main(String[] args) throws Exception {
        setUpClass(); //disable Logging
        fileSystem = new LocalFileSystem();
        String path = steamCrawler.class.getSimpleName();
        for (String fileName: fileSystem.getFileNames(path)) {
            JSONArray array = new JSONArray(fileSystem.getContent(fileName));
            insertArray(array);
        }

    }

    public static void insertArray(JSONArray array) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(array.toString(), headers);

        restTemplate.postForObject(UrlPost, request, String.class);
    }

    public static int getStartIndexForGivenName(String hash_name) throws Exception {
        try(Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select start_index from steam.steam_item_indexes s\n" +
                " where s.name = '"+hash_name+"';")) {

            rs.next();
            return rs.getInt("start_index");
        }
    }

    public static double getSteamPriceForGivenName(String hash_name) throws Exception {

        Boolean repeat = true;
        while (repeat) {
            try {
                repeat = false;
                requestSearch(getStartIndexForGivenName(hash_name));
                Thread.sleep(3000);
            } catch (Exception e) {
                repeat = true;
                logger.error("Retry for Item: "+hash_name);
                Thread.sleep(7000);
            }
        }

        try(Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select s.price_euro from steam.steam_current_prices s\n" +
                " where s.name = '"+hash_name+"';")) {

            rs.next();
            return rs.getDouble("price_euro");
        }
    }
}
