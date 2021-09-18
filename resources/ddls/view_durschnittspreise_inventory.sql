create or replace view durchschnittspreise_inventory as 
select s.name,ii.collection ,string_agg(DISTINCT s.inv_type ,', ') as inv_type,s.price_per_unit,sum(s.amount) as amount,ds.dsratio from inventory_with_prices s
inner join durchschnittspreise_steam ds
using(name)
inner join item_informations ii 
using(name)
group by name,ii.collection,s.price_per_unit,ds.dsratio
order by ds.dsratio desc