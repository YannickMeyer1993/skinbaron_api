-- steam_item_sale.inventory definition

-- Drop table

-- DROP TABLE steam_item_sale.inventory;

CREATE TABLE steam_item_sale.inventory (
	inv_type varchar(30) NOT NULL,
	"name" varchar(200) NOT NULL,
	still_there bool NOT NULL,
	amount int4 NULL,
	"date" date NOT NULL DEFAULT now()
);