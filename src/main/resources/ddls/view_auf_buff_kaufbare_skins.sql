-- steam_item_sale.auf_buff_kaufbare_skins source
drop view steam_item_sale.auf_buff_kaufbare_skins;
CREATE OR REPLACE VIEW steam_item_sale.auf_buff_kaufbare_skins
as
select
	(buff.price_euro-0.04)/sb.price as ratio,
	sb.id as skinbaron_sales_id,
	sb.name,
	sb.price as skinbaron_price,
	sb."timestamp" as skinbaron_timestamp,
	buff.price_euro as buff_price,
	buff."timestamp" as buff_timestamp,
	steam.quantity as steam_quantity,
	steam.price_euro as steam_price,
	steam."timestamp" as steam_timestamp,
	ROUND(cast(EXTRACT(EPOCH FROM (now() - buff."timestamp"))/(60*60*24)as numeric),1) <= 1 as buff_preis_aktuell,
	ROUND(cast(EXTRACT(EPOCH FROM (now() - steam."timestamp"))/(60*60*24)as numeric),1) <= 1 as steam_preis_aktuell
from steam_item_sale.skinbaron_market_search_results sb
inner join steam_item_sale.buff_item_prices buff on sb."name" = buff."name" 
inner join steam_item_sale.steam_most_recent_prices steam
 		on steam."name" = buff."name"
 		and steam.price_euro <> 0
 		and buff."name" not like 'Souvenir%'::text
 		AND buff.name::text not like 'Sealed Graffiti%'::text
 		AND steam.quantity > 25
where sb.price <> 0 
and ((buff.price_euro - 0.04) / sb.price) >= 1.2 --minus fees
and buff.price_euro > 5 --only expensive items because not automatic
and steam.price_euro > buff.price_euro -- no outliers
order by (buff.price_euro-0.04)/sb.price desc;