CREATE OR REPLACE VIEW steam_item_sale.inventory_differenz_vortag
AS 
with ordered_prices as(
select
	"name",
	price_euro,
	timestamp,
	rank() over ( partition by name
order by
	"timestamp" desc) as ranking
from
	steam_item_sale.steam_item_prices sip
where price_euro != 0
group by
	"name",
	price_euro,
	timestamp
), vortag as (
select * from ordered_prices where ranking = 2
), heute as (
select * from ordered_prices where ranking = 1)
select vortag.name,vortag.price_euro as vorheriger_spreis,heute.price_euro as aktueller_preis,vortag."timestamp", heute.price_euro - vortag.price_euro as differenz  from vortag inner join heute on heute.name = vortag.name 
inner join steam_item_sale.inventory i2 on i2."name" = vortag.name and i2.still_there; 