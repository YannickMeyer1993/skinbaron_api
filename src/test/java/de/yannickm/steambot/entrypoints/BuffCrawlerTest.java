package de.yannickm.steambot.entrypoints;

import junit.framework.TestCase;

import static de.yannickm.steambot.common.LoggingHelper.setUpClass;
import static de.yannickm.steambot.entrypoints.BuffCrawler.*;

public class BuffCrawlerTest extends TestCase {

    public void testGetBuffItemNoExterior() throws Exception {
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
        getNewBuffIds();
    }

    public void testTestGetBuffItemNoExteriorNegative() throws Exception {
        boolean failed = false;

        try {
            getBuffItemNoExterior(882474);
        } catch (Exception e) {
            failed = true;
        }

        if (!failed) {
            throw new Exception("Function must fail!");
        }
    }
}