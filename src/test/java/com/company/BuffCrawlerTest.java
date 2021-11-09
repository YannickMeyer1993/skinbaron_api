package com.company;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

import static com.company.BuffCrawler.getBuffItem;
import static com.company.SteamCrawler.*;
import static com.company.common.getConnection;

public class BuffCrawlerTest extends TestCase {

    public void testgetBuffItemWithPageCount() throws Exception {
        Connection conn = getConnection();
        getBuffItem(conn,871156);
    }

}