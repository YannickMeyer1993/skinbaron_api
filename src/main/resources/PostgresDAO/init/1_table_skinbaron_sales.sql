CREATE TABLE if not exists steam.skinbaron_sales (
	id varchar(200) NOT NULL,
	name varchar(200) NULL,
	classid varchar(200) NULL,
	last_updated varchar(200) NULL,
	list_time varchar(200) NULL,
	price float8 NOT NULL,
	assetid varchar(200) NULL,
	contextid varchar(200) NULL,
	insert_timestamp timestamp NULL DEFAULT now()
);