package com.company.buff;

import com.company.buff.BuffCrawler;
import junit.framework.TestCase;

import java.sql.Connection;

import static com.company.common.getConnection;

public class BuffCrawlerTest extends TestCase {

    public void testgetBuffItemWithPageCount() throws Exception {
        Connection conn = getConnection();
        BuffCrawler.getBuffItem(conn,871156);
    }

}