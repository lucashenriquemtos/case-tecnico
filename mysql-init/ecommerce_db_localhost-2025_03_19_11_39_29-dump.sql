-- MySQL dump 10.13  Distrib 8.0.40, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: ecommerce_db
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `pedido`
--

DROP TABLE IF EXISTS `pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido` (
  `id` binary(16) NOT NULL,
  `data_criacao` datetime(6) NOT NULL,
  `status` enum('CANCELADO','ENTREGUE','ENVIADO','PAGO','PENDENTE','PROCESSANDO') NOT NULL,
  `valor_total` decimal(38,2) NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6uxomgomm93vg965o8brugt00` (`usuario_id`),
  CONSTRAINT `FK6uxomgomm93vg965o8brugt00` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido`
--

LOCK TABLES `pedido` WRITE;
/*!40000 ALTER TABLE `pedido` DISABLE KEYS */;
INSERT INTO `pedido` (`id`, `data_criacao`, `status`, `valor_total`, `usuario_id`) VALUES (_binary 'ŒçˇU˙C¿ç\\Da\«','2025-03-19 11:22:36.202902','PAGO',1299.99,1),(_binary '≈∫_\ÛJâø\√\Û\”\◊B','2025-03-19 11:23:40.597257','PAGO',1299.99,1),(_binary '2˙öãK6≠Wæ\√$\’Q\\','2025-03-19 11:31:49.708507','PAGO',899.90,2),(_binary '$S\“%k<O≠≤ò\ÓF\Îe[','2025-03-19 11:32:51.286627','PAGO',599.99,3),(_binary '+≠\∆ØCØ\ÿ LhQ\√#','2025-03-19 11:33:22.793233','PAGO',899.90,4),(_binary '-$¯\"ØüNñÅH√Ç\‰J','2025-03-19 11:28:29.673164','PAGO',1299.99,1),(_binary 'K\…%CG\”H\˜ìëÑn\‰|','2025-03-19 11:24:33.860795','PAGO',1299.99,1),(_binary 'âï[˙˙\˜Nû•≤ã\÷¡Ä∫','2025-03-19 14:21:18.377442','PAGO',1299.99,1),(_binary '°2§\€\’\€@Á•≤ﬂºG5ªD','2025-03-19 11:24:10.814987','PAGO',1299.99,1),(_binary '≈ÅRzáuK…©\…\Âæ.ÄÑ?','2025-03-19 11:30:02.604594','PAGO',2199.89,1),(_binary '‹âf\….M^é?™˙ÖB≥','2025-03-19 11:36:30.308808','PAGO',1199.98,4),(_binary '\„ü)aBiø\Ú}¥©®ö','2025-03-19 11:35:14.322170','PAGO',949.98,4);
/*!40000 ALTER TABLE `pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido_produto`
--

DROP TABLE IF EXISTS `pedido_produto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido_produto` (
  `id` binary(16) NOT NULL,
  `preco_unitario` decimal(38,2) NOT NULL,
  `quantidade` int NOT NULL,
  `pedido_id` binary(16) NOT NULL,
  `produto_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcsbxw0y9i3wfmiupq9eqfpdtc` (`pedido_id`),
  KEY `FKf8l3k06bmjhdwd79t0ndcw7tt` (`produto_id`),
  CONSTRAINT `FKcsbxw0y9i3wfmiupq9eqfpdtc` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `FKf8l3k06bmjhdwd79t0ndcw7tt` FOREIGN KEY (`produto_id`) REFERENCES `produto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido_produto`
--

LOCK TABLES `pedido_produto` WRITE;
/*!40000 ALTER TABLE `pedido_produto` DISABLE KEYS */;
INSERT INTO `pedido_produto` (`id`, `preco_unitario`, `quantidade`, `pedido_id`, `produto_id`) VALUES (_binary '\náΩù\‘nE]ú\»_˚à´Ü',899.90,1,_binary '+≠\∆ØCØ\ÿ LhQ\√#',_binary '\÷A[:	RJ`ÜLÅÅ\√\…j0'),(_binary '¸±\rôKú∏≤K/}™n',1299.99,1,_binary '≈ÅRzáuK…©\…\Âæ.ÄÑ?',_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í'),(_binary 'cU6≤πAßØ\'Õπh5 \Ï',1299.99,1,_binary 'âï[˙˙\˜Nû•≤ã\÷¡Ä∫',_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í'),(_binary '≤ÅH\·DÄç@ﬁΩéf',1299.99,1,_binary '°2§\€\’\€@Á•≤ﬂºG5ªD',_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í'),(_binary '>\”\›5\›AE™M\ËŒ™\ÓŒ®',899.90,1,_binary '≈ÅRzáuK…©\…\Âæ.ÄÑ?',_binary '<?,\ÙF•∞>ÃÉL©í'),(_binary 'c\0Q¡\—¿BÜ;/çnÇ\0',599.99,1,_binary '\„ü)aBiø\Ú}¥©®ö',_binary '™∫z–π*G:Æ\ı…≠r~\◊'),(_binary 'çt\⁄πIﬂΩïû\”icûá',1299.99,1,_binary 'K\…%CG\”H\˜ìëÑn\‰|',_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í'),(_binary 'èé\"˙B¡ïQ9†\Ú3@',599.99,1,_binary '$S\“%k<O≠≤ò\ÓF\Îe[',_binary '™∫z–π*G:Æ\ı…≠r~\◊'),(_binary 'ê≠\ÔPS1Dè∫ïN$u@¸',599.99,2,_binary '‹âf\….M^é?™˙ÖB≥',_binary '™∫z–π*G:Æ\ı…≠r~\◊'),(_binary 'ï\Úû\À˝@a≤\÷\Ì_À∑æ',899.90,1,_binary '2˙öãK6≠Wæ\√$\’Q\\',_binary '<?,\ÙF•∞>ÃÉL©í'),(_binary '´\Áé]	@ñö\˜ô\Í3ü',1299.99,1,_binary '-$¯\"ØüNñÅH√Ç\‰J',_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í'),(_binary '∫|±\–\rÇ@\‡î6\"\‡ÖPè',1299.99,1,_binary 'ŒçˇU˙C¿ç\\Da\«',_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í'),(_binary '∫ã˛¨TêH´íh°gPπ',349.99,1,_binary '\„ü)aBiø\Ú}¥©®ö',_binary '\ÎÉ\'\„`!E2´ÿÑ\n\È?9v'),(_binary '\·»Å˛\–\ÓMà´O\Ô Y£¥!',1299.99,1,_binary '≈∫_\ÛJâø\√\Û\”\◊B',_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í');
/*!40000 ALTER TABLE `pedido_produto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produto`
--

DROP TABLE IF EXISTS `produto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produto` (
  `id` binary(16) NOT NULL,
  `nome` varchar(255) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `preco` decimal(38,2) NOT NULL,
  `categoria` varchar(255) DEFAULT NULL,
  `quantidade_estoque` int NOT NULL,
  `data_criacao` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_atualizacao` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produto`
--

LOCK TABLES `produto` WRITE;
/*!40000 ALTER TABLE `produto` DISABLE KEYS */;
INSERT INTO `produto` (`id`, `nome`, `descricao`, `preco`, `categoria`, `quantidade_estoque`, `data_criacao`, `data_atualizacao`) VALUES (_binary '\r\Èhâ!eE|†\∆\ÁbJ˙\Í','Cadeira Gamer','Cadeira ergon√¥mica para gamers com apoio para os bra√ßos ajust√°vel',1299.99,'M√≥veis',8,'2025-03-19 14:20:14','2025-03-19 11:30:03'),(_binary '”â∫\ÏPK\nô\Ám&°≠XW','Smartphone 12 Pro','Smartphone com c√¢mera de 108MP e tela OLED',3999.99,'Eletr√¥nicos',20,'2025-03-19 14:19:29','2025-03-19 14:19:29'),(_binary '<?,\ÙF•∞>ÃÉL©í','Monitor Full HD 24','Monitor de 24 polegadas com resolu√ß√£o Full HD e borda fina',899.90,'Eletr√¥nicos',23,'2025-03-19 14:20:07','2025-03-19 11:31:50'),(_binary '™∫z–π*G:Æ\ı…≠r~\◊','Fone de Ouvido Bluetooth','Fone de ouvido sem fio com cancelamento de ru√≠do',599.99,'Acess√≥rios',46,'2025-03-19 14:19:35','2025-03-19 11:36:30'),(_binary '\÷A[:	RJ`ÜLÅÅ\√\…j0','Cadeira Ergon√¥mica','Cadeira de escrit√≥rio ajust√°vel com apoio lombar',899.90,'M√≥veis',11,'2025-03-19 14:19:03','2025-03-19 11:33:23'),(_binary '\ÎÉ\'\„`!E2´ÿÑ\n\È?9v','Teclado Mec√¢nico RGB','Teclado mec√¢nico com switches RGB e antighosting',349.99,'Acess√≥rios',29,'2025-03-19 14:19:42','2025-03-19 11:35:14');
/*!40000 ALTER TABLE `produto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `senha` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5171l57faosmj8myawaucatdw` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` (`id`, `email`, `role`, `senha`) VALUES (1,'admin@ecommerce.com','ROLE_ADMIN','$2a$10$NLJDo8/D/r1AIFCdMGkGm.DbP/dBO8qle6rHHqvLKXLpWerB0iJga'),(2,'user@ecommerce.com','ROLE_USER','$2a$10$NLJDo8/D/r1AIFCdMGkGm.DbP/dBO8qle6rHHqvLKXLpWerB0iJga'),(3,'user1@ecommerce.com','ROLE_USER','$2a$10$NLJDo8/D/r1AIFCdMGkGm.DbP/dBO8qle6rHHqvLKXLpWerB0iJga'),(4,'user2@ecommerce.com','ROLE_USER','$2a$10$NLJDo8/D/r1AIFCdMGkGm.DbP/dBO8qle6rHHqvLKXLpWerB0iJga'),(5,'user3@ecommerce.com','ROLE_USER','$2a$10$NLJDo8/D/r1AIFCdMGkGm.DbP/dBO8qle6rHHqvLKXLpWerB0iJga'),(6,'user4@ecommerce.com','ROLE_USER','$2a$10$NLJDo8/D/r1AIFCdMGkGm.DbP/dBO8qle6rHHqvLKXLpWerB0iJga');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-19 11:39:29
