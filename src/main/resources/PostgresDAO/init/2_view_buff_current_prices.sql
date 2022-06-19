-- steam.buff_current_prices source

CREATE OR REPLACE VIEW steam.buff_current_prices
AS WITH max_ts AS (
         SELECT DISTINCT s.id,
            max(s.insert_timestamp) AS insert_timestamp
           FROM steam.buff_prices s
          GROUP BY s.id
        )
 SELECT DISTINCT t.name,
    t.id,
    min(t.price_euro) AS price_euro,
    t.insert_timestamp,
    t.has_exterior
   FROM steam.buff_prices t
     JOIN max_ts ON max_ts.id::text = t.id::text AND max_ts.insert_timestamp = t.insert_timestamp
  GROUP BY t.id, t.name, t.insert_timestamp, t.has_exterior;