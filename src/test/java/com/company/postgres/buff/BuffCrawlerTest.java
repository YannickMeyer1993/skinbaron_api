package com.company.postgres.buff;

import com.company.entrypoints.BuffCrawler;
import junit.framework.TestCase;

import java.sql.Connection;

import static com.company.common.PostgresHelper.getConnection;


public class BuffCrawlerTest extends TestCase {

    //TODO
    public void testgetBuffItemWithPageCount() throws Exception {
        Connection conn = getConnection();
        BuffCrawler.getBuffItem(conn,871156);
    }

}