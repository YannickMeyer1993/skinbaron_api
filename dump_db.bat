set /p postgrespw=<C:\passwords\postgres.txt
pg_dump --dbname=postgres://postgres:%postgrespw%@127.0.0.1:5432/postgres -a -n steam_item_sale > C:/backup/db.sql