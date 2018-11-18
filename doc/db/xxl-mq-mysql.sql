CREATE database if NOT EXISTS `xxl-mq` default character set utf8 collate utf8_general_ci;
use `xxl-mq`;

CREATE TABLE `XXL_MQ_MESSAGE`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `topic` varchar(512) NOT NULL,
  `group` varchar(256) NOT NULL,
  `data` text NOT NULL,
  `status` varchar(32) NOT NULL,
  `retryCount` int(11) NOT NULL DEFAULT 0,
  `shardingId` bigint(11) NOT NULL DEFAULT 0,
  `timeout` int(11) NOT NULL DEFAULT 0,
  `effectTime` datetime DEFAULT NULL,
  `addTime` datetime DEFAULT NULL,
  `log` text NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `I_shardingId`(`shardingId`) USING BTREE,
  INDEX `I_t_g_f_s`(`topic`, `group`, `status`, `effectTime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
