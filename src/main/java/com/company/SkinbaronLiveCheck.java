package com.company;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.company.SkinbaronAPI.getBalance;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

@SuppressWarnings("BusyWait")
public class SkinbaronLiveCheck {
    private static Connection conn;

    public static void main(String[] args) throws FileNotFoundException, SQLException {

        int wait_counter = 3;

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        while (true) {
            try {
                Connection conn = getConnection();

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
