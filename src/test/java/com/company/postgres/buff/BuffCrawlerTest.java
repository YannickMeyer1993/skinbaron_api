package com.company.postgres.buff;

import junit.framework.TestCase;


import static com.company.common.LoggingHelper.setUpClass;
import static com.company.entrypoints.BuffCrawler.getBuffItemWithExterior;


public class BuffCrawlerTest extends TestCase {

    public void testgetBuffItemWithPageCount() throws Exception {
        setUpClass();
        getBuffItemWithExterior(871156);
    }

}