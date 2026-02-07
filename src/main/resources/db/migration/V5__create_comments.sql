create table if not exists comments (
    id bigint auto_increment primary key,
    post_id bigint not null,
    content varchar(1000) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_comments_post
        foreign key (post_id) references posts(id)
        on delete cascade
);

create index if not exists idx_comments_post_id_created_at
       on comments(post_id, created_at desc);