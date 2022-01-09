CREATE OR REPLACE VIEW steam.overview_total
AS SELECT overview.steam_inv_value,
       overview.steam_balance,
       overview.steam_open_sales,
       overview.skinbaron_inv_value,
       overview.smurf_inv_value,
       overview.skinbaron_balance,
       overview.skinbaron_open_sales,
       overview."DATE",
       overview.rare_items_value,
       overview.rare_items_value + round((0.9::double precision * (overview.steam_inv_value + overview.smurf_inv_value + overview.steam_balance + overview.steam_open_sales + 0.875::double precision * overview.skinbaron_inv_value) + overview.skinbaron_balance + round(0.85 * overview.skinbaron_open_sales::numeric, 2)::double precision)::numeric, 2)::double precision AS total_with_fees,
       overview.kommentar
     FROM steam.overview
     ORDER BY overview."DATE" desc;