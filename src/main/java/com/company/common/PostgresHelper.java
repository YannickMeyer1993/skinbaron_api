package com.company.common;

import com.company.SkinbaronAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import static com.company.common.PasswordHelper.readPasswordFromFile;

public class PostgresHelper {

    private final static Logger LOGGER = Logger.getLogger(PostgresHelper.class.getName());

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

    public static void executeDDL(String statement) throws IOException, SQLException {


        LOGGER.info(statement);
        String[] sqls = statement.split(";");

        LOGGER.info("Amount Statements: "+sqls.length);
        for (String sql : sqls) {
            try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
                LOGGER.info(sql);
                st.execute(sql);
                connection.commit();
            }
        }
    }

    public static void executeDDLfromPath(String path) throws IOException, SQLException {

        if (!".sql".equals(path.substring(path.length()-4))) {
            throw new IllegalStateException("Only .sql Files are allowed.");
        }

        String fileContent = "";
        try {
            for (int i=0;i<Files.readAllLines(Paths.get(path)).size();i++) {
                fileContent = fileContent.concat("\n" + Files.readAllLines(Paths.get(path)).get(i));
            }
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        LOGGER.info(fileContent);
        String[] sqls = fileContent.split(";");

        LOGGER.info("Amount Statements: "+sqls.length);
        for (String sql : sqls) {
            try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
                LOGGER.info(sql);
                st.execute(sql);
                connection.commit();
            }
        }
    }

    public static Boolean checkIfResultsetIsEmpty(String statement) throws Exception {
        LOGGER.info(statement);
        try(Connection connection = getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(statement)) {
            return !rs.next();
        }
    }
}
