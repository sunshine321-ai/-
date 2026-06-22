create table if not exists users (
    id bigint primary key auto_increment,
    username varchar(50) not null unique,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp
) engine=InnoDB default charset=utf8mb4;

insert into users(id, username)
values (1, 'default')
on duplicate key update username = values(username);

create table if not exists study_notes (
    id bigint primary key auto_increment,
    user_id bigint not null unique,
    content longtext not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    constraint fk_study_notes_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;

create table if not exists wrong_questions (
    id bigint primary key auto_increment,
    user_id bigint not null,
    question text not null,
    correct_answer text,
    user_answer text,
    note text,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    index idx_wrong_questions_user(user_id),
    constraint fk_wrong_questions_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;

create table if not exists screenshot_notes (
    id bigint primary key auto_increment,
    user_id bigint not null,
    image_url varchar(500) not null,
    video_time decimal(10, 3) not null default 0,
    note text,
    ai_analysis text,
    created_at timestamp not null default current_timestamp,
    index idx_screenshot_notes_user(user_id),
    constraint fk_screenshot_notes_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;

create table if not exists chat_messages (
    id bigint primary key auto_increment,
    user_id bigint not null,
    context varchar(20) not null,
    role varchar(20) not null,
    content text not null,
    created_at timestamp not null default current_timestamp,
    index idx_chat_user_context(user_id, context),
    constraint fk_chat_messages_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;

create table if not exists learning_progress (
    id bigint primary key auto_increment,
    user_id bigint not null,
    chapter_key varchar(100) not null,
    progress decimal(5, 2) not null default 0,
    completed boolean not null default false,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    unique key uk_progress_user_chapter(user_id, chapter_key),
    constraint fk_learning_progress_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;

create table if not exists bookmarks (
    id bigint primary key auto_increment,
    user_id bigint not null,
    video_time decimal(10, 3) not null,
    label varchar(200) not null,
    created_at timestamp not null default current_timestamp,
    index idx_bookmarks_user(user_id),
    constraint fk_bookmarks_user foreign key (user_id) references users(id) on delete cascade
) engine=InnoDB default charset=utf8mb4;
