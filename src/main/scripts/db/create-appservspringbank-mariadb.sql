-- Assumes the database already exists. The spring app uses the database name `appservspringbank`,
-- create such a database before running this script. Run the script with the command
-- mysql appservspringbank < src/scripts/db/create-appservspringbank-mariadb.sql -p


--
-- Drop all tables;
--
DROP TABLE IF EXISTS `BANK_SEQUENCE`;
DROP TABLE IF EXISTS `ACCOUNT`;
DROP TABLE IF EXISTS `HOLDER`;

--
-- Create for table `HOLDER`
--
CREATE TABLE `HOLDER`
(
  `HLD_ID` bigint
(20) NOT NULL,
  `HLD_NO` bigint
(20) NOT NULL,
  `HLD_NAME` varchar
(50) NOT NULL,
  `HLD_OPTLOCK_VERSION` int
(11) DEFAULT NULL,
  PRIMARY KEY
(`HLD_ID`),
  UNIQUE KEY `HLD_NO`
(`HLD_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Create table `ACCOUNT`
--
CREATE TABLE `ACCOUNT`
(
  `ACCT_ID` bigint
(20) NOT NULL,
  `FK_ACCOUNT_HOLDER` bigint
(20) NOT NULL,
  `ACCT_NO` bigint
(20) NOT NULL,
  `ACCT_BALANCE` int
(11) NOT NULL,
  `ACCT_OPTLOCK_VERSION` int
(11) DEFAULT NULL,
  PRIMARY KEY
(`ACCT_ID`),
  UNIQUE KEY `ACCT_NO`
(`ACCT_NO`),
  KEY `FK_ACCOUNT_HOLDER`
(`FK_ACCOUNT_HOLDER`),
  CONSTRAINT `FK_ACCOUNT_HOLDER` FOREIGN KEY
(`FK_ACCOUNT_HOLDER`) REFERENCES `HOLDER`
(`HLD_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Create sequence `BANK_SEQUENCE`
--

CREATE SEQUENCE `BANK_SEQUENCE` start
with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 cache 1000 nocycle ENGINE=InnoDB;
