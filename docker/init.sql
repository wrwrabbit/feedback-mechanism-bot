create table users(
    id bigint primary key
);

create table polls(
    id bigserial primary key,
    user_id bigint,
    question text,
    options text[],
    allow_multiple_answers boolean,
    created_at timestamp
);

create table polls_moderation(
    poll_id bigint primary key,
    telegram_id bigint,
    approves bigint[],
    rejection_reason text null
);
