CREATE TABLE if not exists steam_item_sale.skinbaron_transactions (
	"name" varchar(100) NULL DEFAULT NULL::character varying,
	saleid varchar(100) NULL DEFAULT NULL::character varying,
	"timestamp" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	success bool NULL,
	price float8 NULL
);