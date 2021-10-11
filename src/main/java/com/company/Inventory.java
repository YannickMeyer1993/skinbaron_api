package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.company.SkinbaronAPI.getSales;
import static com.company.SkinbaronAPI.getSkinbaronInventory;
import static com.company.SteamCrawler.getItemsfromInventory;
import static com.company.SteamCrawler.getStorageItems;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

public class Inventory {

    public static void main(String[] args) throws Exception {

        Connection conn = getConnection();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        getSkinbaronInventory(secret,conn);
        getItemsfromInventory(conn,"https://steamcommunity.com/inventory/76561198286004569/730/2?count=2000");
        getItemsfromInventory(conn,"https://steamcommunity.com/inventory/76561198331678576/730/2?count=2000");
        getSales(secret,conn);
        getStorageItems(conn);
        conn.close();
    }
}
