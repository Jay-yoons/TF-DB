CREATE TABLE BOOKING 
    ( 
     BOOKING_NUM        INTEGER  NOT NULL , 
     BOOKING_DATE       TIMESTAMP  NOT NULL , 
     USER_ID            VARCHAR2 (15 CHAR)  NOT NULL , 
     STORE_ID           VARCHAR2 (20)  NOT NULL , 
     BOOKING_STATE_CODE INTEGER  NOT NULL , 
     COUNT              INTEGER  NOT NULL 
    ) 
;

ALTER TABLE BOOKING 
    ADD CONSTRAINT BOOKING_PK PRIMARY KEY ( BOOKING_NUM ) ;

ALTER TABLE BOOKING 
    ADD CONSTRAINT BOOKING_BOOKING_STATE_CODE_FK FOREIGN KEY 
    ( 
     BOOKING_STATE_CODE
    ) 
    REFERENCES BOOKING_STATE_CODE 
    ( 
     BOOKING_STATE_CODE
    ) 
;

CREATE SEQUENCE BOOKING_BOOKING_NUM_SEQ 
START WITH 1 
    NOCACHE 
    ORDER ;

CREATE OR REPLACE TRIGGER BOOKING_BOOKING_NUM_TRG 
BEFORE INSERT ON BOOKING 
FOR EACH ROW 
WHEN (NEW.BOOKING_NUM IS NULL) 
BEGIN 
    :NEW.BOOKING_NUM := BOOKING_BOOKING_NUM_SEQ.NEXTVAL; 
END;
/


---------------------------------------------------------------------

CREATE TABLE BOOKING_STATE_CODE 
    ( 
     BOOKING_STATE_CODE INTEGER  NOT NULL , 
     STATE_NAME         VARCHAR2 (10)  NOT NULL 
    ) 
;

ALTER TABLE BOOKING_STATE_CODE 
    ADD CONSTRAINT BOOKING_STATE_CODE_PK PRIMARY KEY ( BOOKING_STATE_CODE ) ;

------------------------------------------------------------------------------
우선 CDB에서 DB LINK를 생성 할 타 PDB에서 VIEW 생성을 허용 해줘야한다.
CDB 환경 (컨테이너 DB)에서 PDB 간 DB LINK를 허용하려면 아래 설정 필요:

LOCAL_LINKS=ALLOWED 또는 COMMON_USER 사용

공통 유저 또는 각 PDB에서 접속 가능한 전용 유저 필요

NETWORK SERVICE 이름이 각 PDB에 대해 정의돼 있어야 함 (tnsnames.ora or EZCONNECT)

ALTER TABLE USERS_VIEW 
    ADD CONSTRAINT USERS_PK PRIMARY KEY ( USER_ID ) ;

그 후 아래 명령을 통해 뷰를 만들어 무결성을 지키는것이 가능하다.
-----------------------------------------------------------------
CREATE DATABASE LINK USERS_PDB_LINK
CONNECT TO users IDENTIFIED BY "users_password"
USING 'PDB_USER';
-----------------------------------------------------------------------
-- STORE DB LINK (예: STORES_PDB_LINK)
CREATE DATABASE LINK STORES_PDB_LINK
CONNECT TO stores IDENTIFIED BY "stores_password"
USING 'PDB_STORE';

