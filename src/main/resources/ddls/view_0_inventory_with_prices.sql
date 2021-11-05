-- steam_item_sale.inventory_with_prices source

CREATE OR REPLACE VIEW steam_item_sale.inventory_with_prices
AS SELECT si.name,
    si.amount,
    s.price_euro AS price_per_unit,
    s."timestamp",
    si.inv_type
   FROM steam_item_sale.inventory si
     LEFT JOIN steam_item_sale.steam_most_recent_prices s USING (name)
  WHERE si.still_there
  ORDER BY s.price_euro DESC;