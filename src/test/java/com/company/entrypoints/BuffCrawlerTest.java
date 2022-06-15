package com.company.entrypoints;

import junit.framework.TestCase;
import org.dom4j.DocumentException;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.company.common.LoggingHelper.setUpClass;
import static com.company.entrypoints.BuffCrawler.*;

public class BuffCrawlerTest extends TestCase {

    public void testGetBuffItemNoExterior() throws DocumentException, IOException, InterruptedException {
        setUpClass();
        double val = getBuffItemNoExterior(40398);
        assertTrue(val > 0);
        //Sticker | Keyd Stars (Foil) | Katowice 2015
    }

    public void testGetBuffItemWithExterior() throws Exception {
        setUpClass();
        double val = getBuffItemWithExterior(38198);
        assertTrue(val > 0);
        //StatTrak™ AK-47 | Jaguar (Well-Worn)
    }

    public void testGetNewBuffIds() throws Exception {
        //getNewBuffIds();
    }
}