drop table if exists user;
create table user(
                      id int auto_increment comment '用户id',
                      username varchar(50) not null comment '用户名',
                      email varchar(100) not null comment '邮箱',
                      password_hash varchar(255) not null comment '密码哈希',
                      avatar varchar(500) comment '头像url',
                      created_at timestamp default current_timestamp comment '创建时间',
                      updated_at timestamp default current_timestamp on update current_timestamp comment '更新时间',
                      primary key (id),
                      index idx_username (`username`),
                      index idx_email (email)
) default charset = utf8mb4 comment = "用户表";
