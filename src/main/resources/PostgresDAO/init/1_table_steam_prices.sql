CREATE TABLE if not exists steam.steam_prices (
  "name" varchar(200) NOT NULL,
  "quantity" int NOT NULL,
  "price_euro" double precision NOT NULL,
  "date" date NOT NULL DEFAULT now(),
  start_index int
) ;

create index IF NOT EXISTS steam_name on steam.steam_prices (name);
create index IF NOT EXISTS steam_price on steam.steam_prices (price_euro);
create index IF NOT EXISTS steam_date on steam.steam_prices ("date");