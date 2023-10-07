create table users(
    id bigint primary key,
    lang_code text not null,
    status text not null,
    vote_count bigint not null,
    poll_count bigint not null
);

create table user_deletions(
    user_id bigint not null,
    created_at timestamp not null
);

create table polls(
    id bigserial primary key,
    user_id bigint,
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

create table message_queue(
    user_id bigint not null,
    poll_id bigint not null,
    type text not null
);

create table poll_user_review(
    poll_id bigint not null,
    user_id bigint not null,
    approved boolean not null,
    primary key (poll_id, user_id)
);

create table poll_user_vote(
    poll_id bigint not null,
    user_id bigint not null,
    options bigint[] default array[]::bigint[] not null,
    primary key (poll_id, user_id)
);
