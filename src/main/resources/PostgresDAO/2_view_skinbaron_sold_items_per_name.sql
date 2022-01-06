create or replace
view steam.skinbaron_sold_items_per_name as
select
	name,
	sum(price) as sum_price,
	sum(commission) as sum_commision
from
	steam.skinbaron_sold_items
group by
	name
order by
	sum(price) desc;