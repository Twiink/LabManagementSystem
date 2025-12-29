-- MySQL dump 10.13  Distrib 8.4.3, for macos14 (arm64)
--
-- Host: 127.0.0.1    Database: lab_management
-- ------------------------------------------------------
-- Server version	8.4.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `approvals`
--

DROP TABLE IF EXISTS `approvals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approvals` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `operator_id` bigint NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `reservation_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approvals`
--

LOCK TABLES `approvals` WRITE;
INSERT INTO `approvals` VALUES (1,'APPROVE','2025-12-27 01:32:40.000000',0,1,'Course approved',1,'2025-12-27 01:32:40.000000'),(2,'APPROVE','2025-12-27 01:32:40.000000',0,1,'Device available',3,'2025-12-27 01:32:40.000000'),(3,'REJECT','2025-12-27 01:32:40.000000',0,1,'Schedule conflict',7,'2025-12-27 01:32:40.000000'),(4,'APPROVE','2025-12-27 01:32:40.000000',0,8,'Lab available',4,'2025-12-27 01:32:40.000000'),(5,'APPROVE','2025-12-27 01:32:40.000000',0,8,'Device available',9,'2025-12-27 01:32:40.000000'),(6,'APPROVE',NULL,0,11,NULL,17,NULL),(7,'APPROVE',NULL,0,11,NULL,18,NULL),(8,'APPROVE',NULL,0,11,NULL,19,NULL),(9,'APPROVE',NULL,0,11,NULL,20,NULL);
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) NOT NULL,
  `actor_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `detail` json DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
INSERT INTO `audit_logs` VALUES (1,'RESERVATION_CREATE',2,'2025-12-27 01:32:40.000000','{\"note\": \"course reservation\"}',0,1,'RESERVATION','2025-12-27 01:32:40.000000'),(2,'RESERVATION_APPROVE',1,'2025-12-27 01:32:40.000000','{\"reason\": \"course approved\"}',0,1,'RESERVATION','2025-12-27 01:32:40.000000'),(3,'RESERVATION_CREATE',4,'2025-12-27 01:32:40.000000','{\"note\": \"student device\"}',0,2,'RESERVATION','2025-12-27 01:32:40.000000'),(4,'RESERVATION_REJECT',1,'2025-12-27 01:32:40.000000','{\"reason\": \"conflict\"}',0,7,'RESERVATION','2025-12-27 01:32:40.000000'),(5,'RESERVATION_APPROVE',8,'2025-12-27 01:32:40.000000','{\"reason\": \"lab available\"}',0,4,'RESERVATION','2025-12-27 01:32:40.000000'),(6,'RESERVATION_COURSE_CREATE',7,'2025-12-27 01:32:40.000000','{\"note\": \"cs course\"}',0,6,'RESERVATION','2025-12-27 01:32:40.000000'),(7,'RESERVATION_CREATE',3,'2025-12-27 01:32:40.000000','{\"note\": \"teacher device\"}',0,5,'RESERVATION','2025-12-27 01:32:40.000000'),(8,'RESERVATION_CREATE',9,NULL,NULL,0,13,'RESERVATION',NULL),(9,'RESERVATION_CREATE',9,NULL,NULL,0,14,'RESERVATION',NULL),(10,'RESERVATION_CREATE',9,NULL,NULL,0,15,'RESERVATION',NULL),(11,'RESERVATION_CREATE',9,NULL,NULL,0,16,'RESERVATION',NULL),(12,'RESERVATION_CREATE',9,NULL,NULL,0,17,'RESERVATION',NULL),(13,'RESERVATION_APPROVE',11,NULL,NULL,0,17,'RESERVATION',NULL),(14,'RESERVATION_CREATE',9,NULL,NULL,0,18,'RESERVATION',NULL),(15,'RESERVATION_APPROVE',11,NULL,NULL,0,18,'RESERVATION',NULL),(16,'RESERVATION_CREATE',9,NULL,NULL,0,19,'RESERVATION',NULL),(17,'RESERVATION_APPROVE',11,NULL,NULL,0,19,'RESERVATION',NULL),(18,'RESERVATION_CREATE',9,NULL,NULL,0,20,'RESERVATION',NULL),(19,'RESERVATION_APPROVE',11,NULL,NULL,0,20,'RESERVATION',NULL);
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_name` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `is_deleted` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `student_count` int NOT NULL,
  `term` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
