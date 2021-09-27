-- steam_item_sale.auf_skinbaron_kaufbare_skins source

CREATE OR REPLACE VIEW steam_item_sale.auf_skinbaron_kaufbare_skins AS
SELECT smsr.name,
    (smrp.price_euro - 0.04::double precision) / smsr.price::double precision AS rati,
    smsr.price AS skinbaron_preis,
    smrp.price_euro AS steam_preis,
    smrp.quantity AS anzahl_skins_auf_steam,
    smrp."timestamp" AS steam_timestamp,
    s.dsratio,
    ROUND(cast(EXTRACT(EPOCH FROM (now() - smrp."timestamp"))/(60*60*24)as numeric),1) <= 1 as steam_preis_aktuell
   FROM steam_item_sale.skinbaron_market_search_results smsr
     JOIN steam_item_sale.steam_most_recent_prices smrp ON smsr.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision AND smsr.name::text !~~ '1Sticker |%'::text AND smsr.name::text !~~ 'Souvenir%'::text AND smsr.name::text !~~ 'Sealed Graffiti%'::text AND smrp.quantity > 50
     JOIN steam_item_sale.durchschnittspreise_steam s ON smsr.name::text = s.name::text AND s.dsratio < 1.5::double precision
  WHERE ((smrp.price_euro - 0.04::double precision) / smsr.price::double precision) >= 1.6::double precision
  GROUP BY smsr.name, smsr.price, smrp.price_euro, smrp.quantity, smrp."timestamp", ((smrp.price_euro - 0.04::double precision) / smsr.price::double precision), s.dsratio
  ORDER BY ((smrp.price_euro - 0.04::double precision) / smsr.price::double precision) DESC;