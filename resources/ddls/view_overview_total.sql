-- steam_item_sale.overview_total source

CREATE OR REPLACE VIEW steam_item_sale.overview_total
AS SELECT overview.steam_inv_value,
    overview.steam_balance,
    overview.steam_open_sales,
    overview.skinbaron_inv_value,
    overview.smurf_inv_value,
    overview.skinbaron_balance,
    overview.skinbaron_open_sales_wert,
    overview."DATE",
    overview.summe_rare_items,
    overview.summe_rare_items + round((0.9::double precision * (overview.steam_inv_value + overview.smurf_inv_value + overview.steam_balance + overview.steam_open_sales + 0.875::double precision * overview.skinbaron_inv_value) + overview.skinbaron_balance + round(0.85 * overview.skinbaron_open_sales_wert::numeric, 2)::double precision)::numeric, 2)::double precision AS total_with_fees,
    overview.kommentar
   FROM steam_item_sale.overview
  ORDER BY overview."DATE";