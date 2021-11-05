create or replace view steam_item_sale.steam_max_prices as
with max_prices as
(
	select distinct steam."name",max(price_euro) as price_euro from steam_item_sale.steam_item_prices steam
	group by "name" 
)
select distinct steam."name",max(steam.quantity) as max_quantity,max(steam."date") as date,max_prices.price_euro from steam_item_sale.steam_item_prices steam
inner join max_prices on max_prices.name = steam ."name" and max_prices.price_euro = steam .price_euro
group by steam ."name" ,max_prices.price_euro