package com.company.metrics;

import com.company.common.PostgresHelper;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static com.company.common.PostgresHelper.getConnection;

public class MetricsPusher {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MetricsPusher.class);

    public MetricsPusher(ArrayList<Metric> metricsList) throws SQLException {

        String Insert = "INSERT INTO monitoring.metrics(key,value,ts) VALUES(?,?,?)";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(Insert, Statement.RETURN_GENERATED_KEYS)) {

            for (Metric metric: metricsList) {
                pstmt.setString(1, metric.getKey());
                pstmt.setString(2, metric.getValue());
                pstmt.setTimestamp(3, metric.getTs());
                pstmt.addBatch();
            }

            logger.info(pstmt.toString());

            int[] updateCounts = pstmt.executeBatch();
            int amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                logger.info(amountInserts + " items were inserted!");
            }

            connection.commit();
        }
    }

    public static long getLongfromDateString(String stringDate) {

        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");
        try {
            java.util.Date d = f.parse(stringDate);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not parse date to long.");
        }
    }
}
