--
-- Drop all tables;
--
DROP TABLE IF EXISTS `BANK_SEQUENCE`;
DROP TABLE IF EXISTS `ACCOUNT`;
DROP TABLE IF EXISTS `HOLDER`;

--
-- Create for table `HOLDER`
--
CREATE TABLE `HOLDER` (
  `HLD_ID` bigint(20) NOT NULL,
  `HLD_NO` bigint(20) NOT NULL,
  `HLD_NAME` varchar(50) NOT NULL,
  `HLD_OPTLOCK_VERSION` int(11) DEFAULT NULL,
  PRIMARY KEY (`HLD_ID`),
  UNIQUE KEY `HLD_NO` (`HLD_NO`)
);

--
-- Create table `ACCOUNT`
--
CREATE TABLE `ACCOUNT` (
  `ACCT_ID` bigint NOT NULL,
  `ACCT_NO` bigint UNIQUE NOT NULL,
  `ACCT_BALANCE` integer NOT NULL,
  `ACCT_OPTLOCK_VERSION` integer,
  `FK_ACCOUNT_HOLDER` bigint not null,
  PRIMARY KEY (`ACCT_ID`));

ALTER TABLE `ACCOUNT`
  ADD CONSTRAINT `FK_ACCOUNT_HOLDER` FOREIGN KEY (`FK_ACCOUNT_HOLDER`) references `HOLDER` (`HLD_ID`);

--
-- No need to create sequence `BANK_SEQUENCE` manually, but how is it created???
--
-- CREATE SEQUENCE `BANK_SEQUENCE` start
-- with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 cache 1000 nocycle;
