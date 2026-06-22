create table if not exists module_usage_events (
    id bigint primary key auto_increment,
    user_id bigint not null,
    module_key varchar(50) not null,
    event_type varchar(20) not null,
    duration_seconds int not null default 0,
    created_at timestamp not null default current_timestamp,
    index idx_usage_user_module(user_id, module_key),
    index idx_usage_user_created(user_id, created_at),
    constraint fk_module_usage_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;
