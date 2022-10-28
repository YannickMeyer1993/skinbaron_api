package com.company.service;

import com.company.dataaccessobject.ItemDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SteamService {

    private final ItemDAO itemdao;

    @Autowired
    public SteamService(@Qualifier("postgres") ItemDAO itemdao) {
        this.itemdao = itemdao;
    }

    public void setOverview(double steambalance, double steamopensales, double skinbaronbalance) throws Exception {
        itemdao.insertOverviewRow(steambalance,steamopensales,skinbaronbalance);
    }
}
