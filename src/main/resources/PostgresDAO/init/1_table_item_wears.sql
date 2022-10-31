CREATE TABLE if not exists steam.item_wears (
	"name" varchar(999) NOT NULL,
	id varchar(100) NOT NULL,
	min_wear numeric(12,2) NULL,
	max_wear numeric(12,2) NULL
);