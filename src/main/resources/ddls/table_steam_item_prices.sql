CREATE TABLE if not exists steam_item_sale."steam_item_prices" (
  "name" varchar(200) NOT NULL,
  "quantity" int NOT NULL,
  "price_euro" double precision NOT NULL,
  "timestamp" timestamp NOT NULL DEFAULT now(),
  "date" date NOT NULL DEFAULT now()
) ;

create index steam_name on steam_item_sale.steam_item_prices (name);
create index steam_price on steam_item_sale.steam_item_prices (price_euro);
create index steam_date on steam_item_sale.steam_item_prices (date);
create index day_steam_item_prices on steam_item_prices (DATE(timestamp));