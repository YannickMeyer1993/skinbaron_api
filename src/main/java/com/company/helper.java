package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class helper {
    public static String readPasswordFromFile(String path) throws Exception {
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNext()) {
            String line = sc.next();
            if (line.indexOf("password") == 0) {
                return line.split("=")[1];
            }
        }
        throw new Exception("No password within file!");
    }

    public static Connection getConnection() throws SQLException, FileNotFoundException {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = null;
        try {
            password = readPasswordFromFile("C:/passwords/postgres.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");
        return conn;
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }

    public static void printBatchUpdateException(BatchUpdateException b) {

        System.err.println("----BatchUpdateException----");
        System.err.println("SQLState:  " + b.getSQLState());
        System.err.println("Message:  " + b.getMessage());
        System.err.println("Vendor:  " + b.getErrorCode());
        System.err.print("Update counts:  ");
        int[] updateCounts = b.getUpdateCounts();

        for (int updateCount : updateCounts) {
            System.err.print(updateCount + "   ");
        }
    }
}
