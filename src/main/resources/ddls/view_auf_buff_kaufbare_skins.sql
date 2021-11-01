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
   
   INNER JOIN steam_item_sale.buff_item_prices buff on smrp."name" = buff."name" 
   LEFT JOIN steam_item_sale.buff_item_prices buff ON buff.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision AND buff.name::text !~~ 'Souvenir%'::text AND buff.name::text !~~ 'Sealed Graffiti%'::text AND smrp.quantity > 25
   JOIN steam_item_sale.durchschnittspreise_steam s ON buff.name::text = s.name::text
  WHERE buff.price_euro != 0 AND ((smrp.price_euro - 0.04::double precision) / buff.price_euro) >= 1.8::double precision
  ORDER BY ((smrp.price_euro - 0.04::double precision) / buff.price_euro) DESC;
  
 
 
 select (buff.price_euro-0.04)/sb.price as ratio,sb.id as skinbaron_sales_id,sb.name,sb.price as skinbaron_price,sb."timestamp" as skinbaron_timestamp,buff.price_euro as buff_price, buff."timestamp" as buff_timestamp, steam.quantity as steam_quantity, steam.price_euro as steam_price, steam."timestamp" as steam_timestamp from steam_item_sale.skinbaron_market_search_results sb
 inner join steam_item_sale.buff_item_prices buff on sb."name" = buff."name" 
 inner join steam_item_sale.steam_most_recent_prices steam
 		on steam."name" = buff."name"
 		and steam.price_euro <> 0
 		and buff."name" !~~ 'Souvenir%'::text
 		AND buff.name::text !~~ 'Sealed Graffiti%'::text
 		AND steam.quantity > 25
 where sb.price <> 0 
 and ((buff.price_euro - 0.04) / sb.price) >= 1.1 --minus fees
 and buff.price_euro > 5 --only expensive items because not automatic
 order by (buff.price_euro-0.04)/sb.price desc;