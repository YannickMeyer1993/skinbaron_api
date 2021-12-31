package com.company;

import junit.framework.TestCase;
import org.json.JSONException;

import java.sql.*;
import static com.company.helper.getConnection;
import static com.company.helper.readPasswordFromFile;
import static com.company.SkinbaronAPI.*;

public class SkinbaronAPITest extends TestCase {

    public void testResendTradeOffersPositive() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        assertEquals(200,resendTradeOffers(secret));
    }

    public void testCheckIfExists() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        String name="Fracture Case";
        double price = 10d;
        try(Connection conn = getConnection()) {
            assertTrue(checkIfExists(conn,secret,name,price));
        }

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

    public void testgetBalance() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        try(Connection conn = getConnection()) {
            assertTrue(getBalance(secret, true, conn) > 0);
        }
    }

    public void testSearch1() throws Exception {
        Connection conn = getConnection();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        Search(secret,conn,"5f1eb3cb-a81d-4746-a528-211e5c424207");
    }

    public void testBuyItemNegative() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        try(Connection conn = getConnection()) {
            buyItem(conn, secret, "6944b89b-dd36-49f5-b9ae-7dea17f5b0a4", 0.02);
        } catch (JSONException e) {
            assertEquals("[\"some offer(s) are already sold\"]", e.getMessage());
        }
    }

}