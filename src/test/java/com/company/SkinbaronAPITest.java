package com.company;

import junit.framework.TestCase;
import org.json.JSONException;
import org.junit.Test;

import java.sql.*;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;
import static com.company.SkinbaronAPI.*;

public class SkinbaronAPITest extends TestCase {

    public void testResendTradeOffersPositive() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        assertEquals(200,resendTradeOffers(secret));
    }

    @Test
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

    public void testwriteSoldItems() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        try(Connection conn = getConnection()) {
            assertEquals(200, writeSoldItems(conn,secret));
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

    public void testBuyFromSelect() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        try(Connection conn = getConnection()) {
            buyFromSelect(secret, conn);
        }
    }

    public void testGetExtendedPricelist() throws Exception {
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        String query ="select pl.markethashname, count(*) from steam_item_sale.skinbaron_pricelist pl group by pl.markethashname having count(*) > 1";

        try(Connection conn = getConnection();Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(query)) {
            getExtendedPriceList(secret, conn);
            if (rs.next()) {
                throw new Exception("Primary Key Constraint not met!");
            }
        }
    }
}