use moviedb;
drop table if exists employees;
create table employees (email varchar(50) primary key,
password varchar(20) not null,
fullname varchar(100));
insert into employees VALUES('classta@email.edu','classta','TA CS122B');