CREATE TABLE if not exists steam.buff_item_informations (
	id int4 NOT NULL,
	has_exterior bool NULL,
	"name" varchar(999) NULL,
	CONSTRAINT buff_item_informations_pk PRIMARY KEY (id)
);