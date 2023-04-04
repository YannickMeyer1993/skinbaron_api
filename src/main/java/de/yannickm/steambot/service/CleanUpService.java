package de.yannickm.steambot.service;

import de.yannickm.steambot.dataaccessobject.ItemDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
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
