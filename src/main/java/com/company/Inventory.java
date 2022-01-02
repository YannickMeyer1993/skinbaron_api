package com.company;

import java.sql.Connection;

import static com.company.SkinbaronAPI.getSales;
import static com.company.SkinbaronAPI.getSkinbaronInventory;
import static com.company.old.SteamCrawler.getItemsfromInventory;
import static com.company.old.SteamCrawler.getStorageItems;
import static com.company.old.helper.getConnection;
import static com.company.old.helper.readPasswordFromFile;

public class Inventory {

    public static void main(String[] args) throws Exception {

        try(Connection conn = getConnection()) {

            String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

            getSkinbaronInventory(secret, conn);
            getItemsfromInventory(conn, "https://steamcommunity.com/inventory/76561198286004569/730/2?count=2000", "steam");
            getItemsfromInventory(conn, "https://steamcommunity.com/inventory/76561198331678576/730/2?count=2000", "smurf");
            getSales(secret, conn);
            getStorageItems(conn);
        }
    }
}
