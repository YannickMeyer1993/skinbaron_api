-- steam_item_sale.buff_item_prices definition

-- Drop table

-- DROP TABLE steam_item_sale.buff_item_prices;

CREATE TABLE steam_item_sale.buff_item_prices (
	price_euro float8 NULL,
	id int4 NOT NULL,
	"timestamp" timestamp NULL,
	success bool NULL,
	"name" varchar(999) NULL,
	weapon_name varchar(999) NULL,
	CONSTRAINT buff_item_prices_pk PRIMARY KEY (id)
);