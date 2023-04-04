package de.yannickm.steambot.entrypoints;

import junit.framework.TestCase;

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;
import static de.yannickm.steambot.entrypoints.SkinbaronNewSalesCrawler.getNewestSales30Days;

public class SkinbaronNewSalesCrawlerTest extends TestCase {

    public void testGetNewestSales30Days() throws Exception {
        setUpClass();
        String ItemName = "Battle-Scarred";
        getNewestSales30Days(ItemName);
    }

}