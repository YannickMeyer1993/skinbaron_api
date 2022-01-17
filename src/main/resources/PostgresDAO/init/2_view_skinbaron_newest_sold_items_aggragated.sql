create or replace view steam.skinbaron_newest_sold_items_aggregated as
select
	s.name,
	s.doppler_phase,
	ROUND(avg(s.price), 2) as avg_price,
	min(s.price) as min_price,
	max(s.price) as max_price,
	count(*) as amount,
	current_date as insert_date
from
	steam.skinbaron_newest_sold_items s
group by
	s.name ,
	s.doppler_phase;