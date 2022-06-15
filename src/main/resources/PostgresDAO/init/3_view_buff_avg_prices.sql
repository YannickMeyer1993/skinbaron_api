CREATE OR REPLACE VIEW steam.buff_avg_prices
AS SELECT s.name,
    s.durchschnitt,
    t.price_euro AS jetziger_preis,
    t.price_euro - s.durchschnitt::double precision AS differenz,
    t.insert_timestamp,
    t.price_euro / s.durchschnitt::double precision AS dsratio
   FROM ( SELECT t1.name,
            round(avg(t1.price_euro)::numeric, 2) AS durchschnitt
           FROM ( SELECT s1.name,
                    s1.price_euro,
                    s1.insert_timestamp,
                    rank() OVER (PARTITION BY s1.name ORDER BY s1.insert_timestamp DESC) AS ranking
                   FROM ( SELECT sip.name,
                            sip.price_euro,
                            sip.insert_timestamp ,
                            rank() OVER (PARTITION BY sip.name, sip.insert_timestamp ORDER BY sip.insert_timestamp) AS ranking
                           FROM steam.buff_prices sip) s1
                  WHERE s1.ranking = 1) t1
          WHERE t1.ranking <= 30
          GROUP BY t1.name) s
     JOIN steam.buff_current_prices t USING (name)
  WHERE t.price_euro <> 0::double precision
  ORDER BY (t.price_euro / s.durchschnitt::double precision) DESC;