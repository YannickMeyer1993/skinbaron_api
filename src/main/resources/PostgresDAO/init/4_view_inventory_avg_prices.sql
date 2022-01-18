create or replace view steam.inventory_avg_prices as
select
	s.name,
	string_agg(distinct s.inv_type , ', ') as inv_type,
	s.price_per_unit,
	sum(s.amount) as amount,
	ds.dsratio
from
	steam.inventory_current_prices s
inner join steam.steam_avg_prices ds
on
	s."name" = ds."name"
group by
	s.name,
	s.price_per_unit,
	ds.dsratio
order by
	ds.dsratio desc