-- steam_item_sale.item_informations definition

-- Drop table

-- DROP TABLE steam_item_sale.item_informations;

CREATE TABLE steam_item_sale.item_informations (
	"name" varchar(200) NOT NULL,
	weapon varchar(100) NOT NULL,
	collection varchar(100) NOT NULL,
	quality varchar(100) NOT NULL,
	CONSTRAINT item_informations_pkey PRIMARY KEY (name)
);