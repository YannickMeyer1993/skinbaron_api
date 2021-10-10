select
	distinct wear_werte.name as input_name,
	ii.quality as input_grade,
	wear_werte.max_wear as input_max,
	wear_werte.min_wear as input_min,
	ii.collection,
	mg2.grade as outcome_grade,
	wpg.count::decimal as amount_outcomes,
	case
		when replace(ii2.name, 'StatTrak� ', '') != ii2.name then 'StatTrak'
		else 'Normal'
	end as type,
	REGEXP_REPLACE(replace(ii2.name, 'StatTrak� ', ''), ' [(].*[)]', '') as outcome_name,
	iw2.max_wear -iw2.min_wear as outcome_range,
	iw2.min_wear as outcome_min
from
	steam_item_sale.item_wears wear_werte
inner join steam_item_sale.item_informations ii on
	REGEXP_REPLACE(replace(ii.name, 'StatTrak™ ', ''), ' [(].*[)]', '') = wear_werte.name
inner join steam_item_sale.meta_grades mg on
	quality = mg.grade
inner join steam_item_sale.meta_grades mg2 on
	mg.id + 1 = mg2.id
inner join steam_item_sale.weapons_per_grade wpg on
	wpg.quality = mg2.grade
	and wpg.collection = ii.collection
inner join steam_item_sale.item_informations ii2 on
	ii2.quality = mg2.grade
	and ii2.collection = wpg.collection
	and ii2."name" not like 'Souvenir%'
inner join steam_item_sale.item_wears iw2 on
	iw2."name" = REGEXP_REPLACE(replace(ii.name, 'StatTrak™ ', ''), ' [(].*[)]', '');

