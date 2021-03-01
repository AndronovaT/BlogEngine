create table captcha_codes (
    id integer not null,
    code TEXT not null,
    secret_code TEXT not null,
    time datetime(6) not null,
    primary key (id));

create table global_settings (
    id integer not null,
    code varchar(255) not null,
    name varchar(255) not null,
    value varchar(255) not null,
    primary key (id));

create table hibernate_sequence (next_val bigint);
insert into hibernate_sequence values ( 1 );
insert into hibernate_sequence values ( 1 );
insert into hibernate_sequence values ( 1 );
insert into hibernate_sequence values ( 1 );
insert into hibernate_sequence values ( 1 );
insert into hibernate_sequence values ( 1 );
insert into hibernate_sequence values ( 1 );
insert into hibernate_sequence values ( 1 );

create table post_comments (
    id integer not null,
    text TEXT not null,
    time datetime(6) not null,
    parent_id integer,
    post_id integer not null,
    user_id integer not null,
    primary key (id));

create table post_votes (
    id integer not null,
    time datetime(6) not null,
    value tinyint not null,
    post_id integer not null,
    user_id integer not null,
    primary key (id));

create table posts (
    id integer not null,
    is_active tinyint not null,
    moderation_status enum('NEW','ACCEPTED','DECLINED') not null,
    text TEXT not null,
    time datetime(6) not null,
    title varchar(255) not null,
    view_count integer not null,
    moderator_id integer,
    user_id integer not null,
    primary key (id));

create table tag2post (
    id integer not null,
    post_id integer not null,
    tag_id integer not null,
    primary key (id));

create table tags (
    id integer not null,
    name varchar(255) not null,
    primary key (id));

create table users (
    id integer not null,
    code varchar(255),
    email varchar(255) not null,
    is_moderator tinyint not null,
    name varchar(255) not null,
    password varchar(255) not null,
    photo TEXT,
    reg_time datetime(6) not null,
    primary key (id));
