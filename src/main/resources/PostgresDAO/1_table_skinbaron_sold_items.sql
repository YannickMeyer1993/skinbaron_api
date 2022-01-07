CREATE TABLE if not exists steam.skinbaron_sold_items (
  id varchar(100) NOT NULL,
  name varchar(100) DEFAULT NULL,
  price double precision DEFAULT NULL,
  classid varchar(500),
  last_updated varchar(500),
  instanceid varchar(500),
  list_time varchar(500),
  assetid varchar(500),
  txid varchar(500),
  commission double precision DEFAULT NULL,
  PRIMARY KEY (id)
) ;