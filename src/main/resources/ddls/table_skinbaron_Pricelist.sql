create table steam_item_sale.skinbaron_pricelist (
name varchar(100) not null,
exterior varchar(100) not null,
is_statTrak boolean,
is_souvenir boolean,
lowestPrice decimal(12,2),
marketHashName varchar(100) not null,
minWear decimal(12,10),
maxWear decimal(12,10),
imageUrl varchar(1000),
"timestamp" timestamp not null default now(),
primary key (marketHashName));