INSERT INTO `courses` VALUES (1,'Chem 2023-1','2025-12-27 01:32:40.000000',2,0,'Analytical Chemistry',45,'2024-2025-2','2025-12-27 01:32:40.000000'),(2,'Phys 2023-2','2025-12-27 01:32:40.000000',3,0,'Modern Physics',38,'2024-2025-2','2025-12-27 01:32:40.000000'),(3,'Bio 2023-1','2025-12-27 01:32:40.000000',7,0,'Molecular Biology',32,'2024-2025-2','2025-12-27 01:32:40.000000'),(4,'CS 2023-1','2025-12-27 01:32:40.000000',7,0,'Data Structures',48,'2024-2025-2','2025-12-27 01:32:40.000000');
UNLOCK TABLES;

--
-- Table structure for table `devices`
--

DROP TABLE IF EXISTS `devices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `lab_id` bigint NOT NULL,
  `model` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devices`
--

LOCK TABLES `devices` WRITE;
INSERT INTO `devices` VALUES (1,'2025-12-27 01:32:40.000000',0,1,'UV-2600','紫外可见分光光度计','IDLE','2025-12-27 01:32:40.000000'),(2,'2025-12-27 01:32:40.000000',0,1,'CF-16RX','高速离心机','IDLE','2025-12-27 01:32:40.000000'),(3,'2025-12-27 01:32:40.000000',0,2,'TDS-2024C','数字示波器','IDLE','2025-12-27 01:32:40.000000'),(4,'2025-12-27 01:32:40.000000',0,2,'AFG-3102','信号发生器','IDLE','2025-12-27 01:32:40.000000'),(5,'2025-12-27 01:32:40.000000',0,3,'BX-53','荧光显微镜','MAINTENANCE','2025-12-27 01:32:40.000000'),(6,'2025-12-27 01:32:40.000000',0,4,'Form-3L','3D打印机','IDLE','2025-12-27 01:32:40.000000'),(7,'2025-12-27 01:32:40.000000',0,4,'RTX-4090','GPU计算工作站','IDLE','2025-12-27 01:32:40.000000'),(8,'2025-12-27 01:32:40.000000',0,1,'PHS-3C','pH酸度计','IDLE','2025-12-27 01:32:40.000000'),(9,NULL,0,1,'AA-7000','原子吸收光谱仪','IDLE',NULL),(10,NULL,0,1,'GC-2014','气相色谱仪','IDLE',NULL),(11,NULL,0,1,'ME-204','电子天平','IDLE',NULL),(12,NULL,0,2,'LS-532','激光器','IDLE',NULL),(13,NULL,0,2,'FSL-3','频谱分析仪','IDLE',NULL),(14,NULL,0,3,'T100','PCR扩增仪','IDLE',NULL),(15,NULL,0,3,'PowerPac','电泳仪','IDLE',NULL),(16,NULL,0,3,'SW-CJ','超净工作台','IDLE',NULL),(17,NULL,0,4,'S5720','网络交换机','IDLE',NULL),(18,NULL,0,4,'SR650','服务器','IDLE',NULL),(19,NULL,0,5,'RE-52AA','旋转蒸发仪','IDLE',NULL),(20,NULL,0,5,'MS-H280','磁力搅拌器','IDLE',NULL),(21,NULL,0,5,'DHG-9123A','烘箱','IDLE',NULL),(22,NULL,0,6,'SX2-4-10','马弗炉','IDLE',NULL),(23,NULL,0,6,'DZF-6050','真空干燥箱','IDLE',NULL),(24,NULL,0,7,'IT6302','直流稳压电源','IDLE',NULL),(25,NULL,0,7,'Fluke-87V','万用表','IDLE',NULL),(26,NULL,0,7,'LA-5034','逻辑分析仪','IDLE',NULL),(27,NULL,0,8,'U-EC6','编程器','IDLE',NULL),(28,NULL,0,8,'STM32F4','开发板套件','IDLE',NULL),(29,NULL,0,9,'ELx808','酶标仪','IDLE',NULL),(30,NULL,0,9,'DW-86L388','低温冰箱','IDLE',NULL),(31,NULL,0,10,'BSC-1100IIA2','生物安全柜','IDLE',NULL),(32,NULL,0,10,'DNP-9162','恒温培养箱','MAINTENANCE',NULL),(33,NULL,0,11,'WDW-100','万能材料试验机','IDLE',NULL),(34,NULL,0,11,'JB-300B','冲击试验机','IDLE',NULL),(35,NULL,0,12,'OT-1200','光学平台','IDLE',NULL),(36,NULL,0,12,'PM-100D','光功率计','IDLE',NULL);
UNLOCK TABLES;

