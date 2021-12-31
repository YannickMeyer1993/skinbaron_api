package com.company;

import java.sql.*;
import java.util.Scanner;

import static com.company.SkinbaronAPI.*;
import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.helper.getConnection;
import static com.company.helper.readPasswordFromFile;
import static java.lang.Math.min;

public class Bot {

    private static Double max_price;

    public static void main(String[] args) throws Exception {

        Scanner sc= new Scanner(System.in);    //System.in is a standard input stream
        System.out.println("Buy items (true/false)?");
        Boolean buy_item =sc.nextBoolean();

        Connection conn = getConnection();

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        Double balance = getBalance(secret,false,conn);

        System.out.println("Enter max price: ");
        max_price = min(sc.nextDouble(),balance);

        while(true) {
            System.out.println("Bot is started...");
            try (Statement stmt = conn.createStatement()) {
                String query = "select steam_preis_aktuell,skinbaron_preis,steam_preis, name from steam_item_sale.auf_skinbaron_kaufbare_skins where skinbaron_preis<=" + max_price + " order by rati desc";
                try(ResultSet rs = stmt.executeQuery(query)) {

                    while (rs.next()) {
                        if (!rs.getBoolean("steam_preis_aktuell")) {
                            Double recent_price = getSteamPriceForGivenName(rs.getString("name"), conn);
                            if (recent_price < rs.getDouble("steam_preis")) {
                                System.out.println("Steam Preis nicht mehr aktuell für Item " + rs.getString("name") + ".");
                                continue;
                            }
                        }

                        String query2 = "select s.name,s.id, s.price from steam_item_sale.skinbaron_market_search_results s where s.name = ? and  s.price <= ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(query2, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, rs.getString("name"));
                            pstmt.setDouble(2, rs.getDouble("skinbaron_preis"));

                            try (ResultSet rs2 = pstmt.executeQuery()) {
                                while (rs2.next()) {
                                    System.out.println(rs2.getString("name") + " " + rs2.getString("id") + " " + rs2.getDouble("price"));
                                    try {
                                        if (buy_item) {
                                            buyItem(conn, secret, rs2.getString("id"), rs2.getDouble("price"));
                                        } else
                                        {
                                            checkIfExists(conn,secret,rs2.getString("name"),rs2.getDouble("price"));
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Item isn't there anymore.");
                                    }
                                }
                            }
                        }
                    }

                    System.out.println("No Items found!.");
                }
            }
        }
    }


}
