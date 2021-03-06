-- This script assumes the database already exists. The spring app uses the database
-- name appservspringbank. Create such a database before running this script, then run 
-- the script with the command
-- psql -d appservspringbank -f src/main/scripts/db/create-appservspringbank-postgres.sql

--
-- Drop all tables;
--
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS holder;

--
-- Create for table HOLDER
--
create table HOLDER (
    HLD_ID int8 generated by default as identity, 
    HLD_NO int8, 
    HLD_NAME varchar(30) not null, 
    HLD_OPTLOCK_VERSION int4, 
    primary key (HLD_ID)
);

--
-- Create table ACCOUNT
--
create table ACCOUNT (
    ACCT_ID int8 generated by default as identity, 
    ACCT_NO int8, 
    ACCT_BALANCE int4, 
    ACCT_OPTLOCK_VERSION int4, 
    FK_ACCOUNT_HOLDER int8 not null, 
    primary key (ACCT_ID)
);

alter table if exists ACCOUNT add constraint acct_fk foreign key (FK_ACCOUNT_HOLDER) references HOLDER;
