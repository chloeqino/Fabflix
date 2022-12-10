use moviedb;
drop procedure if exists add_movie;
-- Change DELIMITER to $$ 
DELIMITER $$

CREATE PROCEDURE add_movie ( in title varchar(100),  in year int, in director varchar(100), in price decimal, in starName varchar(100), in genre varchar(32), out message varchar(100))
    addmovie:BEGIN



IF EXISTS(SELECT * FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director) then

	set message = 'error:movie exists';
    select message;
	leave addmovie;

end if;
select max(cast(ifnull(substr(id,4),0) as unsigned)) into @newid from movies where id like 'new%';
set @newid = concat('new',ifnull(@newid,0)+1);

insert into movies values(@newid,title,year,director,price);



set @newstar = starName;
set @starId = -1;
set @genreId = -1;

select id into @genreId from genres where name = genre;
if (@genreId=-1) then
	insert into genres values(null, genre);
select last_insert_id() into @genreId;
end if;

insert into genres_in_movies values(@genreId,@newId);
select distinct id into @starId from stars where name=starName;
if (@starId=-1) then 
select max(cast(ifnull(substr(id,5),0) as unsigned)) into @starId from stars where id like 'star%';
set @starId = concat('star',ifnull(@starId,0)+1);

insert into stars (id,name) values(@starId,starName);

end if;
insert into stars_in_movies values(@starId,@newId);

set message = CONCAT("movieID= ",@newId," genreID= ", @genreID, " starID= ",@starID);
select message;


END;



