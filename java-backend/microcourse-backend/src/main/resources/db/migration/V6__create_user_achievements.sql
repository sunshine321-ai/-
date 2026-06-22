create table if not exists user_achievements (
    id bigint primary key auto_increment,
    user_id bigint not null,
    achievement_key varchar(50) not null,
    unlocked_at timestamp not null default current_timestamp,
    unique key uk_user_achievement(user_id, achievement_key),
    index idx_achievement_user_time(user_id, unlocked_at),
    constraint fk_achievement_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;
