create or replace
view steam_item_sale.cases_infos as
select
	c."name",
	c.release_date ,
	smrp.quantity,
	smrp.price_euro,
	smrp.quantity* smrp.price_euro as market_volume,
	sum(coalesce(iwp.amount, 0)) as anzahl_vorhanden,
	c.still_get_dropped,
	c.rare_special_item,
	c.id 
from
	steam_item_sale.cases c
inner join steam.steam_current_prices smrp on
	c."name" = smrp ."name"
left join steam_item_sale.inventory_with_prices iwp on
	c."name" = iwp."name"
group by
	c.name,
	quantity ,
	price_euro,
	market_volume,
	c.release_date,
	c.still_get_dropped,
	c.rare_special_item,
	c.id
order by
	quantity