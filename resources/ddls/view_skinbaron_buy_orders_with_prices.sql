CREATE OR REPLACE VIEW steam_item_sale.skinbaron_buy_orders_with_prices as
SELECT smsr.name,
    (smrp.price_euro - 0.03::double precision) / smsr.price::double precision AS rati,
    smsr.price AS skinbaron_preis,
    smrp.price_euro AS steam_preis,
    smrp.quantity AS anzahl_skins_auf_steam,
    smrp."timestamp" AS steam_timestamp,
    s.dsratio,
    smsr.id 
   FROM  steam_item_sale.skinbaron_buy_orders buyorders   
   INNER JOIN steam_item_sale.skinbaron_market_search_results smsr on buyorders.name = smsr."name" 
     JOIN steam_item_sale.steam_most_recent_prices smrp ON smsr.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision
     JOIN steam_item_sale.durchschnittspreise_steam s ON smsr.name::text = s.name::text AND s.dsratio < 1.4::double precision
  WHERE  ((smrp.price_euro - 0.03::double precision) / smsr.price::double precision) >= buyorders.buy_ratio ::double precision
  ORDER BY ((smrp.price_euro - 0.03::double precision) / smsr.price::double precision) DESC;