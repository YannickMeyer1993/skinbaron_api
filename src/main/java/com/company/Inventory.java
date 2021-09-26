package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.company.SkinbaronAPI.getSkinbaronInventory;
import static com.company.common.readPasswordFromFile;

public class Inventory {

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

        getSkinbaronInventory(secret,conn);
        conn.close();
    }
}
