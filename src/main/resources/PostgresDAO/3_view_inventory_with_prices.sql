CREATE OR REPLACE VIEW steam.inventory_with_prices
AS SELECT si.name,
       count(name) as amount,
       s.price_euro AS price_per_unit,
       s."date",
       si.inv_type
      FROM steam.inventory si
        LEFT JOIN steam.steam_current_prices s USING (name)
     WHERE si.still_there
     group by name,price_euro,s."date",inv_type
     ORDER BY s.price_euro DESC;