package com.company.service;

import com.company.dataaccessobject.ItemDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CleanUpService {

    private final ItemDAO itemdao;

    @Autowired
    public CleanUpService(@Qualifier("postgres") ItemDAO itemdao) {
        this.itemdao = itemdao;
    }

    public void cleanUp() throws Exception {
        itemdao.cleanUp();
    }
}
