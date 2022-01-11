package com.company.entrypoints;

import junit.framework.TestCase;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.entrypoints.ToBeTested.getNewestSales30Days;

public class ToBeTestedTest extends TestCase {

    public void testGetNewestSales30Days() throws Exception {
        setUpClass();
        String ItemName = "Operation Riptide Case";
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