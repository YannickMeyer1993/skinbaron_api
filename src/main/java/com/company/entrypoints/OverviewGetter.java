package com.company.entrypoints;

import java.util.Scanner;

import static com.company.entrypoints.SkinbaronCrawler.getBalance;
import static com.company.entrypoints.SteamCrawler.getItemPricesInventory;

//TODO Most happens in dao
public class OverviewGetter {

    private static double steam_balance;
    private static double steam_sales_value;

    public OverviewGetter() throws Exception {

        System.out.println("Decimal separator is comma!");
        Scanner sc= new Scanner(System.in);
        System.out.println("Enter current steam balance: ");
        steam_balance = sc.nextDouble();
        System.out.println("Enter current steam sales value: ");
        steam_sales_value = sc.nextDouble();

        getItemPricesInventory();

        double skinbaron_balance = getBalance();

        insertOverviewRow(steam_balance, steam_sales_value, skinbaron_balance );

    }

    public void insertOverviewRow(double steam_balance,double steam_sales_value, double skinbaron_balance) throws Exception {
        //TODO request + test
    }
}

