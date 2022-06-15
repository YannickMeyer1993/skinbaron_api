create or replace
view steam.buff_current_prices
as
with max_ts as (
select
	distinct s.name,
	max(s.insert_timestamp) as insert_timestamp
from
	steam.buff_prices s
group by
	s.name
        )
 select
	distinct t.name,
	id,
	min(t.price_euro) as price_euro,
	t.insert_timestamp,
	has_exterior
from
	steam.buff_prices t
join max_ts on
	max_ts.name::text = t.name::text
	and max_ts.insert_timestamp = t.insert_timestamp
group by
	t.id,
	t.name,
	t.insert_timestamp,
	t.has_exterior ;