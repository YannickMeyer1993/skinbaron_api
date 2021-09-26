-- steam_item_sale.skinbaron_skins_die_nicht_auf_csgoexchange_sind source

CREATE OR REPLACE VIEW steam_item_sale.skinbaron_skins_die_nicht_auf_csgoexchange_sind
AS SELECT DISTINCT sip.name
   FROM steam_item_sale.skinbaron_market_search_results sip
  WHERE NOT (sip.name::text IN ( SELECT item_informations.name
           FROM steam_item_sale.item_informations));