package com.company.dataaccessobject;

import com.company.model.Price;
import com.company.model.SkinbaronItem;

import java.io.FileNotFoundException;
import java.sql.SQLException;


public interface ItemDAO {
    void init() throws Exception;

    void addSkinbaronItem(SkinbaronItem item) throws Exception;

    void addSteamPrice(Price price);

   String[] getItemsToBuy();


}
