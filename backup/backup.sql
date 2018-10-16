--------------------------------------------------------
--  File created - воскресенье-августа-13-2017   
--------------------------------------------------------
DROP TABLE "DZ"."DZ_COMPANY" cascade constraints;
--------------------------------------------------------
--  DDL for Table DZ_COMPANY
--------------------------------------------------------

  CREATE TABLE "DZ"."DZ_COMPANY" 
   (	"ID" NUMBER, 
	"DEPCODE" VARCHAR2(20 CHAR), 
	"DEPJOB" VARCHAR2(100 CHAR), 
	"DESCRIPTION" VARCHAR2(255 CHAR)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "APEX_4811620023107695" ;
--------------------------------------------------------
--  DDL for Sequence DZ_COMPANY_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "DZ"."DZ_COMPANY_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 161 CACHE 20 NOORDER  NOCYCLE ;
REM INSERTING into DZ.DZ_COMPANY
SET DEFINE OFF;
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('140','Отдел аналитики','Начальник отдела',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('141','Отдел тестирования','Младший тестировщик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('142','Отдел аналитики','Младший аналитик','В отпуске');
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('143','Отдел тестирования','Главный тестировщик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('144','Отдел разработки','Младший разработчик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('114','Отдел аналитики','Ведущий аналитик','Лентяй');
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('115','Отдел аналитики','Старший аналитик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('116','Отдел аналитики','Главный аналитик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('117','Отдел кадров','Ведущий кадровик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('118','Отдел разработки','Ведущий разработчик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('121','Отдел кадров','Начальник отдела',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('122','Отдел кадров','Младший кадровик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('123','Отдел тестирования','Начальник отдела','В командировке');
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('125','Отдел разработки','Старший разработчик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('126','Отдел разработки','Главный разработчик','На больничном');
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('127','Отдел кадров','Старший кадровик','Декрет');
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('128','Отдел разработки','Начальник отдела',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('129','Отдел кадров','Главный кадровик',null);
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('130','Отдел тестирования','Старший тестировщик','Спит на рабочем месте');
Insert into DZ.DZ_COMPANY (ID,DEPCODE,DEPJOB,DESCRIPTION) values ('132','Отдел тестирования','Ведущий тестировщик',null);
--------------------------------------------------------
--  DDL for Index DZ_COMPANY_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "DZ"."DZ_COMPANY_PK" ON "DZ"."DZ_COMPANY" ("ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "APEX_4811620023107695" ;
--------------------------------------------------------
--  DDL for Index DZ_COMPANY_UK1
--------------------------------------------------------

  CREATE UNIQUE INDEX "DZ"."DZ_COMPANY_UK1" ON "DZ"."DZ_COMPANY" ("DEPCODE", "DEPJOB") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "APEX_4811620023107695" ;
--------------------------------------------------------
--  DDL for Trigger BI_DZ_COMPANY
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "DZ"."BI_DZ_COMPANY" 
  before insert on "DZ_COMPANY"               
  for each row  
begin   
  if :NEW."ID" is null then 
    select "DZ_COMPANY_SEQ".nextval into :NEW."ID" from dual; 
  end if; 
end; 

/
ALTER TRIGGER "DZ"."BI_DZ_COMPANY" ENABLE;
--------------------------------------------------------
--  DDL for Synonymn DUAL
--------------------------------------------------------

  CREATE OR REPLACE PUBLIC SYNONYM "DUAL" FOR "SYS"."DUAL";
--------------------------------------------------------
--  Constraints for Table DZ_COMPANY
--------------------------------------------------------

  ALTER TABLE "DZ"."DZ_COMPANY" ADD CONSTRAINT "DZ_COMPANY_UK1" UNIQUE ("DEPCODE", "DEPJOB")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "APEX_4811620023107695"  ENABLE;
  ALTER TABLE "DZ"."DZ_COMPANY" ADD CONSTRAINT "DZ_COMPANY_PK" PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "APEX_4811620023107695"  ENABLE;
  ALTER TABLE "DZ"."DZ_COMPANY" MODIFY ("DEPJOB" NOT NULL ENABLE);
  ALTER TABLE "DZ"."DZ_COMPANY" MODIFY ("DEPCODE" NOT NULL ENABLE);
  ALTER TABLE "DZ"."DZ_COMPANY" MODIFY ("ID" NOT NULL ENABLE);
