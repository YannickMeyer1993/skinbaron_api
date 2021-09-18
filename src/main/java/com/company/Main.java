package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException, FileNotFoundException {
	// write your code here
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user","postgres");
        String password = readPasswordFromFile("C:/passwords/postgres.txt");
        props.setProperty("password",password);
        Connection conn = DriverManager.getConnection(url, props);
    }

    public static String readPasswordFromFile(String path) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(path));
        return sc.nextLine();
    }
}
