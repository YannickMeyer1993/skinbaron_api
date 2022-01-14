CREATE TABLE steam.collections (
	collection varchar NOT NULL,
	is_cool boolean default false
);

truncate table steam.collections;

INSERT INTO steam_item_sale.meta_table_good_collections (collection,is_cool) VALUES
	 ('The Office Collection',true),
	 ('The Alpha Collection',true),
	 ('The Militia Collection',true),
	 ('The eSports 2014 Summer Collection',true),
	 ('The Cache Collection',true),
	 ('The Gods and Monsters Collection',true),
	 ('The St. Marc Collection',true),
	 ('The Havoc Collection',true),
	 ('The Norse Collection',true),
	 ('The Ancient Collection',true),
	 ('The Aztec Collection',true),
	 ('The Chop Shop Collection',true),
	 ('The Baggage Collection',true),
	 ('The Assault Collection',true),
	 ('The Canals Collection',true),
	 ('The Cobblestone Collection',true),
	 ('The Control Collection',true),
	 ('The Rising Sun Collection',true);