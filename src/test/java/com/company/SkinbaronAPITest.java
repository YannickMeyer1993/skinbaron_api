package com.company;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.company.common.readPasswordFromFile;
import static com.company.SkinbaronAPI.*;

public class SkinbaronAPITest extends TestCase {

    public void testResendTradeOffersPositive() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        assertEquals(200,resendTradeOffers(secret));
    }

    @SuppressWarnings("CatchMayIgnoreException")
    public void testResendTradeOffersNegative() {
        try {
            resendTradeOffers("bad secret");
        }
        catch (Exception e)
        {
            assertEquals(e.getMessage(),"wrong or unauthenticated request");
        }
    }

    public void testwriteSoldItems() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        assertEquals(200,writeSoldItems(secret));
    }

    public void testgetBalance() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        assertTrue(getBalance(secret,true)>0);
    }

    public void testSearch1() throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        Search(secret,conn,"5f1eb3cb-a81d-4746-a528-211e5c424207");
    }
}