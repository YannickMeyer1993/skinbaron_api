-- steam_item_sale.meta_table_percentages definition

-- Drop table

-- DROP TABLE steam_item_sale.meta_table_percentages;

CREATE TABLE steam_item_sale.meta_table_percentages (
	"type" varchar NOT NULL,
	percentage int4 NOT NULL,
	"real" int4 NULL,
	CONSTRAINT meta_table_percentages_pk PRIMARY KEY (type)
);