create table users(
    id bigint
);

create table users_captcha(
    user_id bigint,
    captcha varchar(16)
);