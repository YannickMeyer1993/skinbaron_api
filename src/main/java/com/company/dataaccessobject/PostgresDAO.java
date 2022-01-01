package com.company.dataaccessobject;

import com.company.model.Item;
import com.company.model.ItemCollection;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.company.common.PostgresHelper.*;

@Repository("postgres")
public class PostgresDAO implements ItemDAO {

    private final static Logger LOGGER = Logger.getLogger(PostgresDAO.class.getName());

    @Override
    public void init() throws SQLException, IOException {
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

            LOGGER.info(pstmt.toString());

            int[] updateCounts = pstmt.executeBatch();
            int amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                LOGGER.info(amountInserts + " items were inserted!");
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

            LOGGER.info(pstmt.toString());

            int[] updateCounts = pstmt.executeBatch();
            int amountInserts = IntStream.of(updateCounts).sum();
            if (amountInserts != 0) {
                LOGGER.info(amountInserts + " items were inserted!");
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

        LOGGER.info("table steam_iteration has a new entry for today.");

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
        LOGGER.info("Highest steam iteration for today is: " + iteration);
    }

    @Override
    public void insertInventoryItem(String ItemName, String InventoryType) throws Exception {
        List<String> name_list = new ArrayList<>();

        try (Connection connection = getConnection();Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("select name from steam_item_sale.item_informations")) {
            while (rs.next()) {
                name_list.add(rs.getString("name"));
            }
        }

        if (!name_list.contains(ItemName)) {
            return;
        }

        String SQLInsert = "INSERT INTO steam_item_sale.inventory(inv_type,name,still_there) "
                + "VALUES(?,?,true)";

        try (Connection connection = getConnection(); PreparedStatement pstmt = connection.prepareStatement(SQLInsert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, InventoryType);
            pstmt.setString(2,ItemName);
            pstmt.execute();
            connection.commit();
        }

        LOGGER.info("Item \""+ItemName+"\" was inserted to inventory.");
    }

    @Override
    public Item getItem(String ItemName) {
        ItemCollection collection = new ItemCollection("",false);
        Item item = new Item(ItemName, collection);
        //item.setSkinbaronPrice();
        return item;
    }

}
