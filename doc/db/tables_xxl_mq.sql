#
# XXL-MQ
# Copyright (c) 2015-present, xuxueli.

CREATE database if NOT EXISTS `xxl_mq` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `xxl_mq`;

SET NAMES utf8mb4;


## —————————————————————— config data ——————————————————

CREATE TABLE `xxl_mq_topic`(
    `id`          int(11)      NOT NULL AUTO_INCREMENT,
    `topic`       varchar(255) NOT NULL COMMENT '消息主题Topic',
    `desc`        varchar(100) NOT NULL COMMENT '消息主题名称',
    `store_type`  tinyint(4)   NOT NULL COMMENT '存储类型：0-通用存储，2-单独存储',
    `level`       int(11)      NOT NULL COMMENT '优先级',
    `owner`       varchar(50)  NOT NULL COMMENT '负责人',
    `alarm_email` varchar(255) DEFAULT NULL COMMENT '告警配置（邮箱）',
    `add_time`    datetime     NOT NULL COMMENT '新增时间',
    `update_time` datetime     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_topic` (`topic`) USING BTREE
) ENGINE = InnoDB  DEFAULT CHARSET = utf8mb4 COMMENT ='消息主题';

CREATE TABLE `xxl_mq_message`(
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `data`           text         NOT NULL COMMENT '消息数据',
    `topic`          varchar(255) NOT NULL COMMENT '消息主题Topic',
    `group`          varchar(255) NOT NULL COMMENT '消息分组',
    `sharding_id`    int(11)      NOT NULL COMMENT '消息分片序号',
    `status`         tinyint(4)   NOT NULL COMMENT '状态：0-正常、1-运行中、2-成功、3-失败',
    `retry_count`    int(11)      NOT NULL COMMENT '重试次数',
    `retry_type`     varchar(100) NOT NULL COMMENT '重试策略（固定；增长；指数）',
    `retry_interval` int(11)      NOT NULL COMMENT '重试间隔（3s；2/4/6；2/4/8）',
    `effect_time`    datetime     NOT NULL COMMENT '生效时间',
    `consume_log`    text DEFAULT NULL COMMENT '消费地址',
    `add_time`       datetime     NOT NULL COMMENT '新增时间',
    `update_time`    datetime     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `i_t_g_1` (`topic`, `group`, `sharding_id`)
) ENGINE = InnoDB  DEFAULT CHARSET = utf8mb4 COMMENT ='消息数据表';

CREATE TABLE `xxl_mq_message_archive` (
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `data`           text         NOT NULL COMMENT '消息数据',
    `topic`          varchar(255) NOT NULL COMMENT '消息主题Topic',
    `group`          varchar(255) NOT NULL COMMENT '消息分组',
    `sharding_id`    int(11)      NOT NULL COMMENT '消息分片序号',
    `status`         tinyint(4)   NOT NULL COMMENT '状态：0-正常、1-运行中、2-成功、3-失败',
    `retry_count`    int(11)      NOT NULL COMMENT '重试次数',
    `retry_type`     varchar(100) NOT NULL COMMENT '重试策略（固定；增长；指数）',
    `retry_interval` int(11)      NOT NULL COMMENT '重试间隔（3s；2/4/6；2/4/8）',
    `effect_time`    datetime     NOT NULL COMMENT '生效时间',
    `consume_log`    text DEFAULT NULL COMMENT '消费日志',
    `add_time`       datetime     NOT NULL COMMENT '新增时间',
    `update_time`    datetime     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `i_t_g_1` (`topic`, `group`, `sharding_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息数据归档表';

## —————————————————————— registry of application ——————————————————

CREATE TABLE `xxl_mq_registry` (
    `id`            bigint(20)      NOT NULL AUTO_INCREMENT,
    `type`          tinyint(4)      NOT NULL COMMENT '注册类型：1-Broker，2-Consumer',
    `key`           varchar(255)    NOT NULL COMMENT '注册Key（broker，consumer—uuid）',
    `data`          text            NOT NULL COMMENT '数据正文，json结构体（address01，topic01&group）',
    `add_time`      datetime        NOT NULL COMMENT '新增时间',
    `update_time`   datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注册表';





## —————————————————————— user and token and application ——————————————————

CREATE TABLE `xxl_mq_application` (
    `id`            int(11)         NOT NULL AUTO_INCREMENT,
    `appname`       varchar(50)     NOT NULL COMMENT 'AppName（应用唯一标识）',
    `name`          varchar(20)     NOT NULL COMMENT '应用名称',
    `desc`          varchar(100)    NOT NULL COMMENT '应用描述',
    `add_time`      datetime        NOT NULL COMMENT '新增时间',
    `update_time`   datetime        NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `i_appname` (`appname`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用';

CREATE TABLE `xxl_mq_user` (
     `id`            int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
     `username`      varchar(50) NOT NULL COMMENT '账号',
     `password`      varchar(50) NOT NULL COMMENT '密码',
     `user_token`    varchar(50) DEFAULT NULL COMMENT '登录token',
     `status`        tinyint(4)  NOT NULL COMMENT '状态：0-正常、1-禁用',
     `real_name`     varchar(50) DEFAULT NULL COMMENT '真实姓名',
     `role`          varchar(20) NOT NULL COMMENT '角色：ADMIN-管理员，NORMAL-普通用户',
     `permission`    varchar(255) DEFAULT NULL COMMENT '权限：应用ID列表，多个逗号分割',
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
VALUES (1, 'xxl-mq-sample', '示例应用', '示例应用，演示使用', '2025-01-18 20:03:13', '2025-01-18 20:03:13');


commit;
