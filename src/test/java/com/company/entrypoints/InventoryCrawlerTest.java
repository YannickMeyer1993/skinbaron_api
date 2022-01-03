package com.company.entrypoints;

import junit.framework.TestCase;

import static com.company.common.PostgresHelper.checkIfResultsetIsEmpty;
import static com.company.common.PostgresHelper.executeDDL;

public class InventoryCrawlerTest extends TestCase {

    public void testsendRequestInsertInventoryItem() throws Exception {
        InventoryCrawler crawler = new InventoryCrawler();
        crawler.sendRequestInsertInventoryItem("AWP | Dragon Lore (Factory New)","Test Inv");
        assertFalse(checkIfResultsetIsEmpty("select * from steam.inventory where name = 'AWP | Dragon Lore (Factory New)' and inv_type='Test Inv'"));
        //executeDDL("DELETE FROM steam.inventory where name = 'AWP | Dragon Lore (Factory New)'");
    }

}