--
-- Table structure for table `labs`
--

DROP TABLE IF EXISTS `labs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `labs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `capacity` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `location` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `open_time_end` time(6) NOT NULL,
  `open_time_start` time(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `labs`
--

LOCK TABLES `labs` WRITE;
INSERT INTO `labs` VALUES (1,40,'2025-12-27 01:32:40.000000',0,'理学楼1号楼203室','分析化学实验室','22:00:00.000000','08:00:00.000000','IDLE','2025-12-27 01:32:40.000000'),(2,35,'2025-12-27 01:32:40.000000',0,'理学楼2号楼105室','普通物理实验室','21:00:00.000000','08:00:00.000000','IDLE','2025-12-27 01:32:40.000000'),(3,30,'2025-12-27 01:32:40.000000',0,'生命科学楼307室','生物工程实验室','20:00:00.000000','09:00:00.000000','MAINTENANCE','2025-12-27 01:32:40.000000'),(4,50,'2025-12-27 01:32:40.000000',0,'信息楼4号楼101室','计算机网络实验室','22:30:00.000000','08:30:00.000000','IDLE','2025-12-27 01:32:40.000000'),(5,40,NULL,0,'理学楼1号楼205室','有机化学实验室','22:00:00.000000','08:00:00.000000','IDLE',NULL),(6,35,NULL,0,'理学楼1号楼301室','无机化学实验室','22:00:00.000000','08:00:00.000000','IDLE',NULL),(7,50,NULL,0,'信息楼3号楼201室','电子技术实验室','22:00:00.000000','08:00:00.000000','IDLE',NULL),(8,45,NULL,0,'信息楼3号楼203室','单片机实验室','22:00:00.000000','08:00:00.000000','IDLE',NULL),(9,30,NULL,0,'生命科学楼401室','生物化学实验室','22:00:00.000000','08:00:00.000000','IDLE',NULL),(10,25,NULL,0,'生命科学楼405室','微生物实验室','22:00:00.000000','08:00:00.000000','MAINTENANCE',NULL),(11,40,NULL,0,'理学楼2号楼201室','力学实验室','22:00:00.000000','08:00:00.000000','IDLE',NULL),(12,35,NULL,0,'理学楼2号楼305室','光学实验室','22:00:00.000000','08:00:00.000000','IDLE',NULL);
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `status` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
INSERT INTO `notifications` VALUES (1,'Reservation approved','2025-12-27 01:32:40.000000',0,'READ','RESERVATION','2025-12-27 01:32:40.000000',2),(2,'Reservation submitted','2025-12-27 01:32:40.000000',0,'UNREAD','RESERVATION','2025-12-27 01:32:40.000000',4),(3,'Reservation cancelled','2025-12-27 01:32:40.000000',0,'READ','RESERVATION','2025-12-27 01:32:40.000000',5),(4,'Reservation approved','2025-12-27 01:32:40.000000',0,'UNREAD','RESERVATION','2025-12-27 01:32:40.000000',6),(5,'Course reservation pending','2025-12-27 01:32:40.000000',0,'UNREAD','RESERVATION','2025-12-27 01:32:40.000000',7),(6,'Reservation approved','2025-12-27 01:32:40.000000',0,'READ','RESERVATION','2025-12-27 01:32:40.000000',3),(7,'Reservation rejected','2025-12-27 01:32:40.000000',0,'READ','RESERVATION','2025-12-27 01:32:40.000000',4),(8,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,2),(9,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,5),(10,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,6),(11,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,2),(12,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,3),(13,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,4),(14,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,5),(15,'Reservation submitted, status: APPROVED',NULL,0,'UNREAD','RESERVATION',NULL,9),(16,'Your reservation has expired due to no check-in',NULL,0,'UNREAD','RESERVATION',NULL,9),(17,'Reservation submitted, status: APPROVED',NULL,0,'UNREAD','RESERVATION',NULL,9),(18,'Reservation submitted, status: APPROVED',NULL,0,'UNREAD','RESERVATION',NULL,9),(19,'Reservation submitted, status: PENDING',NULL,0,'UNREAD','RESERVATION',NULL,9),(20,'Reservation submitted, status: PENDING',NULL,0,'UNREAD','RESERVATION',NULL,9),(21,'Reservation approved',NULL,0,'UNREAD','RESERVATION',NULL,9),(22,'Reservation submitted, status: PENDING',NULL,0,'UNREAD','RESERVATION',NULL,9),(23,'Reservation approved',NULL,0,'UNREAD','RESERVATION',NULL,9),(24,'Reservation submitted, status: PENDING',NULL,0,'UNREAD','RESERVATION',NULL,9),(25,'Reservation approved',NULL,0,'UNREAD','RESERVATION',NULL,9),(26,'Reservation submitted, status: PENDING',NULL,0,'UNREAD','RESERVATION',NULL,9),(27,'Reservation approved',NULL,0,'UNREAD','RESERVATION',NULL,9);
UNLOCK TABLES;

--
-- Table structure for table `reservation_series`
--

DROP TABLE IF EXISTS `reservation_series`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_series` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `mode` varchar(255) NOT NULL,
  `requester_id` bigint NOT NULL,
  `rule_type` varchar(255) NOT NULL,
  `rule_value` json NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_series`
--

LOCK TABLES `reservation_series` WRITE;
INSERT INTO `reservation_series` VALUES (1,'2025-12-27 01:32:40.000000',0,'PARTIAL',2,'WEEKLY','{\"count\": 6, \"daysOfWeek\": [1, 3, 5]}','2025-12-27 01:32:40.000000'),(2,'2025-12-27 01:32:40.000000',0,'STRICT',7,'DAILY','{\"count\": 4, \"interval\": 2}','2025-12-27 01:32:40.000000');
UNLOCK TABLES;

--
-- Table structure for table `reservation_series_items`
--

DROP TABLE IF EXISTS `reservation_series_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_series_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `reservation_id` bigint NOT NULL,
  `series_id` bigint NOT NULL,
  `status` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_series_items`
--

LOCK TABLES `reservation_series_items` WRITE;
INSERT INTO `reservation_series_items` VALUES (1,'2025-12-27 01:32:40.000000',0,1,1,'APPROVED','2025-12-27 01:32:40.000000'),(2,'2025-12-27 01:32:40.000000',0,9,1,'APPROVED','2025-12-27 01:32:40.000000'),(3,'2025-12-27 01:32:40.000000',0,10,1,'APPROVED','2025-12-27 01:32:40.000000'),(4,'2025-12-27 01:32:40.000000',0,6,2,'PENDING','2025-12-27 01:32:40.000000');
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `approved_at` datetime(6) DEFAULT NULL,
  `approved_by` bigint DEFAULT NULL,
  `cancel_reason` varchar(255) DEFAULT NULL,
  `course_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `device_id` bigint DEFAULT NULL,
  `end_time` datetime(6) NOT NULL,
  `is_deleted` int NOT NULL,
  `lab_id` bigint NOT NULL,
  `priority` varchar(255) NOT NULL,
  `requester_id` bigint NOT NULL,
  `start_time` datetime(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
INSERT INTO `reservations` VALUES (1,'2025-12-27 01:32:40.000000',1,'Auto-expired: no check-in within 30 minutes',1,'2025-12-27 01:32:40.000000',NULL,'2025-01-15 10:30:00.000000',0,1,'COURSE',2,'2025-01-15 08:30:00.000000','EXPIRED','COURSE','2025-12-27 01:32:40.000000',NULL),(2,NULL,NULL,NULL,NULL,'2025-12-27 01:32:40.000000',1,'2025-01-16 11:00:00.000000',0,1,'NORMAL',4,'2025-01-16 09:00:00.000000','PENDING','SINGLE','2025-12-27 01:32:40.000000',NULL),(3,'2025-12-27 01:32:40.000000',1,'Auto-expired: no check-in within 30 minutes',NULL,'2025-12-27 01:32:40.000000',3,'2025-01-16 15:00:00.000000',0,2,'NORMAL',5,'2025-01-16 13:00:00.000000','EXPIRED','SINGLE','2025-12-27 01:32:40.000000',NULL),(4,'2025-12-27 01:32:40.000000',8,'Auto-expired: no check-in within 30 minutes',NULL,'2025-12-27 01:32:40.000000',NULL,'2025-01-17 12:00:00.000000',0,2,'NORMAL',6,'2025-01-17 10:00:00.000000','EXPIRED','SINGLE','2025-12-27 01:32:40.000000',NULL),(5,NULL,NULL,NULL,NULL,'2025-12-27 01:32:40.000000',6,'2025-01-18 16:00:00.000000',0,4,'NORMAL',3,'2025-01-18 14:00:00.000000','PENDING','SINGLE','2025-12-27 01:32:40.000000',NULL),(6,NULL,NULL,NULL,4,'2025-12-27 01:32:40.000000',NULL,'2025-01-19 11:30:00.000000',0,4,'COURSE',7,'2025-01-19 09:30:00.000000','PENDING','COURSE','2025-12-27 01:32:40.000000',NULL),(7,'2025-12-27 01:32:40.000000',1,NULL,NULL,'2025-12-27 01:32:40.000000',2,'2025-01-20 09:30:00.000000',0,1,'NORMAL',4,'2025-01-20 08:30:00.000000','REJECTED','SINGLE','2025-12-27 01:32:40.000000',NULL),(8,NULL,NULL,'User cancel',NULL,'2025-12-27 01:32:40.000000',NULL,'2025-01-20 12:00:00.000000',0,1,'NORMAL',5,'2025-01-20 10:00:00.000000','CANCELLED','SINGLE','2025-12-27 01:32:40.000000',NULL),(9,'2025-12-27 01:32:40.000000',8,'Auto-expired: no check-in within 30 minutes',NULL,'2025-12-27 01:32:40.000000',4,'2025-01-21 16:00:00.000000',0,2,'NORMAL',2,'2025-01-21 14:00:00.000000','EXPIRED','SINGLE','2025-12-27 01:32:40.000000',NULL),(10,'2025-12-27 01:32:40.000000',1,'Auto-expired: no check-in within 30 minutes',NULL,'2025-12-27 01:32:40.000000',8,'2025-01-22 09:00:00.000000',0,1,'NORMAL',3,'2025-01-22 08:00:00.000000','EXPIRED','SINGLE','2025-12-27 01:32:40.000000',NULL),(11,'2025-12-27 01:32:40.000000',8,'Auto-expired: no check-in within 30 minutes',NULL,'2025-12-27 01:32:40.000000',7,'2025-01-22 12:00:00.000000',0,4,'NORMAL',4,'2025-01-22 10:00:00.000000','EXPIRED','SINGLE','2025-12-27 01:32:40.000000',NULL),(12,'2025-12-27 01:32:40.000000',1,'Auto-expired: no check-in within 30 minutes',NULL,'2025-12-27 01:32:40.000000',NULL,'2025-01-23 15:00:00.000000',0,2,'NORMAL',5,'2025-01-23 13:00:00.000000','EXPIRED','SINGLE','2025-12-27 01:32:40.000000',NULL),(13,NULL,NULL,'Auto-expired: no check-in within 30 minutes',NULL,NULL,NULL,'2025-12-16 16:00:00.000000',0,4,'NORMAL',9,'2025-12-15 16:00:00.000000','EXPIRED','SINGLE',NULL,NULL),(14,NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-31 16:00:00.000000',0,4,'NORMAL',9,'2025-12-30 16:00:00.000000','APPROVED','SINGLE',NULL,NULL),(15,NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-01 16:00:00.000000',0,2,'NORMAL',9,'2025-12-30 16:00:00.000000','APPROVED','SINGLE',NULL,NULL),(16,NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-31 16:00:00.000000',0,1,'NORMAL',9,'2025-12-30 16:00:00.000000','PENDING','SINGLE',NULL,'生物实验'),(17,'2025-12-29 02:53:18.756192',11,NULL,NULL,NULL,NULL,'2026-01-01 16:00:00.000000',0,9,'NORMAL',9,'2025-12-31 16:00:00.000000','APPROVED','SINGLE',NULL,'物理实验'),(18,'2025-12-29 03:23:23.488885',11,NULL,NULL,NULL,NULL,'2026-01-07 04:00:00.000000',0,9,'NORMAL',9,'2026-01-07 01:00:00.000000','APPROVED','SINGLE',NULL,'化学实验'),(19,'2025-12-29 03:29:50.752346',11,NULL,NULL,NULL,NULL,'2026-01-08 09:00:00.000000',0,8,'NORMAL',9,'2026-01-08 04:00:00.000000','APPROVED','SINGLE',NULL,'数学实验'),(20,'2025-12-29 03:53:03.220834',11,NULL,NULL,NULL,NULL,'2025-12-31 07:00:00.000000',0,7,'NORMAL',9,'2025-12-31 02:00:00.000000','APPROVED','SINGLE',NULL,'语文实验');
UNLOCK TABLES;

--
-- Table structure for table `rule_configs`
--

DROP TABLE IF EXISTS `rule_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `key_name` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `value` json NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_configs`
--

LOCK TABLES `rule_configs` WRITE;
INSERT INTO `rule_configs` VALUES (1,'2025-12-27 01:32:40.000000','Reservation rules',0,'reservation_rules','2025-12-27 01:32:40.000000','{\"studentCancelHours\": 24, \"teacherCancelHours\": 12, \"maxDailyReservations\": 2}'),(2,'2025-12-27 01:32:40.000000','Priority and override policy',0,'priority_rules','2025-12-27 01:32:40.000000','{\"coursePriority\": true, \"overridePolicy\": \"CANCEL_CONFLICTS\"}');
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `is_deleted` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
INSERT INTO `users` VALUES (1,'2025-12-27 01:32:40.000000','admin1@example.com',0,'Admin A','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000001','ADMIN','ACTIVE','2025-12-27 01:32:40.000000'),(2,'2025-12-27 01:32:40.000000','teacher1@example.com',0,'Teacher Li','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000002','TEACHER','ACTIVE','2025-12-27 01:32:40.000000'),(3,'2025-12-27 01:32:40.000000','teacher2@example.com',0,'Teacher Wang','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000003','TEACHER','ACTIVE','2025-12-27 01:32:40.000000'),(4,'2025-12-27 01:32:40.000000','student1@example.com',0,'Student Chen','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000004','STUDENT','ACTIVE','2025-12-27 01:32:40.000000'),(5,'2025-12-27 01:32:40.000000','student2@example.com',0,'Student Zhao','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000005','STUDENT','ACTIVE','2025-12-27 01:32:40.000000'),(6,'2025-12-27 01:32:40.000000','student3@example.com',0,'Student Liu','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000006','STUDENT','ACTIVE','2025-12-27 01:32:40.000000'),(7,'2025-12-27 01:32:40.000000','teacher3@example.com',0,'Teacher Sun','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000007','TEACHER','ACTIVE','2025-12-27 01:32:40.000000'),(8,'2025-12-27 01:32:40.000000','admin2@example.com',0,'Admin B','{bcrypt}$2b$12$Ub2RNxl7O1Z6lR7XT.a.PuUe359MVD8h1/jcNy6xk2ULCUim0JATG','13000000008','ADMIN','ACTIVE','2025-12-27 01:32:40.000000'),(9,'2025-12-28 01:04:06.468541','zy13990529155@163.com',0,'test','{bcrypt}$2a$10$UpmCsLZtW2mPeiSBVx8Md.RE6QFOh7ndsozAyzJXSY9wG1MrH8sFK','13990529155','STUDENT','ACTIVE','2025-12-28 01:04:06.468552'),(10,'2025-12-28 01:32:10.000000','admin@lab.com',0,'admin','{bcrypt}$2b$12$3ZkOf2XuEAknONrrUUtkf.NCUJGBWzhkZimWT/IgWcrjUHeVWjoY6',NULL,'ADMIN','ACTIVE','2025-12-28 01:32:10.000000'),(11,'2025-12-28 01:32:10.000000','teacher@lab.com',0,'teacher','{bcrypt}$2b$12$3ZkOf2XuEAknONrrUUtkf.NCUJGBWzhkZimWT/IgWcrjUHeVWjoY6',NULL,'TEACHER','ACTIVE','2025-12-28 01:32:10.000000');
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-29 12:36:04
