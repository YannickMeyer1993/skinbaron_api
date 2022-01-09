-- steam_item_sale.steam_most_recent_prices source

CREATE OR REPLACE VIEW steam.steam_current_prices
AS SELECT distinct t.name,
       t.quantity,
       t.price_euro,
       t."date"
      FROM ( SELECT distinct sip.name,
               sip.quantity,
               sip.price_euro,
               sip."date",
               rank() OVER (PARTITION BY sip.name ORDER BY sip."date" desc,sip.price_euro,quantity) AS ranking
              FROM steam.steam_prices sip) t
     WHERE t.ranking = 1 OR t.ranking IS NULL