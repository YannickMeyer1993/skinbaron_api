-- steam_item_sale.overview_bought_items_per_day source

CREATE OR REPLACE VIEW steam_item_sale.overview_bought_items_per_day
AS SELECT count(*) AS anzahl_skins,
    sum(bs.price) AS summe,
    date(bs."timestamp") AS datum
   FROM steam_item_sale.skinbaron_transactions bs
  WHERE bs.success
  GROUP BY (date(bs."timestamp"));