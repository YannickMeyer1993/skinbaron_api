package com.company.entrypoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

import static com.company.SkinbaronAPI.*;
import static com.company.old.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.old.helper.getConnection;
import static com.company.old.helper.readPasswordFromFile;
import static java.lang.Math.min;


//TODO
public class Bot {

    private static Double max_price;
    private static Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);    //System.in is a standard input stream
        logger.info("Buy items (true/false)?");
        boolean buy_item =sc.nextBoolean();

        double balance;
        String secret;

        try (Connection conn = getConnection()) {
            secret = readPasswordFromFile("C:/passwords/api_secret.txt");
            balance = getBalance(secret, false, conn);
        }

        logger.info("Enter max price: ");
        max_price = min(sc.nextDouble(), balance);

        String query = "select steam_preis_aktuell,skinbaron_preis,steam_preis, name from steam_item_sale.auf_skinbaron_kaufbare_skins where skinbaron_preis<=" + max_price + " order by rati desc";
        String query2 = "select s.name,s.id, s.price from steam_item_sale.skinbaron_market_search_results s where s.name = ? and  s.price <= ?";

        while (true) {
            logger.info("Bot is started...");
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    if (!rs.getBoolean("steam_preis_aktuell")) {
                        double recent_price = getSteamPriceForGivenName(rs.getString("name"), conn);
                        if (recent_price < rs.getDouble("steam_preis")) {
                            logger.info("Steam Preis nicht mehr aktuell für Item " + rs.getString("name") + ".");
                            continue;
                        }
                    }

                    try (PreparedStatement pstmt = conn.prepareStatement(query2, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, rs.getString("name"));
                        pstmt.setDouble(2, rs.getDouble("skinbaron_preis"));

                        try (ResultSet rs2 = pstmt.executeQuery()) {
                            while (rs2.next()) {
                                logger.info(rs2.getString("name") + " " + rs2.getString("id") + " " + rs2.getDouble("price"));
                                try {
                                    if (buy_item) {
                                        buyItem(conn, secret, rs2.getString("id"), rs2.getDouble("price"));
                                    } else {
                                        checkIfExists(conn, secret, rs2.getString("name"), rs2.getDouble("price"));
                                    }
                                } catch (Exception e) {
                                    logger.info("Item isn't there anymore.");
                                }
                            }
                        }
                    }
                }

                logger.info("No Items found!.");
            }
        }
    }


}
