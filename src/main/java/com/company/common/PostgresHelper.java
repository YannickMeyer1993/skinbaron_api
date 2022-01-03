package com.company.common;

import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

import static com.company.common.PasswordHelper.readPasswordFromFile;

public class PostgresHelper {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(PostgresHelper.class);

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
        logger.debug("Successfully Connected.");
        return conn;
    }

    public static void executeDDL(String statement) throws IOException, SQLException {


        logger.info(statement);
        String[] sqls = statement.split(";");

        if (sqls.length > 1){
            logger.info("Amount Statements: "+sqls.length);
        }
        
        for (String sql : sqls) {
            try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
                logger.info(sql);
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

        logger.info(fileContent);
        String[] sqls = fileContent.split(";");

        logger.info("Amount Statements: "+sqls.length);
        for (String sql : sqls) {
            try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
                logger.info(sql);
                st.execute(sql);
                connection.commit();
            }
        }
    }

    public static Boolean checkIfResultsetIsEmpty(String statement) throws Exception {
        logger.info(statement);
        try(Connection connection = getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(statement)) {
            return !rs.next();
        }
    }
}
