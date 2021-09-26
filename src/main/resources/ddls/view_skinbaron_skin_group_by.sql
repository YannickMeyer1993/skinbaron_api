-- steam_item_sale.skinbaron_skin_group_by source

CREATE OR REPLACE VIEW steam_item_sale.skinbaron_skin_group_by
AS SELECT smsr.name,
    count(*) AS count
   FROM steam_item_sale.skinbaron_market_search_results smsr
  GROUP BY smsr.name
  ORDER BY (count(*)) DESC;