package com.company;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

import static com.company.SkinbaronAPI.getBalance;
import static com.company.SteamCrawler.setRowInOverviewTable;
import static com.company.SteamItemPriceChecker.getSteamPriceForGivenName;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;
import static java.lang.Math.min;

public class Overview {

    private static double smurf_inv_value;
    private static double skinbaron_open_sale_wert;
    private static double steam_inv_value   ;
    private static double skinbaron_inv_value ;
    private static double sum_rare_items ;
    private static double steam_balance;
    private static double steam_sales_value;

    public static void main(String[] args) throws Exception {

        Scanner sc= new Scanner(System.in);    //System.in is a standard input stream
        System.out.println("Enter Steam Balance: ");
        steam_balance = sc.nextDouble();
        System.out.println("Enter Steam Sales Value: ");
        steam_balance = sc.nextDouble();

        Connection conn = getConnection();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select highest_iteration_steam+1 as iteration from steam_item_sale.overview where \"DATE\" = CURRENT_DATE;");

        if (!rs.next()) //Start of today
        {
            setRowInOverviewTable(conn);
        }
        rs.close();
        stmt.close();

        Statement st = conn.createStatement();
        st.execute("UPDATE steam_item_sale.inventory set still_there = false;");
        st.close();

        Inventory.main(null);
        getItemPricesInventory(conn);
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        getBalance(secret,true,conn);

        Statement stmt2 = conn.createStatement();
        ResultSet rs2 = stmt2.executeQuery("with smurf as \n" +
                "(select round(cast(x.smurf_inv_wert as numeric),2) as smurf_inv_value from (select t.smurf_inv_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as smurf_inv_wert\n" +
                "\tfrom steam_item_sale.inventory_with_prices si where inv_type = 'smurf' ) t) x),\n" +
                "skinbaron_inv as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as skinbaron_inv_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam_item_sale.inventory_with_prices si where inv_type = 'skinbaron' ) t) w),\n" +
                "steam_inv as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as steam_inv_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam_item_sale.inventory_with_prices si where inv_type = 'steam' or inv_type like 'storage%' ) t) w),\n" +
                "skinbaron_open_sales as \n" +
                "(select ROUND(cast(w.skinbaron_wert as numeric),2) as skinbaron_open_sales_value from ( select t.skinbaron_wert \n" +
                "\tfrom ( select sum(si.amount*si.price_per_unit) as skinbaron_wert\n" +
                "\tfrom steam_item_sale.inventory_with_prices si where inv_type = 'skinbaron_sales' ) t) w)\n" +
                "select smurf.*,skinbaron_open_sales.*,steam_inv.*,skinbaron_inv.* from smurf\n" +
                "inner join skinbaron_inv on 1=1\n" +
                "inner join steam_inv on 1=1\n" +
                "inner join skinbaron_open_sales on 1=1");

        rs2.next();

        smurf_inv_value =            rs2.getDouble("smurf_inv_value");
        skinbaron_open_sale_wert =   rs2.getDouble("skinbaron_open_sales_value");
        steam_inv_value =            rs2.getDouble("steam_inv_value");
        skinbaron_inv_value =        rs2.getDouble("skinbaron_inv_value");


        rs2.close();
        stmt2.close();

        String SQLUpdate = "UPDATE\n" +
                "            steam_item_sale.overview s\n" +
                "        SET\n" +
                "                smurf_inv_value = ?::numeric\n" +
                "                ,skinbaron_open_sales_wert = ?::numeric\n" +
                "                ,steam_inv_value = ?::numeric\n" +
                "                ,skinbaron_inv_value = ?::numeric\n" +
                "                ,summe_rare_items = ?::numeric\n" +
                "                ,steam_balance = ?::numeric\n" +
                "                ,steam_open_sales = ?::numeric\n" +
                "        WHERE\n" +
                "            s.\"DATE\" = '2021-09-26'";


        Statement stmt3 = conn.createStatement();
        ResultSet rs3 = stmt3.executeQuery("select sum(zusatz_wert) as sum_rare_items from steam_item_sale.rare_skins;");

        rs3.next();
        sum_rare_items = rs3.getDouble("sum_rare_items");

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpdate, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, smurf_inv_value);
            pstmt.setDouble(2, skinbaron_open_sale_wert);
            pstmt.setDouble(3, steam_inv_value);
            pstmt.setDouble(4, skinbaron_inv_value);
            pstmt.setDouble(5, sum_rare_items);
            pstmt.setDouble(5, steam_balance);
            pstmt.setDouble(5, steam_sales_value);

            int[] updateCounts = pstmt.executeBatch();
            System.out.println(updateCounts.length + " were inserted!");

            conn.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

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
