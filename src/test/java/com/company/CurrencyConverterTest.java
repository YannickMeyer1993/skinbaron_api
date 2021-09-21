package com.company;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class CurrencyConverterTest {

    @Test
    public void getUSDinEURtest() throws Exception {
        Double usd = CurrencyConverter.getUSDinEURO(new Double (1));
        System.out.println(usd);
        assertTrue(null != usd);
    }

    @Test
    public void testGet_RMB_in_EURO() {
        Double rmb = CurrencyConverter.getRMBinEURO(new Double (1));
        System.out.println(rmb);
        assertTrue(null != rmb);
    }

}