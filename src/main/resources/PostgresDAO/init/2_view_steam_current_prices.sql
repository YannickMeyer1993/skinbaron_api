CREATE OR REPLACE VIEW steam.steam_current_prices
   AS with max_ts as
    ( select distinct "name",max("date") as last_date from steam.steam_prices group by "name")
    SELECT DISTINCT t."name",
       min(t.quantity) as quantity ,
       min(t.price_euro) as price_euro ,
       t."date"
      FROM steam.steam_prices t inner join max_ts on max_ts."name" = t."name" and max_ts.last_date = t."date"
     group by t."name" ,t."date" ;