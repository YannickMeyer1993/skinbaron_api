set /p postgrespw=<C:\passwords\postgres.txt
"C:\Program Files\PostgreSQL\13\bin\pg_dump.exe" --dbname=postgres://postgres:%postgrespw%@127.0.0.1:5432/postgres -a -n steam > C:/backup/db.sql