create index smsr_name on steam_item_sale.skinbaron_market_search_results (name);
create index smsr_price on steam_item_sale.skinbaron_market_search_results (price);
create index smsr_id on steam_item_sale.skinbaron_market_search_results (id);
create index steam_name on steam_item_sale.steam_item_prices (name);
create index steam_price on steam_item_sale.steam_item_prices (price_euro);
create index steam_date on steam_item_sale.steam_item_prices (date);
create index day_steam_item_prices on steam_item_prices (DATE(timestamp));
