CREATE TABLE if not exists steam_item_sale.buff_item_prices (
	price_euro float8 NULL,
	id int4 NOT NULL,
	"timestamp" timestamp NULL,
	has_enterior bool NULL,
	"name" varchar(999) NULL,
	weapon_name varchar(999) NULL,
	CONSTRAINT buff_item_prices_pk PRIMARY KEY (id)
);