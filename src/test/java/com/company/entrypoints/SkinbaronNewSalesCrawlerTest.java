package com.company.entrypoints;

import junit.framework.TestCase;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.entrypoints.SkinbaronNewSalesCrawler.getNewestSales30Days;

public class SkinbaronNewSalesCrawlerTest extends TestCase {

    public void testGetNewestSales30Days() throws Exception {
        setUpClass();
        String ItemName = "Sticker Capsule";
        getNewestSales30Days(ItemName);
    }

    public void testGetNewestSales30DaysStatTrak() throws Exception {
        setUpClass();
        String ItemName = "StatTrak™ Desert Eagle";
        getNewestSales30Days(ItemName);
    }

    public void testGetNewestSales30DaysSouvenir() throws Exception {
        setUpClass();
        String ItemName = "Souvenir Desert Eagle";
        getNewestSales30Days(ItemName);
    }
}