package de.yannickm.steambot.crawler.steamcrawler;


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

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;

public class steamCrawler {
    private static final Logger logger = LoggerFactory.getLogger(steamCrawler.class);
    private static LocalFileSystem fileSystem;

    public static void main(String[] args) throws Exception {
        setUpClass(); //disable Logging
        Boolean repeat = true;
        int start = 5000;

        fileSystem = new LocalFileSystem();

        while (repeat) {
            try {
                repeat = requestSearch(start);
                Thread.sleep(3000);
                start+=100;
            } catch (Exception e) {
                logger.error(e.getMessage());
                logger.error("Retry for Index: "+start);
                Thread.sleep(7000);
            }
        }
    }

    /**
     * @param start start index in search
     * @return true, if end is not reached yet. Else false
     * @throws Exception
     */
    static Boolean requestSearch(int start) throws Exception {
        HttpPost httpPost = new HttpPost("https://steamcommunity.com/market/search/render/?search_descriptions=0&sort_column=name&sort_dir=desc&appid=730&norender=1&count=500&currency=3&start="+start);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

        if (!resultJson.has("success") || resultJson.getInt("total_count")==0) {
            throw new Exception(resultJson.toString());
        }

        if (start > resultJson.getInt("total_count")) {
            logger.info("End reached.");
            return false;
        }

        logger.info("Success: "+resultJson.getBoolean("success"));
        logger.info("Start: "+resultJson.getInt("start"));
        logger.info("Page Size: "+resultJson.getInt("pagesize"));
        logger.info("Total Count: "+resultJson.getInt("total_count"));
        logger.info("Count within JSON: "+((JSONArray)resultJson.get("results")).length());

        JSONArray insertArray = new JSONArray();

        int add_to_start = 0;
        for (Object o: ((JSONArray)resultJson.get("results"))) {
            if (o instanceof JSONObject) {
                String name = ((JSONObject) o).getString("hash_name");
                int quantity = ((JSONObject) o).getInt("sell_listings");
                Double sell_price = Double.valueOf(((JSONObject) o).getString("sell_price_text").substring(1).replace(",",""));
                Double price = Double.valueOf(((JSONObject) o).getString("sale_price_text")
                        .substring(1) //currency symbol
                        .replace(",","") //thousand
                );

                JSONObject o1 = new JSONObject();
                o1.put("itemname",name);
                o1.put("quantity",quantity);
                o1.put("sell_price",sell_price);
                o1.put("price",price);

                int steamStartIndex = start+add_to_start;
                o1.put("start_parameter",steamStartIndex);

                insertArray.put(o1);

            }
            add_to_start++;
        }

        fileSystem.saveFile(steamCrawler.class.getSimpleName()+"/"+ System.currentTimeMillis()+"_index"+start+".json",insertArray.toString());
        return true;
    }
}
