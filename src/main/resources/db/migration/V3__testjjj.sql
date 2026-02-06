alter table posts add column sometext text;
update posts set sometext = '' where sometext is null;
alter table posts alter column sometext set not null;