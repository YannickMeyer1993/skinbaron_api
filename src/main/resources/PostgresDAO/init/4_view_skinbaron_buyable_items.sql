CREATE OR REPLACE VIEW steam.skinbaron_buyable_items AS
SELECT skinbaronitems.name,
    (smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision AS rati,
    skinbaronitems.price AS skinbaron_preis,
    smrp.price_euro AS steam_preis,
    smrp.quantity AS anzahl_skins_auf_steam,
    smrp."date" AS steam_date,
    s.dsratio,
    ROUND(cast(EXTRACT(EPOCH FROM (now() - smrp."date"))/(60*60*24)as numeric),1) <= 1 as steam_preis_aktuell
   FROM steam.skinbaron_items skinbaronitems
     JOIN steam.steam_current_prices smrp ON skinbaronitems.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision AND skinbaronitems.name::text !~~ '1Sticker |%'::text AND skinbaronitems.name::text !~~ 'Souvenir%'::text AND skinbaronitems.name::text !~~ 'Sealed Graffiti%'::text AND smrp.quantity > 50
     JOIN steam.steam_avg_prices s ON skinbaronitems.name::text = s.name::text AND s.dsratio < 1.5::double precision
  WHERE ((smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision) >= 1.7::double precision
  GROUP BY skinbaronitems.name, skinbaronitems.price, smrp.price_euro, smrp.quantity, smrp."date", ((smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision), s.dsratio
  ORDER BY ((smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision) DESC;