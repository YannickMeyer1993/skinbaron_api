package com.company.dataaccessobject;

import com.company.model.Price;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;

import java.io.FileNotFoundException;
import java.sql.SQLException;


public interface ItemDAO {
    void init() throws Exception;

    void addSkinbaronItem(SkinbaronItem item) throws Exception;

    void addSteamPrice(SteamPrice price) throws Exception;

   String[] getItemsToBuy();


}
