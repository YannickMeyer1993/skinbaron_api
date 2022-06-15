CREATE TABLE if not exists steam.buff_prices (
	price_euro float8 NULL,
	id int4 NOT NULL,
	insert_timestamp timestamp NOT NULL DEFAULT now(),
	has_exterior bool NULL,
	"name" varchar(999) NULL,
	CONSTRAINT buff_prices_pk PRIMARY KEY (id, insert_timestamp)
);