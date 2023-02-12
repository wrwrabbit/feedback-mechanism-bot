create table users(
    id bigint primary key
);

create table polls(
    id bigserial primary key,
    user_id bigint,
    status text,
    question text,
    options text[],
    allow_multiple_answers boolean,
    created_at timestamp,
    approves bigint[] default array[]::bigint[],
    rejection_reason text null
);
