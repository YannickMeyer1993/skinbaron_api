CREATE TABLE if not exists steam_item_sale.item_informations (
	"name" varchar(200) NOT NULL,
	weapon varchar(100) NOT NULL,
	collection varchar(100) NOT NULL,
	quality varchar(100) NOT NULL,
	name_without_exterior varchar(200) NULL,
	CONSTRAINT item_informations_pkey PRIMARY KEY (name)
);