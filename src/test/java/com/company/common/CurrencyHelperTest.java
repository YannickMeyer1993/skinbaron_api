package com.company.common;

import com.company.CurrencyConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrencyHelperTest {

    @Test
    public void getUSDinEURtest() throws Exception {
        Double usd = CurrencyHelper.getConversionRateToEuro("USD");
        assertNotNull(usd);
    }

    @Test
    public void testGet_RMB_in_EURO() throws Exception{
        Double cny = CurrencyHelper.getConversionRateToEuro("CNY");
        assertNotNull(cny);
    }

}