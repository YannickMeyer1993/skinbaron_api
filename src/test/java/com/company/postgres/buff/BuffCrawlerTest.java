package com.company.postgres.buff;

import junit.framework.TestCase;

import java.sql.Connection;

import static com.company.old.helper.getConnection;

public class BuffCrawlerTest extends TestCase {

    public void testgetBuffItemWithPageCount() throws Exception {
        Connection conn = getConnection();
        BuffCrawler.getBuffItem(conn,871156);
    }

}