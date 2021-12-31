package com.company.common;

import junit.framework.TestCase;

public class CurrencyHelperTest extends TestCase {

    public void testGetUSDinEURtest() throws Exception {
        Double usd = CurrencyHelper.getConversionRateToEuro("USD");
        assertNotNull(usd);
    }

    public void testGetRMBinEUROtest() throws Exception{
        Double cny = CurrencyHelper.getConversionRateToEuro("CNY");
        assertNotNull(cny);
    }
}