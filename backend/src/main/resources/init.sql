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

drop table if exists teaching_resources;
create table teaching_resources(
                                   id varchar(50) primary key comment '资源ID',
                                   original_name varchar(255) not null comment '原始文件名',
                                   stored_filename varchar(255) not null comment '存储文件名',
                                   file_path varchar(500) not null comment '本地文件路径',
                                   content_type varchar(100) not null comment '文件MIME类型',
                                   file_size bigint not null comment '文件大小(字节)',
                                   resource_type enum('document', 'audio') not null comment '资源类型',
                                   
                                   -- 教学相关元数据
                                   title varchar(200) comment '资源标题',
                                   description text comment '资源描述',
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
                                   
                                   -- 新增字段以支持接口文档要求
                                   is_vectorized boolean default false comment '是否已向量化',
                                   processing_status varchar(50) default 'completed' comment '处理状态：processing/completed/failed',
                                   extracted_keywords text comment '自动提取的关键词',
                                   
                                   -- 所有者
                                   user_id int not null comment '上传者ID',
                                   
                                   created_at timestamp default current_timestamp comment '创建时间',
                                   updated_at timestamp default current_timestamp on update current_timestamp comment '更新时间',
                                   
                                   index idx_user_id (user_id),
                                   index idx_subject (subject),
                                   index idx_resource_type (resource_type),
                                   index idx_is_vectorized (is_vectorized),
                                   index idx_processing_status (processing_status)
) default charset = utf8mb4 comment = "教学资源表";

-- 音频转录任务表
drop table if exists transcription_tasks;
create table transcription_tasks(
                                    task_id varchar(50) primary key comment '任务ID',
                                    resource_id varchar(50) not null comment '关联资源ID',
                                    transcription_mode enum('sync', 'async', 'stream') not null comment '转录模式',
                                    status enum('processing', 'completed', 'failed') default 'processing' comment '任务状态',
                                    progress int default 0 comment '处理进度(0-100)',
                                    estimated_time int comment '预估处理时间(秒)',
                                    error_message text comment '错误信息',
                                    started_at timestamp default current_timestamp comment '开始时间',
                                    completed_at timestamp comment '完成时间',
                                    
                                    index idx_resource_id (resource_id),
                                    index idx_status (status)
) default charset = utf8mb4 comment = "音频转录任务表";

-- 对话会话表
drop table if exists conversations;
create table conversations(
                              id varchar(100) primary key comment '对话ID',
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

-- 对话消息存储说明：
-- Spring AI Alibaba的MysqlChatMemoryRepository会自动创建ai_chat_memory表来存储聊天记录
-- 该表结构由Spring AI框架管理，支持消息窗口、自动清理等高级功能
-- 表结构大致为：
-- CREATE TABLE ai_chat_memory (
--     conversation_id VARCHAR(255) NOT NULL,
--     message_content TEXT NOT NULL,
--     message_type VARCHAR(50) NOT NULL,  -- USER, ASSISTANT, SYSTEM
--     timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     metadata JSON,
--     INDEX idx_conversation_id (conversation_id),
--     INDEX idx_timestamp (timestamp)
-- );
--
-- 注意：此表由Spring AI自动创建和管理，无需手动建表

-- 知识库表（新增，支持模块5的知识库管理功能）
drop table if exists knowledge_base;
create table knowledge_base(
                               id varchar(50) primary key comment '知识库ID',
                               name varchar(200) not null comment '知识库名称',
                               description text comment '知识库描述',
                               subject varchar(100) not null comment '学科领域',
                               course_level enum('undergraduate', 'graduate', 'doctoral') not null comment '课程层次',
                               resource_ids text comment '资源ID列表(JSON格式)',
                               vector_store varchar(50) default 'redis' comment '向量存储类型',
                               chunk_size int default 1000 comment '分块大小',
                               chunk_overlap int default 200 comment '分块重叠',
                               status enum('processing', 'completed', 'failed') default 'processing' comment '知识库状态',
                               progress int default 0 comment '进度百分比',
                               resource_count int default 0 comment '资源数量',
                               chunk_count int default 0 comment '分块数量',
                               document_count int default 0 comment '文档数量（兼容字段）',
                               message text comment '状态消息',
                               user_id int not null comment '创建者ID',
                               created_at timestamp default current_timestamp comment '创建时间',
                               updated_at timestamp default current_timestamp on update current_timestamp comment '更新时间',
                               completed_at timestamp comment '完成时间',
                               last_used timestamp comment '最后使用时间',

                               index idx_user_id (user_id),
                               index idx_subject (subject),
                               index idx_status (status)
) default charset = utf8mb4 comment = "知识库表";

-- 知识库资源关联表
drop table if exists knowledge_base_resources;
create table knowledge_base_resources(
                                        id varchar(50) primary key comment '关联ID',
                                        knowledge_base_id varchar(50) not null comment '知识库ID',
                                        resource_id varchar(50) not null comment '资源ID',
                                        added_at timestamp default current_timestamp comment '添加时间',
                                        
                                        unique key uk_kb_resource (knowledge_base_id, resource_id),
                                        index idx_knowledge_base_id (knowledge_base_id),
                                        index idx_resource_id (resource_id)
) default charset = utf8mb4 comment = "知识库资源关联表";


