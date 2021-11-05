CREATE TABLE if not exists steam_item_sale."skinbaron_market_search_results" (
  "id" varchar(100) NOT NULL,
  "name" varchar(100) NOT NULL,
  "price" decimal(12,2) NOT NULL,
  "stickers" varchar(300) DEFAULT NULL,
  "wear" varchar(20) DEFAULT NULL,
  "timestamp" timestamp NOT NULL DEFAULT now(),
  PRIMARY KEY ("id","price")
) ;

create index IF NOT EXISTS smsr_name on steam_item_sale.skinbaron_market_search_results (name);
create index IF NOT EXISTS smsr_price on steam_item_sale.skinbaron_market_search_results (price);
create index IF NOT EXISTS smsr_id on steam_item_sale.skinbaron_market_search_results (id);