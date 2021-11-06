package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static com.company.BuffCrawler.getBuffItem;
import static com.company.BuffCrawler.getBuffItemNoExterior;
import static com.company.SkinbaronAPI.getBalance;
import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

public class BuffBot {

    private static Double max_price;

    public static void main(String[] args) throws Exception {

        String query = "select  buff_preis_ok,steam_preis_ok,buff_preis,steam_preis, name,id from steam_item_sale.auf_buff_kaufbare_skins order by rati desc";

        System.out.println("BuffBot is started...");

        try (Connection conn = getConnection();Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                if (!rs.getBoolean("steam_preis_ok")) {
                    getSteamPriceForGivenName(rs.getString("name"), conn);
                }

                if (!rs.getBoolean("buff_preis_ok")) {
                    try {
                        getBuffItem(conn, rs.getInt("id"));
                    } catch (IndexOutOfBoundsException e) {
                        getBuffItemNoExterior(conn, rs.getInt("id"));
                    }
                }
            }
        }
    }
}
