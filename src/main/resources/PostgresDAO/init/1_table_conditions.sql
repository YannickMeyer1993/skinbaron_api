CREATE TABLE if not exists steam.conditions (
	"condition" varchar NULL,
	min_wear float8 NULL,
	max_wear float8 NULL
);

truncate table steam.conditions;

INSERT INTO steam.conditions ("condition",min_wear,max_wear) VALUES
	 ('Factory New',0.0,0.07),
	 ('Minimal Wear',0.07,0.15),
	 ('Field-Tested',0.15,0.38),
	 ('Well-Worn',0.38,0.45),
	 ('Battle-Scarred',0.45,1.0);