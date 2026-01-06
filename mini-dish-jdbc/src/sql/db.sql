CREATE DATABASE mini_dish_db ;

CREATE USER mini_dish_db_manager with password '1234';

GRANT CONNECT on database mini_dish_db to mini_dish_db_manager ;

\c mini_dish_db;

grant create on schema public to mini_dish_db_manager;

alter default privileges in schema public
grant select , insert ,update ,delete on tables to mini_dish_db_manager;

