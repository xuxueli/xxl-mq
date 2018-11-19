CREATE database if NOT EXISTS `xxl-mq` default character set utf8 collate utf8_general_ci;
use `xxl-mq`;



CREATE TABLE `XXL_MQ_BIZ` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bizName` varchar(64) NOT NULL,
  `order` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `XXL_MQ_TOPIC` (
  `topic` varchar(255) NOT NULL,
  `bizId` int(11) NOT NULL DEFAULT '0',
  `messageInfo` varchar(255) NOT NULL,
  `author` varchar(64) DEFAULT NULL,
  `alarmEmails` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`topic`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `XXL_MQ_MESSAGE` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `topic` varchar(255) NOT NULL,
  `group` varchar(255) NOT NULL,
  `data` text NOT NULL,
  `status` varchar(32) NOT NULL,
  `retryCount` int(11) NOT NULL DEFAULT '0',
  `shardingId` bigint(11) NOT NULL DEFAULT '0',
  `timeout` int(11) NOT NULL DEFAULT '0',
  `effectTime` datetime NOT NULL,
  `addTime` datetime NOT NULL,
  `log` text NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `I_shardingId` (`shardingId`) USING BTREE,
  KEY `I_t_g_f_s` (`topic`,`group`,`status`,`effectTime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

