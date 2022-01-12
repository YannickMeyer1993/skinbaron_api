CREATE TABLE if not exists steam.skinbaron_items (
  "id" varchar(100) NOT NULL,
  "name" varchar(100) NOT NULL,
  "price" decimal(12,2) NOT NULL,
  "stickers" varchar(300) DEFAULT NULL,
  "wear" varchar(50) DEFAULT NULL,
  "timestamp" timestamp NOT NULL DEFAULT now(),
  "inspect" varchar(300) DEFAULT NULL,
  "sbinspect" varchar(300) DEFAULT NULL,
  PRIMARY KEY ("id")
) ;

create index IF NOT EXISTS smsr_name on steam.skinbaron_items (name);
create index IF NOT EXISTS smsr_price on steam.skinbaron_items (price);