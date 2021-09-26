CREATE OR REPLACE VIEW steam_item_sale.auf_buff_kaufbare_skins_investment_skins
AS SELECT buff.id,
    buff.name,
    buff.price_euro AS buff_price,
    smrp.price_euro AS steam_price,
    smrp.quantity,
    smrp."timestamp" AS steam_timestamp,
    (smrp.price_euro - 0.04::double precision) / buff.price_euro::double precision AS ratio,
    s.dsratio
   FROM steam_item_sale.buff_item_prices buff
     JOIN steam_item_sale.steam_most_recent_prices smrp ON buff.name::text = smrp.name::text AND smrp.price_euro <> 0::double precision AND buff.name::text !~~ 'Souvenir%'::text AND buff.name::text !~~ 'Sealed Graffiti%'::text AND smrp.quantity > 25
     JOIN steam_item_sale.durchschnittspreise_steam s ON buff.name::text = s.name::text AND s.dsratio < 1.8::double precision
     JOIN steam_item_sale.item_informations ii ON ii.name::text = buff.name::text
     JOIN steam_item_sale.investment_skin_names isn ON isn.name::text = buff.name::text
  WHERE buff.price_euro != 0 and ((smrp.price_euro - 0.04::double precision) / buff.price_euro::double precision) > 1.5::double precision
  GROUP BY buff.id, buff.name, buff.price_euro, smrp.price_euro, smrp.quantity, smrp."timestamp", ((smrp.price_euro - 0.04::double precision) / buff.price_euro::double precision), s.dsratio
  ORDER BY ((smrp.price_euro - 0.04::double precision) / buff.price_euro::double precision) DESC;