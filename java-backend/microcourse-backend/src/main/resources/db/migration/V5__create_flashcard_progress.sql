create table if not exists flashcard_progress (
    id bigint primary key auto_increment,
    user_id bigint not null,
    card_index int not null,
    mastered_at timestamp not null default current_timestamp,
    unique key uk_flashcard_progress_user_card(user_id, card_index),
    constraint fk_flashcard_progress_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;
