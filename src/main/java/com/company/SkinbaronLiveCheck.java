package com.company;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.company.SkinbaronAPI.getBalance;
import static com.company.common.readPasswordFromFile;

public class SkinbaronLiveCheck {
    private static Connection conn;

    public static void main(String[] args) throws FileNotFoundException, SQLException {

        int wait_counter = 3;

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        while (true) {
            try {
                String url = "jdbc:postgresql://localhost/postgres";
                Properties props = new Properties();
                props.setProperty("user", "postgres");
                String password = readPasswordFromFile("C:/passwords/postgres.txt");
                props.setProperty("password", password);
                conn = DriverManager.getConnection(url, props);
                conn.setAutoCommit(false);
                System.out.println("Successfully Connected.");
                System.out.println("Waiting for " + Math.pow(2, wait_counter) + " seconds");
                Thread.sleep((long) (Math.pow(2, wait_counter) * 1000));
                getBalance(secret,false,conn);
                break;
            } catch (Exception e) {
                System.out.println("Kaputt!!!!");
                wait_counter++;
                conn.close();
            }

        }
    }
}
