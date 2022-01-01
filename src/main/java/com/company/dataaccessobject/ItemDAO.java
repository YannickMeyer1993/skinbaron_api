package com.company.dataaccessobject;

import com.company.model.Item;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;

public interface ItemDAO {
    void init() throws Exception;

    void addSkinbaronItem(SkinbaronItem item) throws Exception;

    void addSteamPrice(SteamPrice price) throws Exception;

   String[] getItemsToBuy();

   int getHighestSteamIteration() throws Exception;

   void initHightestSteamIteration() throws Exception;

   void setHighestSteamIteration(int iteration) throws Exception;

   void insertInventoryItem(String ItemName, String InventoryType) throws Exception;

   Item getItem(String ItemName);
}
