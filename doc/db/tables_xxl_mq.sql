#
# XXL-MQ
# Copyright (c) 2015-present, xuxueli.

CREATE database if NOT EXISTS `xxl_mq` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `xxl_mq`;

SET NAMES utf8mb4;


## —————————————————————— topic and message ——————————————————

CREATE TABLE `xxl_mq_topic`(
    `id`                    bigint(20)      NOT NULL AUTO_INCREMENT,
    `topic`                 varchar(100)    NOT NULL COMMENT '消息主题Topic',
    `appname`               varchar(50)     NOT NULL COMMENT 'AppName（服务唯一标识）',
    `desc`                  varchar(50)     NOT NULL COMMENT '消息主题描述',
    `owner`                 varchar(50)     NOT NULL COMMENT '负责人',
    `alarm_email`           varchar(200)    DEFAULT NULL COMMENT '告警配置（邮箱）',
    `status`                tinyint(4)      NOT NULL COMMENT '状态（正常；禁用）',
    `store_strategy`        tinyint(4)      NOT NULL COMMENT '存储策略（统一存储；隔离存储）',
    `archive_strategy`      tinyint(4)      NOT NULL COMMENT '归档策略（归档保留7天；归档保留30天；归档保留90天；归档永久保留；不归档直接清理；）',
    `partition_strategy`    tinyint(4)      NOT NULL COMMENT '分区策略（Hash分区路由；随机分区路由；轮询分区路由）',
    `retry_strategy`        tinyint(4)      NOT NULL COMMENT '重试策略（固定间隔重试；线性退避重试；指数退避重试；）',
    `retry_count`           int(11)         NOT NULL COMMENT '重试次数',
    `retry_interval`        int(11)         NOT NULL COMMENT '重试间隔，单位秒（3s；3/6/9；3/9/27；）',
    `level`                 tinyint(4)      NOT NULL COMMENT '优先级，默认5级',
    `execution_timeout`     int(11)         NOT NULL COMMENT '执行超时时间，单位秒（大于0生效）',
    `add_time`              datetime        NOT NULL COMMENT '新增时间',
    `update_time`           datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_topic` (`topic`) USING BTREE
) ENGINE = InnoDB  DEFAULT CHARSET = utf8mb4 COMMENT ='消息主题';

CREATE TABLE `xxl_mq_message`(
    `id`                    bigint(20)      NOT NULL AUTO_INCREMENT,
    `topic`                 varchar(100)    NOT NULL COMMENT '消息主题Topic',
    `partition_id`          int(11)         NOT NULL COMMENT '消息分片ID',
    `data`                  text            NOT NULL COMMENT '消息数据',
    `biz_id`                bigint(20)      NOT NULL COMMENT '消息关联的业务ID',
    `status`                tinyint(4)      NOT NULL COMMENT '状态',
    `effect_time`           datetime        NOT NULL COMMENT '生效时间',
    `retry_count_remain`    int(11)         NOT NULL COMMENT '重试次数',
    `consume_log`           text            DEFAULT NULL COMMENT '消费日志',
    `consume_instance_uuid` varchar(50)     DEFAULT NULL COMMENT '消费实例实例唯一标识',
    `add_time`              datetime        NOT NULL COMMENT '新增时间',
    `update_time`           datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `i_t_s_p_e_i` (`topic`, `status`, `partition_id`, `effect_time`, `id`),
    KEY `i_t_s` (`topic`, `status`),
    KEY `i_cuuid` (`consume_instance_uuid`)
) ENGINE = InnoDB  DEFAULT CHARSET = utf8mb4 COMMENT ='消息数据表';

CREATE TABLE `xxl_mq_message_archive` (
    `id`                    bigint(20)      NOT NULL ,
    `topic`                 varchar(100)    NOT NULL COMMENT '消息主题Topic',
    `partition_id`          int(11)         NOT NULL COMMENT '消息分片ID',
    `data`                  text            NOT NULL COMMENT '消息数据',
    `biz_id`                bigint(20)      NOT NULL COMMENT '消息关联的业务ID',
    `status`                tinyint(4)      NOT NULL COMMENT '状态',
    `effect_time`           datetime        NOT NULL COMMENT '生效时间',
    `retry_count_remain`    int(11)         NOT NULL COMMENT '重试次数',
    `consume_log`           text            DEFAULT NULL COMMENT '消费日志',
    `consume_instance_uuid` varchar(50)     DEFAULT NULL COMMENT '消费实例实例唯一标识',
    `add_time`              datetime        NOT NULL COMMENT '新增时间',
    `update_time`           datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `i_t_s_p_e_i` (`topic`, `status`, `partition_id`, `effect_time`, `id`),
    KEY `i_cuuid` (`consume_instance_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息数据归档表';

CREATE TABLE `xxl_mq_message_report`(
    `id`            int(11)         NOT NULL AUTO_INCREMENT,
    `produce_day`   datetime        DEFAULT NULL COMMENT '生产-时间',
    `new_count`     int(11)         NOT NULL DEFAULT '0' COMMENT '新消息-数量',
    `running_count` int(11)         NOT NULL DEFAULT '0' COMMENT '运行中-数量',
    `suc_count`     int(11)         NOT NULL DEFAULT '0' COMMENT '执行成功-数量',
    `fail_count`    int(11)         NOT NULL DEFAULT '0' COMMENT '执行失败-数量',
    `update_time`   datetime        DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `i_produce_day` (`produce_day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息数据报表';

## —————————————————————— registry ——————————————————

CREATE TABLE `xxl_mq_instance` (
    `id`                 bigint(20)      NOT NULL AUTO_INCREMENT,
    `appname`            varchar(50)     NOT NULL COMMENT 'AppName（服务唯一标识）',
    `uuid`               varchar(50)     NOT NULL COMMENT '实例唯一标识',
    `register_heartbeat` datetime        DEFAULT NULL COMMENT '实例最后心跳时间，动态注册时判定是否过期',
    `add_time`           datetime        NOT NULL COMMENT '新增时间',
    `update_time`        datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_instance` (`appname`, `uuid`) USING BTREE,
    KEY `i_e_a` (`appname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务注册实例';

## —————————————————————— user and token and application ——————————————————

CREATE TABLE `xxl_mq_application` (
    `id`            int(11)         NOT NULL AUTO_INCREMENT,
    `appname`       varchar(50)     NOT NULL COMMENT 'AppName（服务唯一标识）',
    `name`          varchar(20)     NOT NULL COMMENT '服务名称',
    `desc`          varchar(100)    NOT NULL COMMENT '服务描述',
    `registry_data` text            DEFAULT NULL COMMENT '注册数据，JSON',
    `add_time`      datetime        NOT NULL COMMENT '新增时间',
    `update_time`   datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `i_appname` (`appname`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务';

CREATE TABLE `xxl_mq_user` (
     `id`            int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
     `username`      varchar(50) NOT NULL COMMENT '账号',
     `password`      varchar(50) NOT NULL COMMENT '密码',
     `user_token`    varchar(50) DEFAULT NULL COMMENT '登录token',
     `status`        tinyint(4)  NOT NULL COMMENT '状态：0-正常、1-禁用',
     `real_name`     varchar(50) DEFAULT NULL COMMENT '真实姓名',
     `role`          varchar(20) NOT NULL COMMENT '角色：ADMIN-管理员，NORMAL-普通用户',
     `permission`    varchar(255) DEFAULT NULL COMMENT '权限：服务ID列表，多个逗号分割',
     `add_time`      datetime    NOT NULL COMMENT '新增时间',
     `update_time`   datetime    NOT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `i_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `xxl_mq_access_token` (
    `id`             bigint(20)      NOT NULL AUTO_INCREMENT,
    `access_token`   varchar(50)     NOT NULL COMMENT '注册发现AccessToken',
    `status`         tinyint(4)      NOT NULL COMMENT '状态：0-正常、1-禁用',
    `add_time`       datetime        NOT NULL COMMENT '新增时间',
    `update_time`    datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注册发现AccessToken';

## —————————————————————— init data ——————————————————

INSERT INTO `xxl_mq_user`(`id`, `username`, `password`, `user_token`, `status`, `real_name`, `role`, `add_time`, `update_time`)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '', 0, 'Jack', 'ADMIN', now(), now()),
       (2, 'user', 'e10adc3949ba59abbe56e057f20f883e', '', 0, 'Lucy', 'NORMAL', now(), now());

INSERT INTO `xxl_mq_access_token` (id, `access_token`, `status`, add_time, update_time)
VALUES (1, 'defaultaccesstoken', 0, now(), now());

INSERT INTO `xxl_mq_application` (id, appname, name, `desc`, add_time, update_time)
VALUES (1, 'xxl-mq-sample', '示例服务', '示例服务，演示使用', '2025-01-18 20:03:13', '2025-01-18 20:03:13');

INSERT INTO `xxl_mq_topic` (id, topic, appname, `desc`, owner, alarm_email, status, store_strategy, archive_strategy, partition_strategy, retry_strategy, retry_count, retry_interval, level, execution_timeout, add_time, update_time)
VALUES (1, 'topic_sample', 'xxl-mq-sample', '示例1:全局并行消费', 'XXL', '', 0, '0', '1', '2', '1', 0, 3, 1, 0, now(), now()),
       (2, 'topic_sample_02', 'xxl-mq-sample', '示例2:全局串行消费', 'XXL', '', 0, '0', '1', '4', '1', 0, 3, 1, 0, now(), now()),
       (3, 'topic_sample_03', 'xxl-mq-sample', '示例3:串并行结合消费', 'XXL', '', 0, '0', '1', '1', '1', 0, 3, 1, 0, now(), now()),
       (4, 'topic_sample_04', 'xxl-mq-sample', '示例4:广播消费', 'XXL', '', 0, '0', '1', '6', '1', 0, 3, 1, 0, now(), now()),
       (5, 'topic_sample_05', 'xxl-mq-sample', '示例5:延时消息（模拟 延时3min）', 'XXL', '', 0, '0', '1', '1', '1', 0, 3, 1, 0, now(), now()),
       (6, 'topic_sample_06', 'xxl-mq-sample', '示例6:失败重试消息（模拟 重试3次）', 'XXL', '', 0, '0', '1', '1', '1', 3, 3, 1, 0, now(), now()),
       (7, 'topic_sample_07', 'xxl-mq-sample', '示例7:性能测试（模拟 生产10000条消息）', 'XXL', '', 0, '0', '1', '2', '1', 0, 3, 1, 0, now(), now());

INSERT INTO `xxl_mq_message` (id, topic, data, biz_id, partition_id, status, effect_time, retry_count_remain, consume_log, consume_instance_uuid, add_time, update_time)
VALUES (1, 'topic_sample', 'hello world.', 0,1, 0, now(), 0, null, null, now(), now());


commit;
