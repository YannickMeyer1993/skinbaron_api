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

    public void testGetNewestSales30DaysKarambit() throws Exception {
        setUpClass();
        String ItemName = "★ StatTrak™ Talon Knife | Stained (Field-Tested)";
        getNewestSales30Days(ItemName);
    }

    public void testGetNewestSales30DaysDoppler() throws Exception {
        setUpClass();
        String ItemName = "Doppler";
        getNewestSales30Days(ItemName);
    }
}