CREATE TABLE if not exists steam.buff_prices (
	id int4 NOT NULL,
	price_euro double precision NOT NULL,
	insert_timestamp timestamp default now()
);