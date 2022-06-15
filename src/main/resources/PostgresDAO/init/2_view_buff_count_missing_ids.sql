create or replace view steam.buff_count_missing_ids as
select count(distinct name) from steam.item_informations ii where name not in (select distinct name from steam.buff_prices);