create or replace view steam.steam_item_indexes as
select
	name,
	max(start_index) as start_index
from
	steam.steam_prices s
group by 1
order by 2;