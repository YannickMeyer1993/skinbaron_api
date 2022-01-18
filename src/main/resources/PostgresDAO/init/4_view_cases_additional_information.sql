create or replace
view steam.cases_additional_information as
select
	c."name",
	c.release_date ,
	smrp.quantity as market_quantity,
	smrp.price_euro,
	smrp.quantity* smrp.price_euro as market_volume,
	sum(coalesce(iwp.amount, 0)) as anzahl_vorhanden,
	c.still_get_dropped,
	smrp.quantity/price_euro as quantity_per_euro,
	c.id,
	ds.dsratio
from
	steam.cases c
inner join steam.steam_current_prices smrp on
	c."name" = smrp ."name"
left join steam.inventory_current_prices iwp on
	c."name" = iwp."name"
inner join steam.steam_avg_prices ds on
	c."name" = ds."name"
group by
	c.name,
	smrp.quantity ,
	price_euro,
	market_volume,
	c.release_date,
	c.still_get_dropped,
	c.id,
	ds.dsratio
order by
	smrp.quantity