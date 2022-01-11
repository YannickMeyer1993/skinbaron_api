-- steam_item_sale.durchschnittspreise_investment_skins source

CREATE OR REPLACE VIEW steam_item_sale.durchschnittspreise_investment_skins
AS SELECT s.name,
    s.price_euro,
    s."timestamp",
    ds.dsratio,
    s.quantity 
   FROM steam_item_sale.investment_skin_names
     JOIN steam_item_sale.durchschnittspreise_steam ds USING (name)
     JOIN steam.steam_current_prices s USING (name)
  GROUP BY s.name, s.price_euro, s."timestamp", ds.dsratio,s.quantity 
  ORDER BY ds.dsratio DESC;