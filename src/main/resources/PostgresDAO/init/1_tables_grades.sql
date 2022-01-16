CREATE TABLE if not exists steam.grades (
	grade varchar NULL,
	id int4 NOT NULL
);

truncate table steam.grades;

INSERT INTO steam.grades (grade,id) VALUES
	 ('Consumer Grade',1),
	 ('Industrial Grade',2),
	 ('Mil-Spec Grade',3),
	 ('Restricted',4),
	 ('Classified',5),
	 ('Covert',6);