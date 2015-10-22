/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50544
Source Host           : localhost:3306
Source Database       : test

Target Server Type    : MYSQL
Target Server Version : 50544
File Encoding         : 65001

Date: 2015-10-17 04:46:59   
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for xxlmq_queue_lock
-- ----------------------------
DROP TABLE IF EXISTS `xxlmq_queue_lock`;
CREATE TABLE `xxlmq_queue_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for xxlmq_queue_message
-- ----------------------------
DROP TABLE IF EXISTS `xxlmq_queue_message`;
CREATE TABLE `xxlmq_queue_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `queue_name` varchar(255) NOT NULL,
  `invoke_request` varchar(512) NOT NULL,
  `effect_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` int(11) NOT NULL DEFAULT '0',
  `retry_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for xxlmq_topic_log
-- ----------------------------
DROP TABLE IF EXISTS `xxlmq_topic_log`;
CREATE TABLE `xxlmq_topic_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for xxlmq_topic_message
-- ----------------------------
DROP TABLE IF EXISTS `xxlmq_topic_message`;
CREATE TABLE `xxlmq_topic_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic_name` varchar(255) NOT NULL,
  `invoke_request` varchar(512) NOT NULL,
  `effect_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
