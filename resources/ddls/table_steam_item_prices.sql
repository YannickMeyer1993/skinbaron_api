CREATE TABLE steam_item_sale."steam_item_prices" (
  "name" varchar(200) NOT NULL,
  "quantity" int NOT NULL,
  "price_euro" double precision NOT NULL,
  "timestamp" timestamp NOT NULL DEFAULT now(),
  "date" date NOT NULL DEFAULT now()
) ;
