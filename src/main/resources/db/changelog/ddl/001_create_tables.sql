create table users(
    id bigint primary key,
    lang_code text not null,
    vote_count bigint not null,
    poll_count bigint not null
);

create table polls(
    id bigserial primary key,
    user_id bigint not null,
    status text not null,
    question text not null,
    options text[] not null,
    allow_multiple_answers boolean not null,
    created_at timestamp not null,
    started_at timestamp null,
    finished_at timestamp null,
    moderator_approves bigint[] default array[]::bigint[] not null,
    rejection_reason text null,
    message_id bigint null
);

create table poll_user_review_queue(
    user_id bigint not null,
    poll_id bigint not null
);

create table poll_user_vote_queue(
    user_id bigint not null,
    poll_id bigint not null
);

create table poll_user_review(
    poll_id bigint not null,
    user_id bigint not null,
    approved boolean not null
);

create table poll_user_vote(
    poll_id bigint not null,
    user_id bigint not null,
    option_1 bigint default 0 not null,
    option_2 bigint default 0 not null,
    option_3 bigint default 0 not null,
    option_4 bigint default 0 not null,
    option_5 bigint default 0 not null,
    option_6 bigint default 0 not null,
    option_7 bigint default 0 not null,
    option_8 bigint default 0 not null,
    option_9 bigint default 0 not null,
    option_10 bigint default 0 not null
);
