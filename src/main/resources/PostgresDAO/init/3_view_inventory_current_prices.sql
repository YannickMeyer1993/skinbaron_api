CREATE OR REPLACE VIEW steam.inventory_current_prices
AS
select
	si.name,
	si.amount,
	s.price_euro as price_per_unit,
	s."date",
	si.inv_type,
	sii.start_index
from
	steam.inventory si
left join steam.steam_current_prices s on si."name" = s."name"
left join steam.steam_item_indexes sii on sii."name" = si."name"
where
	si.still_there
order by
	s.price_euro desc;