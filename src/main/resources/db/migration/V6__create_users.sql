create table if not exists users (
    id bigint auto_increment primary key,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    role varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint nk_users_email unique (email)
);

create index if not exists idx_users_email on users(email);