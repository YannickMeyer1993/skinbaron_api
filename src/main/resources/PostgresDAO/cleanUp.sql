delete from steam.skinbaron_items where name like '%Sealed Graffiti%';

with deletable_ids as(
select smsr.id
from steam.skinbaron_items smsr
inner join steam.steam_current_prices using (name)
where smsr .price > 3 * steam.steam_current_prices.price_euro)
delete from steam.skinbaron_items where id in ( select id from deletable_ids);