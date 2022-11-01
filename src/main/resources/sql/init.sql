create schema idempotency;

create table idempotency.cache
(
    id              varchar(250) primary key not null,
    idempotency_key varchar(250),
    external_id     varchar(250),
    data            text,
    fingerprint     varchar(250),
    path            varchar(250),
    method          varchar(250),
    is_processing   boolean
);

