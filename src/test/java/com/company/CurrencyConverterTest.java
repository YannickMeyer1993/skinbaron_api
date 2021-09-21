package com.company;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CurrencyConverterTest {

    @Test
    public void getUSDinEURtest() throws Exception {
        Double usd = CurrencyConverter.getUSDinEURO();
        System.out.println(usd);
        assertTrue(null != usd);
    }

    @Test
    public void testGet_RMB_in_EURO() {
        Double rmb = CurrencyConverter.getRMBinEURO();
        System.out.println(rmb);
        assertTrue(null != rmb);
    }

}