CREATE TABLE if not exists steam.overview (
	"DATE" date NOT NULL DEFAULT now(),
	steam_balance float8 NOT NULL,
	skinbaron_balance float8 NOT NULL,
	steam_open_sales float8 NOT NULL,
	skinbaron_open_sales float8 NOT NULL,
	steam_inv_value float8 NOT NULL,
	smurf_inv_value float8 NOT NULL,
	skinbaron_inv_value float8 NOT NULL,
	rare_items_value float8 NULL DEFAULT 0,
	kommentar varchar(200) NULL DEFAULT NULL::character varying,
	CONSTRAINT overview_pkey PRIMARY KEY ("DATE")
);