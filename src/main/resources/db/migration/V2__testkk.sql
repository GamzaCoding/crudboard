create table if not exists posts (
    id bigint auto_increment primary key,
    title varchar(255) not null,
    content text not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    sometext text not null
    );