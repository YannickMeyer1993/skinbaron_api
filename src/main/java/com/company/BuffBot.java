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
import static com.company.common.readPasswordFromFile;

public class BuffBot {

    private static Double max_price;

    public static void main(String[] args) throws Exception {

        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        Double balance = getBalance(secret, false, conn);

        System.out.println("Bot is started...");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select  buff_preis_ok,steam_preis_ok,buff_preis,steam_preis, name,id from steam_item_sale.auf_buff_kaufbare_skins order by rati desc");

        while (rs.next()) {
            if (!rs.getBoolean("steam_preis_ok")) {
                Double recent_price = getSteamPriceForGivenName(rs.getString("name"), conn);
            }

            if (!rs.getBoolean("buff_preis_ok")) {
                try {
                    getBuffItem(conn, rs.getInt("id"));
                } catch (IndexOutOfBoundsException e) {
                    getBuffItemNoExterior(conn, rs.getInt("id"));
                }
            }

        }

        rs.close();
        conn.close();
    }
}
