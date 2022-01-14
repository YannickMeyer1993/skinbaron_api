CREATE TABLE if not exists steam.buff_prices (
	price_euro float8 NULL,
	id int4 NOT NULL,
	"timestamp" timestamp default now(),
	has_enterior bool NULL,
	"name" varchar(999) NULL,
	weapon_name varchar(999) NULL
);