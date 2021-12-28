package com.company.postgres.skinbaron;

import junit.framework.TestCase;

public class SoldItemsTest extends TestCase {
    public void testGetSoldItems() throws Exception {
        assertEquals(SoldItems.get(),200);
    }

}