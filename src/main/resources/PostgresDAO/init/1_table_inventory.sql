CREATE TABLE if not exists steam.inventory (
	inv_type varchar(30) NOT NULL,
	"name" varchar(200) NOT NULL,
	amount int4,
	still_there bool NOT NULL,
	"date" date NOT NULL DEFAULT now()
);