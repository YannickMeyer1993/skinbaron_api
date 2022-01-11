delete from steam_item_sale.skinbaron_market_search_results where name like '%Sealed Graffiti%';

with deletable as (
    select s.name,s.wear,s.timestamp, rank() over (partition by s.name, s.wear order by timestamp desc) as ranking
    from steam_item_sale.skinbaron_market_search_results s
    where s.wear != '0.0'
    group by s.name, s.wear, s.timestamp ),
deletable_ids as (
    select * from steam_item_sale.skinbaron_market_search_results smsr
    inner join deletable on smsr.wear = deletable.wear
    and smsr.name = deletable.name
    and smsr.timestamp = deletable.timestamp
    and ranking > 1)
delete from steam_item_sale.skinbaron_market_search_results where id in ( select id from deletable_ids);

with deletable_ids as(
select smsr.id
from steam_item_sale.skinbaron_market_search_results smsr
inner join steam.steam_current_prices using (name)
where smsr .price > 3 * steam_most_recent_prices.price_euro)
delete from steam_item_sale.skinbaron_market_search_results where id in ( select id from deletable_ids);