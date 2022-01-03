package com.company.dataaccessobject;

import com.company.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.company.common.PostgresHelper.*;

@Repository("postgres")
public class PostgresDAO implements ItemDAO {
    
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(PostgresDAO.class);

    public PostgresDAO() throws Exception {
        init();
    }

    @Override
    public void init() throws Exception {
        String resourcePath = "src/main/resources/PostgresDAO/";
        executeDDLfromPath(resourcePath + "0_schema.sql");
        executeDDLfromPath(resourcePath + "1_table_skinbaron_items.sql");
        executeDDLfromPath(resourcePath + "1_table_steam_item_prices.sql");
        executeDDLfromPath(resourcePath + "1_steam_iteration.sql");
        executeDDLfromPath(resourcePath + "1_table_inventory.sql");

        //all data is already inside the tables
    }

    @Override
    public void addSkinbaronItem(SkinbaronItem item) throws Exception {
        ArrayList<SkinbaronItem> list = new ArrayList<>();
        list.add(item);
        addSkinbaronItems(list);
    }

    public void addSkinbaronItems(List<SkinbaronItem> items) throws Exception {
        String SQLUpsert = "WITH\n" +
                "    to_be_upserted (id,name,price,stickers,wear) AS (\n" +
                "        VALUES\n" +
                "            (?,?,?,?,?)\n" +
                "    ),\n" +
                "    updated AS (\n" +
                "        UPDATE\n" +
                "            steam.skinbaron_items s\n" +
                "        SET\n" +
                "            price = to_be_upserted.price::numeric\n" +
                "        FROM\n" +
                "            to_be_upserted\n" +
                "        WHERE\n" +
                "            s.id = to_be_upserted.id\n" +
                "        RETURNING s.id\n" +
                "    )\n" +
                "INSERT INTO steam.skinbaron_items\n" +
                "    SELECT * FROM to_be_upserted\n" +
                "    WHERE id NOT IN (SELECT id FROM updated);";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            for (SkinbaronItem item : items) {
                pstmt.setString(1, item.getId());
                pstmt.setString(2, item.getName());
                pstmt.setDouble(3, item.getPrice().getValue());
                pstmt.setString(4, item.getStickers());
                pstmt.setDouble(5, item.getWear());
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

    @Override
    public void addSteamPrice(SteamPrice price) throws Exception {
        ArrayList<SteamPrice> list = new ArrayList<>();
        list.add(price);
        addSteamPrices(list);
    }

    public void addSteamPrices(List<SteamPrice> prices) throws Exception {
        String Insert = "INSERT INTO steam.steam_prices(name,quantity,price_euro) VALUES(?,?,?)";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(Insert, Statement.RETURN_GENERATED_KEYS)) {

            for (SteamPrice price : prices) {
                pstmt.setString(1, price.getItemName());
                pstmt.setInt(2, price.getQuantity());
                pstmt.setDouble(3, price.getValue());
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

    @Override
    public String[] getItemsToBuy() {
        return new String[0];
    }

    @Override
    public int getHighestSteamIteration() throws Exception {
        String query = "select iteration from steam.steam_iteration where \"date\" = CURRENT_DATE;";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (!rs.next()) {
                initHightestSteamIteration();
                return getHighestSteamIteration();
            } else {
                return rs.getInt("iteration"); //rs.next() was called above
            }
        }
    }

    @Override
    public void initHightestSteamIteration() throws Exception {

        Connection connection = getConnection();

        if (!checkIfResultsetIsEmpty("select iteration from steam.steam_iteration where \"date\" = CURRENT_DATE;")) {
            return;
        }
        String SQLinsert = "INSERT INTO steam.steam_iteration(iteration) "
                + "VALUES(0)";
        try (PreparedStatement pstmt = connection.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();
            connection.commit();
        }

        logger.info("table steam_iteration has a new entry for today.");

    }

    @Override
    public void setHighestSteamIteration(int iteration) throws Exception {

        String SQLinsert = "UPDATE steam.steam_iteration set iteration=? where \"date\"=current_date";

        if (checkIfResultsetIsEmpty("select iteration from steam.steam_iteration where \"date\" = CURRENT_DATE;")) {
            throw new Exception("steam.steam_iteration must be initialized.");
        }

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLinsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, iteration);

            pstmt.executeUpdate();
            connection.commit();
        }
        logger.info("Highest steam iteration for today is: " + iteration);
    }

    @Override
    public void addInventoryItem(InventoryItem item) throws Exception {

        String sql = "select name from steam_item_sale.item_informations where name =?;";
        String SQLInsert = "INSERT INTO steam.inventory(inv_type,name,still_there) "
                + "VALUES(?,?,true)";

        try (Connection connection = getConnection();
             PreparedStatement pstmt1 = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmt2 = connection.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt1.setString(1, item.getItemName());

            ResultSet rs = pstmt1.executeQuery();
            if (!rs.next()) {
                return;
            }

            pstmt2.setString(1, item.getInventoryType());
            pstmt2.setString(2, item.getItemName());
            pstmt2.execute();
            connection.commit();
        }

        logger.info("Item \""+ item.getItemName() +"\" was inserted to inventory.");
    }

    @Override
    public Item getItem(String ItemName) {
        ItemCollection collection = new ItemCollection("",false);
        return new Item(ItemName, collection);
    }

    @Override
    public void deleteInventoryItems() throws Exception {
        executeDDL("TRUNCATE TABLE steam.inventory");
    }
}
