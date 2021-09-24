package com.company;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Properties;

import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.common.readPasswordFromFile;

public class Overview {

    public static void getItemPricesInventory(Connection conn) throws Exception {

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select \n" +
                "distinct name from steam_item_sale.inventory_with_prices s\n" +
                "where (round((date_part('epoch'::text, now() - s.\"timestamp\" ::timestamp with time zone) / (60 * 60 * 24)::double precision)::numeric, 1) > 1)  order by name");

        String name;
        while (rs.next()){
            name = rs.getString("name");
            getSteamPriceForGivenName(name,conn);
        }

        rs.close();
    }
}
