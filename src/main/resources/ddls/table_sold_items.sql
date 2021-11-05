CREATE TABLE if not exists steam_item_sale.sold_items (
  sale_id varchar(100) NOT NULL,
  name varchar(100) DEFAULT NULL,
  price double precision DEFAULT NULL,
  PRIMARY KEY (sale_id)
) ;