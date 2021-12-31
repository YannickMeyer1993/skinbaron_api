package com.company.dataaccessobject;

import com.company.model.Price;
import com.company.model.SkinbaronItem;

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

        Connection connection = getConnection();
        try (PreparedStatement pstmt = connection.prepareStatement(SQLUpsert, Statement.RETURN_GENERATED_KEYS)) {

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
    public void addSteamPrice(Price price) {

    }

    @Override
    public String[] getItemsToBuy() {
        return new String[0];
    }
}
