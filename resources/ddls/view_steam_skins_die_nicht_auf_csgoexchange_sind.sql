-- steam_item_sale.steam_skins_die_nicht_auf_csgoexchange_sind source

CREATE OR REPLACE VIEW steam_item_sale.steam_skins_die_nicht_auf_csgoexchange_sind
AS SELECT DISTINCT sip.name
   FROM steam_item_sale.steam_item_prices sip
  WHERE NOT (sip.name::text IN ( SELECT item_informations.name
           FROM steam_item_sale.item_informations));