use moviedb;
drop procedure if exists add_stars;
-- Change DELIMITER to $$
DELIMITER $$

CREATE PROCEDURE add_stars ( in movieId varchar(10),  in star varchar(32))
    addstar:BEGIN

set @starId = -1;

select distinct id, birthYear into @starId, @year from stars where name=star;
select @starId;
if (@starId=-1) then
select max(cast(ifnull(substr(id,5),0) as unsigned)) into @starId from stars where id like 'star%';
s
set @starId = concat('star',ifnull(@starId,0)+1);
set @year = null;

insert into stars (id,name,birthYear) values(@starId,star, @year);

end if;
insert into stars_in_movies values(@starId,movieId);



END;