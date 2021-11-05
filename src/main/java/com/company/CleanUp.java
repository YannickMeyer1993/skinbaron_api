package com.company;

import java.io.FileNotFoundException;
import java.sql.*;

import static com.company.common.getConnection;

public class CleanUp {

    public static void main(String[] args) throws FileNotFoundException, SQLException {
        try(Connection conn = getConnection()) {

            try(Statement st = conn.createStatement()) {
                st.execute("delete from steam_item_sales.skinbaron_market_search_results where name like '%Sealed Graffiti%'");
            }

            try(Statement st2 = conn.createStatement()) {
                st2.execute("with deletable as (\n" +
                        "select\n" +
                        "\ts.name, s.wear, s.\"timestamp\", rank() over (partition by s.name, s.wear\n" +
                        "order by\n" +
                        "\ttimestamp desc) as ranking\n" +
                        "from\n" +
                        "\tsteam_item_sale.skinbaron_market_search_results s\n" +
                        "where\n" +
                        "\ts.wear != '0.0'\n" +
                        "group by\n" +
                        "\ts.name, s.wear, s.\"timestamp\" ),\n" +
                        "deletable_ids as (\n" +
                        "select\n" +
                        "\t*\n" +
                        "from\n" +
                        "\tsteam_item_sale.skinbaron_market_search_results smsr\n" +
                        "inner join deletable on\n" +
                        "\tsmsr.wear = deletable.wear\n" +
                        "\tand smsr.\"name\" = deletable.name\n" +
                        "\tand smsr.\"timestamp\" = deletable.timestamp \n" +
                        "\tand ranking > 1)\n" +
                        "delete from steam_item_sale.skinbaron_market_search_results where id in (select id from deletable_ids);");
            }

            try(Statement st3 = conn.createStatement()){
            st3.execute("with deletable_ids as(\n" +
                    "SELECT smsr.id\n" +
                    "\t\tfrom steam_item_sale.skinbaron_market_search_results smsr\n" +
                    "\t\tinner join steam_item_sale.steam_most_recent_prices\n" +
                    "\t\tusing (name)\n" +
                    "\t\twhere smsr .price > 3*steam_most_recent_prices.price_euro)\n" +
                    "delete from steam_item_sale.skinbaron_market_search_results where id in (select id from deletable_ids);");
            }
        }
    }

}
