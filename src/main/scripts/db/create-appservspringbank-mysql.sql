-- Assumes the database already exists. The spring app uses the database
-- name `appservspringbank`. Create such a database before running this script. Run 
-- the script with the command
-- mysql appservspringbank < src/main/scripts/db/create-appservspringbank-mysql.sql -p

--
-- Drop all tables;
--
DROP TABLE IF EXISTS `ACCOUNT`;
DROP TABLE IF EXISTS `HOLDER`;

--
-- Create for table `HOLDER`
--
CREATE TABLE `HOLDER` (
       `HLD_ID` bigint NOT NULL AUTO_INCREMENT,
       `HLD_NO` bigint DEFAULT NULL,
       `HLD_NAME` varchar(30) NOT NULL,
       `HLD_OPTLOCK_VERSION` int DEFAULT NULL,
       PRIMARY KEY (`HLD_ID`)
     ) ENGINE=InnoDB;

--
-- Create table `ACCOUNT`
--
CREATE TABLE `ACCOUNT` (
       `ACCT_ID` bigint NOT NULL AUTO_INCREMENT,
       `ACCT_NO` bigint DEFAULT NULL,
       `ACCT_BALANCE` int DEFAULT NULL,
       `ACCT_OPTLOCK_VERSION` int DEFAULT NULL,
       `FK_ACCOUNT_HOLDER` bigint NOT NULL,
       PRIMARY KEY (`ACCT_ID`),
       KEY (`FK_ACCOUNT_HOLDER`),
       CONSTRAINT FOREIGN KEY (`FK_ACCOUNT_HOLDER`) REFERENCES `HOLDER` (`HLD_ID`)
     ) ENGINE=InnoDB;
