package com.company.entrypoints;

import junit.framework.TestCase;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.entrypoints.SkinbaronNewSalesCrawler.getNewestSales30Days;

public class SkinbaronNewSalesCrawlerTest extends TestCase {

    public void testGetNewestSales30Days() throws Exception {
        setUpClass();
        String ItemName = "Battle-Scarred";
        getNewestSales30Days(ItemName);
    }

}