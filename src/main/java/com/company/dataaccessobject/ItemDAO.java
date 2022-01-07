package com.company.dataaccessobject;

import com.company.model.InventoryItem;
import com.company.model.Item;
import com.company.model.SkinbaronItem;
import com.company.model.SteamPrice;
import org.apache.xpath.operations.Bool;

public interface ItemDAO {
    void init() throws Exception;

    String addSkinbaronItem(SkinbaronItem item) throws Exception;

    void addSteamPrice(SteamPrice price) throws Exception;

   String[] getItemsToBuy();

   int getHighestSteamIteration() throws Exception;

   void initHightestSteamIteration() throws Exception;

   void setHighestSteamIteration(int iteration) throws Exception;

   void addInventoryItem(InventoryItem item) throws Exception;

   Item getItem(String ItemName);

   void deleteInventoryItems() throws Exception;

   void cleanUp() throws Exception;

   void crawlWearValues() throws Exception;

   void crawlItemInformations() throws Exception;

   String getLastSkinbaronId() throws Exception;


}
