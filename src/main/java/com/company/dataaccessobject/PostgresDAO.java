package com.company.dataaccessobject;

import com.company.model.Price;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.company.common.PostgresHelper.executeDDLfromPath;
import static com.company.common.PostgresHelper.getConnection;


public class PostgresDAO implements ItemDAO {

    private final static Logger LOGGER = Logger.getLogger(PostgresDAO.class.getName());

    @Override
    public void init() throws SQLException, IOException {
        executeDDLfromPath("src/main/resources/PostgresDAO/0_schema.sql");
        executeDDLfromPath("src/main/resources/PostgresDAO/1_table_skinbaron_items.sql");
        executeDDLfromPath("src/main/resources/PostgresDAO/1_table_steam_item_prices.sql");
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

        try (Connection connection = getConnection();PreparedStatement pstmt = connection.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

            for (SkinbaronItem item: items) {
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

    public void addSteamPrices(List<SteamPrice> prices) throws Exception{
        String Insert = "INSERT INTO steam.steam_prices(name,quantity,price_euro) VALUES(?,?,?)";

        try (Connection connection = getConnection();PreparedStatement pstmt = connection.prepareStatement(Insert, Statement.RETURN_GENERATED_KEYS)) {

            for (SteamPrice price: prices) {
                pstmt.setString(1,price.getItemName());
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
    public int getHighestSteamIteration() {
        return 0;
    }
}
