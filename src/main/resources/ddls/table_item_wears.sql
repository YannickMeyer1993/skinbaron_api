-- steam_item_sale.item_wears definition

-- Drop table

-- DROP TABLE steam_item_sale.item_wears;

CREATE TABLE steam_item_sale.item_wears (
	"name" varchar(999) NOT NULL,
	min_wear numeric(12,2) NULL,
	max_wear numeric(12,2) NULL
);