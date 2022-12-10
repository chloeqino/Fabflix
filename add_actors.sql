use moviedb;
drop procedure if exists add_actors;
-- Change DELIMITER to $$
DELIMITER $$

CREATE PROCEDURE add_actors ( in star varchar(32),  in bYear int)
    addstar:BEGIN

set @starId = -1;
set @birth = null;

select distinct id, birthYear into @starId, @year from stars where name=star limit 1;
select @starId;
if (@starId=-1) then
select max(cast(ifnull(substr(id,5),0) as unsigned)) into @starId from stars where id like 'star%';
set @starId = concat('star',ifnull(@starId,0)+1);
    if (bYear!=-1) then
        set @birth = bYear;
    end if;

insert into stars (id,name,birthYear) values(@starId,star, @birth);
elseif (@starId!=-1 and @year=null) then
update stars set birthYear = @year where id = @starId;
end if;

END;
