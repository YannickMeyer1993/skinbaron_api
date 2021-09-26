-- steam_item_sale.durchschnittspreise_steam source

CREATE OR REPLACE VIEW steam_item_sale.durchschnittspreise_steam
AS SELECT s.name,
    s.durchschnitt,
    t.price_euro AS jetziger_preis,
    t.price_euro - s.durchschnitt::double precision AS differenz,
    t.quantity,
    t."timestamp",
    t.price_euro / s.durchschnitt::double precision AS dsratio
   FROM ( SELECT t1.name,
            round(avg(t1.price_euro)::numeric, 2) AS durchschnitt
           FROM ( SELECT s1.name,
                    s1.price_euro,
                    s1."timestamp",
                    rank() OVER (PARTITION BY s1.name ORDER BY s1."timestamp" DESC) AS ranking
                   FROM ( SELECT sip.name,
                            sip.price_euro,
                            sip."timestamp",
                            rank() OVER (PARTITION BY sip.name, sip.date ORDER BY sip.date) AS ranking
                           FROM steam_item_sale.steam_item_prices sip) s1
                  WHERE s1.ranking = 1) t1
          WHERE t1.ranking <= 30
          GROUP BY t1.name) s
     JOIN steam_item_sale.steam_most_recent_prices t USING (name)
  WHERE t.price_euro <> 0::double precision
  ORDER BY (t.price_euro / s.durchschnitt::double precision) DESC;