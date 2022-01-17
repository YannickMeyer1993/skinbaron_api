create or replace
view steam.buff_current_prices
as
select
	distinct
	id,
	has_exterior,
	insert_timestamp,
	name,
	price_euro
from
	(
	select
		distinct buff.id,
		has_exterior,
		insert_timestamp,
		name,
		price_euro,
            rank() over (partition by bii.name
	order by
		buff.insert_timestamp desc,
		buff.price_euro) as ranking
	from
		steam.buff_prices buff inner join steam.buff_item_informations bii on buff.id=bii.id) t
where
	t.ranking = 1
	or t.ranking is null
order by id;