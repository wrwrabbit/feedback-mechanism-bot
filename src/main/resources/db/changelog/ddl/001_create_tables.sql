create table users(
    id bigint primary key,
    lang_code text,
    vote_count bigint,
    poll_count bigint
);

create table polls(
    id bigserial primary key,
    user_id bigint,
    status text,
    question text,
    options text[],
    allow_multiple_answers boolean,
    created_at timestamp,
    started_at timestamp null,
    finished_at timestamp null,
    moderator_approves bigint[] default array[]::bigint[],
    user_approves bigint default 0,
    rejection_reason text null,
    message_id bigint
);

create table poll_user_review(
    user_id bigint,
    poll_id bigint
);

create table poll_user_vote(
    user_id bigint,
    poll_id bigint
);

create table poll_vote(
    poll_id bigint,
    option_1 bigint default 0,
    option_2 bigint default 0,
    option_3 bigint default 0,
    option_4 bigint default 0,
    option_5 bigint default 0,
    option_6 bigint default 0,
    option_7 bigint default 0,
    option_8 bigint default 0,
    option_9 bigint default 0,
    option_10 bigint default 0
);
