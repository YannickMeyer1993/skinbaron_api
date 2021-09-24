package com.company;

import junit.framework.TestCase;

import java.io.FileNotFoundException;

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
}