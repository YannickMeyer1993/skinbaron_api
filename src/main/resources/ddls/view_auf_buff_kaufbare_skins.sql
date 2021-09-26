-- steam_item_sale.auf_buff_kaufbare_skins source

CREATE OR REPLACE VIEW steam_item_sale.auf_buff_kaufbare_skins
AS SELECT smrp.name,
    (smrp.price_euro - 0.04::double precision) / buff.price_euro AS rati,
    buff.price_euro AS buff_preis,
    smrp.price_euro AS steam_preis,
    smrp.quantity AS anzahl_skins_auf_steam,
    smrp."timestamp" AS steam_timestamp,
    buff."timestamp" as buff_timestamp,
    s.dsratio,
    DATE_PART('day', now()-smrp."timestamp" ) in (0) as steam_preis_ok,
    DATE_PART('day', now()-buff."timestamp" ) in (0) buff_preis_ok,
    buff.id 
   FROM steam_item_sale.steam_most_recent_prices smrp
     LEFT JOIN steam_item_sale.buff_item_prices buff ON buff.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision AND buff.name::text !~~ 'Souvenir%'::text AND buff.name::text !~~ 'Sealed Graffiti%'::text AND smrp.quantity > 25
     JOIN steam_item_sale.durchschnittspreise_steam s ON buff.name::text = s.name::text
  WHERE buff.price_euro != 0 AND ((smrp.price_euro - 0.04::double precision) / buff.price_euro) >= 1.8::double precision
  ORDER BY ((smrp.price_euro - 0.04::double precision) / buff.price_euro) DESC;