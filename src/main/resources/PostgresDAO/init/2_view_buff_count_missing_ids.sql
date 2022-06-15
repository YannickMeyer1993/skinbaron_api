-- steam.buff_count_missing_ids source

CREATE OR REPLACE VIEW steam.buff_count_missing_ids
AS SELECT ii.*
   FROM steam.item_informations ii
  WHERE NOT (ii.name::text IN ( SELECT DISTINCT buff_prices.name
           FROM steam.buff_prices
          WHERE buff_prices.name IS NOT NULL));