package com.company;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;

import static com.company.Main.readPasswordFromFile;
import static com.company.Main.setIterationCounter;

public class MainTest extends TestCase {

    public void testSetIterationCounter() throws SQLException, FileNotFoundException {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password", password);
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("Successfully Connected.");

        System.out.println(Date.valueOf("2000-01-01").toString());
        setIterationCounter(conn,0);

        conn.close();

    }
}