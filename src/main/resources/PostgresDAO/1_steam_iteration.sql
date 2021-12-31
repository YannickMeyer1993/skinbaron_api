CREATE TABLE if not exists steam.steam_iteration (
	"date" date NOT NULL DEFAULT now(),
	iteration int4 NOT NULL DEFAULT 0
);