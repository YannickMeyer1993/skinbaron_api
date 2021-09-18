-- steam_item_sale.cases definition

-- Drop table

-- DROP TABLE steam_item_sale.cases;

CREATE TABLE steam_item_sale.cases (
	id int4 NULL,
	still_get_dropped bool NULL,
	"name" varchar(30) NULL DEFAULT NULL::character varying,
	release_date timestamp NULL,
	day_of_week varchar(15) NULL DEFAULT NULL::character varying,
	wait_time int4 NULL
);