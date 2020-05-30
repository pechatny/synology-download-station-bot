create schema public;

comment on schema public is 'standard public schema';

alter schema public owner to ayrejqkmawmnoy;

create table users
(
  id serial not null,
  user_id integer not null,
  username varchar,
  surname varchar,
  authorized boolean default false not null,
  chat_id integer
);

alter table users owner to ayrejqkmawmnoy;

create unique index users_user_id_uindex
  on users (user_id);

create table messages
(
  id serial not null,
  message_id integer not null,
  message_type varchar,
  content varchar,
  created_at timestamp,
  chat_id integer
);

alter table messages owner to ayrejqkmawmnoy;

create table chats
(
  id serial not null,
  chat_id integer not null,
  stage varchar
);

alter table chats owner to ayrejqkmawmnoy;

create unique index chats_chat_id_uindex
  on chats (chat_id);

