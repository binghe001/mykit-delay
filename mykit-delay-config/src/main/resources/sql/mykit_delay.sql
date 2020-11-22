/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50615
Source Host           : 127.0.0.1:3306
Source Database       : sdmq

Target Server Type    : MYSQL
Target Server Version : 50615
File Encoding         : 65001

Date: 2019-05-30 11:19:56
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for mykit_delay_queue_job
-- ----------------------------
DROP TABLE IF EXISTS `mykit_delay_queue_job`;
CREATE TABLE `mykit_delay_queue_job` (
  `id` varchar(128) NOT NULL,
  `bizkey` varchar(128) DEFAULT NULL,
  `topic` varchar(128) DEFAULT NULL,
  `subtopic` varchar(250) DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `create_time` bigint(20) DEFAULT NULL,
  `body` text,
  `status` int(11) DEFAULT NULL,
  `ttl` int(11) DEFAULT NULL,
  `update_time` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mykit_delay_queue_job_ID_STATUS` (`id`,`status`),
  KEY `mykit_delay_queue_job_STATUS` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for mykit_delay_queue_job_log
-- ----------------------------
DROP TABLE IF EXISTS `mykit_delay_queue_job_log`;
CREATE TABLE `mykit_delay_queue_job_log` (
  `id` varchar(128) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `thread` varchar(60) DEFAULT NULL,
  `update_time` datetime(3) DEFAULT NULL,
  `host` varchar(128) DEFAULT NULL,
  KEY `mykit_delay_queue_job_LOG_ID_STATUS` (`id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
