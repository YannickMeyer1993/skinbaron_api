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
        getBuffItemNoExterior(40398);
        //Sticker | Keyd Stars (Foil) | Katowice 2015
    }

    public void testGetBuffItemWithExterior() throws Exception {
        setUpClass();
        getBuffItemWithExterior(38198);
        //StatTrak™ AK-47 | Jaguar (Well-Worn)
    }

    public void testGetNewBuffIds() throws FileNotFoundException {
        getNewBuffIds();
    }
}