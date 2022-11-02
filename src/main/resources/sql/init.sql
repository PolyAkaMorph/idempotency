create schema idempotency;

create table idempotency.cache
(
    id                  varchar(250) primary key not null,
    idempotency_key     varchar(250),
    request_path        varchar(250),
    request_method      varchar(250),
    request_fingerprint varchar(250),
    response_code       varchar(250),
    response_data       text,
    is_processing       boolean,
    created             bigint
);

