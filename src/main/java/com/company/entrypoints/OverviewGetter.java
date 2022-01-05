package com.company.entrypoints;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

import static com.company.common.PasswordHelper.readPasswordFromFile;
import static com.company.common.PostgresHelper.getConnection;
import static com.company.entrypoints.SteamCrawler.getSteamPriceForGivenName;

//TODO
public class OverviewGetter {

    private static double smurf_inv_value;
    private static double skinbaron_open_sale_wert;
    private static double steam_inv_value   ;
    private static double skinbaron_inv_value ;
    private static double sum_rare_items ;
    private static double steam_balance;
    private static double steam_sales_value;

    private final static Logger logger = LoggerFactory.getLogger(OverviewGetter.class);

    public static void main(String[] args) throws Exception {

        System.out.println("Decimal separator is comma!");
        Scanner sc= new Scanner(System.in);    //System.in is a standard input stream
        System.out.println("Enter Steam Balance: ");
        steam_balance = sc.nextDouble();
        System.out.println("Enter Steam Sales Value: ");
        steam_sales_value = sc.nextDouble();

        Connection conn = getConnection();

        try(Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery("select highest_iteration_steam+1 as iteration from steam_item_sale.overview where \"DATE\" = CURRENT_DATE;")) {

            if (!rs.next()) //Start of today
            {
                setRowInOverviewTable(conn);
            }
        }


        try(Statement st = conn.createStatement()) {
            st.execute("UPDATE steam_item_sale.inventory set still_there = false;");
        }

        //TODO trigger InventoryCrawler
        getItemPricesInventory(conn);
        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");
        getBalance(secret,true);

        try(Statement stmt2 = conn.createStatement();
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
                    "inner join skinbaron_open_sales on 1=1")) {

            rs2.next();

            smurf_inv_value = rs2.getDouble("smurf_inv_value");
            skinbaron_open_sale_wert = rs2.getDouble("skinbaron_open_sales_value");
            steam_inv_value = rs2.getDouble("steam_inv_value");
            skinbaron_inv_value = rs2.getDouble("skinbaron_inv_value");


        }

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
                "            s.\"DATE\" = CURRENT_DATE";


        try(Statement stmt3 = conn.createStatement();ResultSet rs3 = stmt3.executeQuery("select sum(zusatz_wert) as sum_rare_items from steam_item_sale.rare_skins;")){
            rs3.next();
            sum_rare_items = rs3.getDouble("sum_rare_items");
        }

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpdate, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, smurf_inv_value);
            pstmt.setDouble(2, skinbaron_open_sale_wert);
            pstmt.setDouble(3, steam_inv_value);
            pstmt.setDouble(4, skinbaron_inv_value);
            pstmt.setDouble(5, sum_rare_items);
            pstmt.setDouble(6, steam_balance);
            pstmt.setDouble(7, steam_sales_value);

            pstmt.addBatch();

            int[] updateCounts = pstmt.executeBatch();
            System.out.println(updateCounts.length + " were inserted!");
            conn.commit();
            if (updateCounts.length==0){
                throw new IllegalStateException("Nothing was inserted into table OVERVIEW!");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void getItemPricesInventory(Connection conn) throws Exception {


        try(Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery("select \n" +
                "distinct name from steam_item_sale.inventory_with_prices s\n" +
                "where (round((date_part('epoch'::text, now() - s.\"timestamp\" ::timestamp with time zone) / (60 * 60 * 24)::double precision)::numeric, 1) > 1)  order by name")) {
            String name;
            while (rs.next()) {
                name = rs.getString("name");
                getSteamPriceForGivenName(name);
            }
        }
    }

    public static Double getBalance(String secret, Boolean overwriteDB) throws Exception {

        Connection conn = getConnection();


        logger.info("Skinbaron API GetBalance has been called.");
        String jsonInputString = "{\"apikey\": \"" + secret + "\"}";

        HttpPost httpPost = new HttpPost("https://api.skinbaron.de/GetBalance");
        httpPost.setHeader("Content.Type", "application/json");
        httpPost.setHeader("x-requested-with", "XMLHttpRequest");
        httpPost.setHeader("Accept", "application/json");

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        HttpEntity entity = new ByteArrayEntity(jsonInputString.getBytes(StandardCharsets.UTF_8));
        httpPost.setEntity(entity);
        HttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());

        JSONObject resultJson = (JSONObject) new JSONTokener(result).nextValue();

        if (resultJson.has("message")) {
            System.out.println("Result: " + resultJson.get("message"));
            throw new Exception((String) resultJson.get("message"));
        }

        double skinbaronBalance = resultJson.getDouble("balance");

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select count(*) from steam_item_sale.overview where \"DATE\" = CURRENT_DATE;")) {

            if (!rs.next()) //Start of today
            {
                setRowInOverviewTable(conn);
            }
        }

        String SQLUpdate = "Update steam_item_sale.overview set skinbaron_balance =  ? where \"DATE\" = CURRENT_DATE;";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLUpdate, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, skinbaronBalance);
            pstmt.executeUpdate();
        }

        logger.info("Skinbaron Balance ist zur Zeit bei: " + skinbaronBalance + " Euro.");
        conn.close();
        return skinbaronBalance;
    }

    public static void setRowInOverviewTable(Connection conn) throws SQLException {
        String SQLinsert = "INSERT INTO steam_item_sale.overview(\"DATE\",highest_iteration_steam,steam_balance,steam_open_sales,skinbaron_balance,smurf_inv_value,skinbaron_open_sales_wert,steam_inv_value,skinbaron_inv_value,kommentar) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, 0);
            pstmt.setInt(3, 0);
            pstmt.setInt(4, 0);
            pstmt.setInt(5, 0);
            pstmt.setInt(6, 0);
            pstmt.setInt(7, 0);
            pstmt.setInt(8, 0);
            pstmt.setInt(9, 0);
            pstmt.setString(10, "");
            int rowsAffected = pstmt.executeUpdate();
        }
    }

    public static void setIterationCounter(@NotNull Connection conn, int i) {
        String SQLinsert = "UPDATE steam_item_sale.overview set highest_iteration_steam=? where \"DATE\"=current_date";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, i);

            //System.out.println(pstmt);
            int rowsAffected = pstmt.executeUpdate();
            conn.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}
