CREATE TABLE if not exists steam.skinbaron_inventory (
	id int4 not null,
	"name" varchar(200) NOT NULL,
	tradeLockHourseLeft int4
);