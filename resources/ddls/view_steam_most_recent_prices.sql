-- steam_item_sale.steam_most_recent_prices source

CREATE OR REPLACE VIEW steam_item_sale.steam_most_recent_prices
AS SELECT t.name,
    t.quantity,
    t.price_euro,
    t."timestamp"
   FROM ( SELECT sip.name,
            sip.quantity,
            sip.price_euro,
            sip."timestamp",
            rank() OVER (PARTITION BY sip.name ORDER BY sip."timestamp" DESC) AS ranking
           FROM steam_item_sale.steam_item_prices sip) t
  WHERE t.ranking = 1 OR t.ranking IS NULL