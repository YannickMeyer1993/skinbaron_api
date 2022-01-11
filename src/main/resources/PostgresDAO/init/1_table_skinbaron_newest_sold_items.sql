create table if not exists steam.skinbaron_newest_sold_items (
name varchar(100) NOT NULL,
price decimal(12,2) NOT NULL,
wear varchar(50) DEFAULT NULL,
datesold varchar(50) DEFAULT NULL
);