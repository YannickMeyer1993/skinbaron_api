create table if not exists steam.skinbaron_newest_sold_items_tmp (
name varchar(100) NOT NULL,
price decimal(12,2) NOT NULL,
wear varchar(50) DEFAULT NULL,
datesold varchar(50) DEFAULT NULL,
doppler_phase varchar(50) default null
);

CREATE TABLE if not exists steam.skinbaron_newest_sold_items (
	"name" varchar(100) NULL,
	doppler_phase varchar(50) NULL,
	avg_price numeric NULL,
	min_price numeric NULL,
	max_price numeric NULL,
	amount int8 NULL,
	insert_date date NULL,
	insert_timestamp timestamp NOT NULL DEFAULT now()
);