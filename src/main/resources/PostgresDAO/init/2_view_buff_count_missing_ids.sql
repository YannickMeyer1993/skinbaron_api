-- steam.buff_count_missing_ids source

CREATE OR REPLACE VIEW steam.buff_count_missing_ids
AS SELECT count(DISTINCT ii.name) AS count
   FROM steam.item_informations ii
  WHERE NOT (ii.name::text IN ( SELECT DISTINCT buff_prices.name
           FROM steam.buff_prices where "name" is not null));