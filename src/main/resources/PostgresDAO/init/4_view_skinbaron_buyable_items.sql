CREATE OR REPLACE VIEW steam.skinbaron_buyable_items AS
SELECT skinbaronitems.name,
    (smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision AS rati,
    skinbaronitems.price AS skinbaron_price,
    smrp.price_euro AS steam_price,
    smrp.quantity AS steam_quantity,
    smrp."date" AS steam_date,
    s.dsratio as steam_avg_ratio,
    ROUND(cast(EXTRACT(EPOCH FROM (now() - smrp."date"))/(60*60*24)as numeric),1) <= 1 as steam_price_is_new,
    string_agg(skinbaronitems.id,',') as skinbaron_ids,
    bcp.price_euro as buff_price,
    ROUND(cast(EXTRACT(EPOCH FROM (now() - bcp.insert_timestamp))/(60*60*24)as numeric),1) <= 1 as buff_price_is_new,
    bcp.has_exterior,
    bcp.id as buff_id
FROM steam.skinbaron_items skinbaronitems
JOIN steam.steam_current_prices smrp ON skinbaronitems.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision AND skinbaronitems.name::text !~~ 'Souvenir%'::text AND skinbaronitems.name::text !~~ 'Sealed Graffiti%'::text AND smrp.quantity > 50
and ((smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision) >= 1.8::double precision
JOIN steam.steam_avg_prices s ON skinbaronitems.name::text = s.name::text AND s.dsratio < 1.5::double precision
left join steam.buff_current_prices bcp on skinbaronitems."name" = bcp."name"
GROUP BY skinbaronitems.name, skinbaronitems.price, smrp.price_euro, smrp.quantity, smrp."date", ((smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision), s.dsratio,bcp.price_euro,bcp.insert_timestamp,bcp.id,bcp.has_exterior
ORDER BY ((smrp.price_euro - 0.04::double precision) / skinbaronitems.price::double precision) DESC;