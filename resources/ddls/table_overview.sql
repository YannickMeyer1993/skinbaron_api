-- steam_item_sale.overview definition

-- Drop table

-- DROP TABLE steam_item_sale.overview;

CREATE TABLE steam_item_sale.overview (
	"DATE" date NOT NULL DEFAULT now(),
	highest_iteration_steam int4 NOT NULL DEFAULT 0,
	steam_balance float8 NOT NULL,
	steam_open_sales float8 NOT NULL,
	skinbaron_balance float8 NOT NULL,
	smurf_inv_value float8 NOT NULL,
	skinbaron_open_sales_wert float8 NOT NULL,
	steam_inv_value float8 NOT NULL,
	skinbaron_inv_value float8 NOT NULL,
	kommentar varchar(200) NULL DEFAULT NULL::character varying,
	summe_rare_items float8 NULL DEFAULT 0,
	CONSTRAINT overview_pkey PRIMARY KEY ("DATE")
);