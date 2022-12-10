use moviedb;
drop procedure if exists add_genres;
-- Change DELIMITER to $$ 
DELIMITER $$

CREATE PROCEDURE add_genres ( in movieId varchar(10),  in genre varchar(32))
    addgenre:BEGIN





set @genreId = -1;

select id into @genreId from genres where name = genre;
if (@genreId=-1) then
	insert into genres values(null, genre);
select last_insert_id() into @genreId;
end if;

insert into genres_in_movies(genreId,movieId); values(@genreId,movieId);



END;



