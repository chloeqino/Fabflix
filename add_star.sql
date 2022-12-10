use moviedb;
drop procedure if exists add_stars;
-- Change DELIMITER to $$
DELIMITER $$

CREATE PROCEDURE add_stars ( in movieId varchar(10),  in star varchar(32))
    addstar:BEGIN

set @starId = -1;

select distinct id into @starId from stars where name=star limit 1 ;
select @starId;
if (@starId=-1) then
select max(cast(ifnull(substr(id,5),0) as unsigned)) into @starId from stars where id like 'star%';
set @starId = concat('star',ifnull(@starId,0)+1);

insert into stars (id,name) values(@starId,star);

end if;
insert into stars_in_movies(starId,movieId) values(@starId,movieId);



END;