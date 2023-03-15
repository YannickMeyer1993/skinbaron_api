select
	*
from
	(
	select
		s.name,
		s.doppler_phase,
		round(avg(s.price), 2) as avg_price,
		min(s.price) as min_price,
		max(s.price) as max_price,
		count(*) as amount,
		max(cast(s.datesold as date)) - min(cast(s.datesold as date)) as date_interval
	from
		(
		select
			sold."name",
			price,
			wear,
			datesold,
			doppler_phase
		from
			steam.skinbaron_newest_sold_items sold
		inner join (select "name",max(insert_timestamp) as max_ts from steam.skinbaron_newest_sold_items sub group by "name") t on t."name" = sold."name" and t.max_ts = sold.insert_timestamp
		group by
			sold."name",
			price,
			wear,
			datesold,
			doppler_phase) s
	group by
		s.name,
		s.doppler_phase) sold
inner join steam.steam_current_prices steam
on
	sold."name" = steam."name"
inner join steam.item_informations infos
on sold."name" = infos."name"
where 1=1
	and sold.max_price >= steam.price_euro
	and infos.quality = 'Base Grade'
order by sold."name"
;