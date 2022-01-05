CREATE OR REPLACE VIEW steam_item_sale.auf_skinbaron_kaufbare_skins_investment_skins
AS SELECT smsr.id,
    smsr.name,
    smsr.price AS skinbaron_price,
    smrp.price_euro AS steam_price,
    smrp.quantity,
    smrp."timestamp" AS steam_timestamp,
    (smrp.price_euro - 0.03::double precision) / smsr.price::double precision AS ratio,
    s.dsratio
   FROM steam_item_sale.skinbaron_market_search_results smsr
     JOIN steam_item_sale.steam_most_recent_prices smrp ON smsr.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision AND smsr.name::text !~~ 'Souvenir%'::text AND smsr.name::text !~~ 'Sealed Graffiti%'::text AND smrp.quantity > 25
     JOIN steam_item_sale.durchschnittspreise_steam s ON smsr.name::text = s.name::text AND s.dsratio < 1.8::double precision
     JOIN steam_item_sale.item_informations ii ON ii.name::text = smsr.name::text
     JOIN steam_item_sale.investment_skin_names isn ON isn.name::text = smsr.name::text
  WHERE ((smrp.price_euro - 0.03::double precision) / smsr.price::double precision) > 1.5::double precision
  GROUP BY smsr.id, smsr.name, smsr.price, smrp.price_euro, smrp.quantity, smrp."timestamp", ((smrp.price_euro - 0.03::double precision) / smsr.price::double precision), s.dsratio
  ORDER BY ((smrp.price_euro - 0.03::double precision) / smsr.price::double precision) DESC;