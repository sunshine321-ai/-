alter table learning_progress
    add column detail_json json null after completed,
    add column duration_seconds bigint not null default 0 after detail_json;
