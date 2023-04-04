package de.yannickm.steambot.service;

import de.yannickm.steambot.dataaccessobject.ItemDAO;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class BuffService {

    private final ItemDAO itemdao;

    @Autowired
    public BuffService(@Qualifier("postgres") ItemDAO itemdao) {
        this.itemdao = itemdao;
    }

    public void insertBuffPrices(JSONArray payload) throws Exception {
        itemdao.insertBuffPrices(payload);
    }

    public String getBuffIds() throws SQLException {
        return itemdao.getBuffIds();
    }
}
