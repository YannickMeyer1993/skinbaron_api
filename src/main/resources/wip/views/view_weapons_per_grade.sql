-- steam_item_sale.weapons_per_grade source

CREATE OR REPLACE VIEW steam_item_sale.weapons_per_grade
AS SELECT s.collection,
    s.quality,
    count(*) AS count
   FROM ( SELECT ii.collection,
            ii.quality
           FROM steam_item_sale.item_informations ii
          WHERE ii.collection::text <> ''::text AND ii.name::text !~~ '%StatTrak%'::text AND ii.name::text !~~ '%Souvenir%'::text
          GROUP BY ii.collection, ii.quality, ii.weapon) s
  GROUP BY s.collection, s.quality;