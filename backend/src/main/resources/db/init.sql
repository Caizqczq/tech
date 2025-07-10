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

-- 教学素材表
drop table if exists teaching_materials;
create table teaching_materials(
                                   id varchar(50) primary key comment '素材ID',
                                   original_name varchar(255) not null comment '原始文件名',
                                   stored_filename varchar(255) not null comment '存储文件名',
                                   oss_key varchar(500) not null comment 'OSS对象key',
                                   content_type varchar(100) not null comment '文件MIME类型',
                                   file_size bigint not null comment '文件大小(字节)',
                                   material_type enum('document', 'audio') not null comment '素材类型',
                                   
                                   -- 教学相关元数据
                                   title varchar(200) comment '素材标题',
                                   description text comment '素材描述',
                                   subject varchar(100) not null comment '学科分类',
                                   course_level enum('undergraduate', 'graduate', 'doctoral') not null comment '课程层次',
                                   document_type enum('lesson_plan', 'syllabus', 'paper', 'textbook', 'exercise') comment '文档类型',
                                   keywords varchar(500) comment '关键词(逗号分隔)',
                                   
                                   -- 音频特有字段
                                   duration int comment '音频时长(秒)',
                                   language varchar(10) comment '语言(zh/en)',
                                   audio_type enum('lecture', 'seminar', 'discussion', 'interview') comment '音频类型',
                                   speaker varchar(100) comment '主讲人',
                                   transcription_text longtext comment '转录文本',
                                   
                                   -- 所有者
                                   user_id int not null comment '上传者ID',
                                   
                                   created_at timestamp default current_timestamp comment '创建时间',
                                   updated_at timestamp default current_timestamp on update current_timestamp comment '更新时间',
                                   
                                   index idx_user_id (user_id),
                                   index idx_subject (subject),
                                   index idx_material_type (material_type)
) default charset = utf8mb4 comment = "教学素材表";

-- 音频转录任务表
drop table if exists transcription_tasks;
create table transcription_tasks(
                                    task_id varchar(50) primary key comment '任务ID',
                                    material_id varchar(50) not null comment '关联素材ID',
                                    transcription_mode enum('sync', 'async') not null comment '转录模式',
                                    status enum('processing', 'completed', 'failed') default 'processing' comment '任务状态',
                                    progress int default 0 comment '处理进度(0-100)',
                                    estimated_time int comment '预估处理时间(秒)',
                                    error_message text comment '错误信息',
                                    started_at timestamp default current_timestamp comment '开始时间',
                                    completed_at timestamp comment '完成时间',
                                    
                                    index idx_material_id (material_id),
                                    index idx_status (status)
) default charset = utf8mb4 comment = "音频转录任务表";

-- 对话会话表
drop table if exists conversations;
create table conversations(
                              id varchar(50) primary key comment '对话ID',
                              user_id int not null comment '用户ID',
                              title varchar(200) not null comment '对话标题',
                              scenario enum('teaching_advice', 'content_analysis', 'writing_assistance', 'general_chat') not null comment '对话场景',
                              context_info json comment '上下文信息(学科、课程层次等)',
                              total_messages int default 0 comment '消息总数',
                              created_at timestamp default current_timestamp comment '创建时间',
                              updated_at timestamp default current_timestamp on update current_timestamp comment '更新时间',
                              
                              index idx_user_id (user_id),
                              index idx_scenario (scenario),
                              index idx_created_at (created_at)
) default charset = utf8mb4 comment = "对话会话表";

-- 对话消息表
drop table if exists chat_messages;
create table chat_messages(
                              id varchar(50) primary key comment '消息ID',
                              conversation_id varchar(50) not null comment '对话ID',
                              message_type enum('user', 'assistant', 'system') not null comment '消息类型',
                              content longtext not null comment '消息内容',
                              metadata json comment '消息元数据(tokens、模型参数等)',
                              created_at timestamp default current_timestamp comment '创建时间',
                              
                              index idx_conversation_id (conversation_id),
                              index idx_created_at (created_at)
) default charset = utf8mb4 comment = "对话消息表";
