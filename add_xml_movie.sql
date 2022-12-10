use moviedb;
drop procedure if exists add_xml_movie;
-- Change DELIMITER to $$ 
DELIMITER $$

CREATE PROCEDURE add_xml_movie (in id varchar(100), in title varchar(100),  in year int, in director varchar(100), in price decimal)
    add_xml_movie:BEGIN



IF EXISTS(SELECT * FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director) then

	leave add_xml_movie;

end if;

insert into movies values(id,title,year,director, price);
leave add_xml_movie;


END;



