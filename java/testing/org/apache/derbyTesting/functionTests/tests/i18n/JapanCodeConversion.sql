drop table T1_EUC_JP;
-- table for data in EUC_JP encoding
create table T1_EUC_JP (	jnum int,
				jtime time,
				jstring char(200) );
	

-- import data in EUC_JP encoding 
call SYSCS_UTIL.SYSCS_IMPORT_TABLE(null, 'T1_EUC_JP' ,
	 		          'extin/jap_EUC_JP.dat' , 
				  null, null, 'EUC_JP', 0) ;
				  
SELECT jnum, jtime, { fn length(jstring) } AS JLEN from T1_EUC_JP;

-- export to file with EUC_JP encoding 
call SYSCS_UTIL.SYSCS_EXPORT_TABLE('APP', 'T1_EUC_JP' ,
	 		          'extinout/jap_EUC_JP.dump' , 
				  null, null, 'EUC_JP') ;

-- export to file with SJIS encoding	
call SYSCS_UTIL.SYSCS_EXPORT_TABLE('APP', 'T1_EUC_JP' ,
	 		          'extinout/jap_SJIS.dump' , 
				  null, null, 'SJIS') ;

-- import as EUC_JP and compare to original
create table T1_EUC_JP_IMPORT_AS_EUC_JP(jnum int, jtime time, jstring char(200));
						
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1_EUC_JP_IMPORT_AS_EUC_JP',
   'extinout/jap_EUC_JP.dump',
   NULL, NULL,
   'EUC_JP', 0);

SELECT jnum, jtime, { fn length(jstring) } AS JLEN from T1_EUC_JP_IMPORT_AS_EUC_JP;

SELECT count(*) FROM T1_EUC_JP OG, T1_EUC_JP_IMPORT_AS_EUC_JP IM
  WHERE OG.jnum = IM.jnum AND OG.jtime = IM.jtime AND OG.jstring = IM.jstring;

-- import as SJIS and compare to original
create table T1_EUC_JP_IMPORT_AS_SJIS(jnum int, jtime time, jstring char(200));

call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1_EUC_JP_IMPORT_AS_SJIS',
   'extinout/jap_SJIS.dump',
   NULL, NULL,
   'SJIS', 0);

SELECT jnum, jtime, { fn length(jstring) } AS JLEN from T1_EUC_JP_IMPORT_AS_SJIS;

SELECT count(*) FROM T1_EUC_JP OG, T1_EUC_JP_IMPORT_AS_SJIS IM
  WHERE OG.jnum = IM.jnum AND OG.jtime = IM.jtime AND OG.jstring = IM.jstring;

delete from T1_EUC_JP_IMPORT_AS_EUC_JP;
delete from T1_EUC_JP_IMPORT_AS_SJIS;

-- now import using the wrong encoding file just to ensure that different
-- encodings are being used.

call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1_EUC_JP_IMPORT_AS_EUC_JP',
   'extinout/jap_SJIS.dump',
   NULL, NULL,
   'EUC_JP', 0);
SELECT jnum, jtime, { fn length(jstring) } AS JLEN from T1_EUC_JP_IMPORT_AS_EUC_JP;
SELECT count(*) FROM T1_EUC_JP OG, T1_EUC_JP_IMPORT_AS_EUC_JP IM
  WHERE OG.jnum = IM.jnum AND OG.jtime = IM.jtime AND OG.jstring = IM.jstring;   
  
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1_EUC_JP_IMPORT_AS_SJIS',
   'extinout/jap_EUC_JP.dump',
   NULL, NULL,
   'SJIS', 0);
SELECT jnum, jtime, { fn length(jstring) } AS JLEN from T1_EUC_JP_IMPORT_AS_SJIS;
SELECT count(*) FROM T1_EUC_JP OG, T1_EUC_JP_IMPORT_AS_SJIS IM
  WHERE OG.jnum = IM.jnum AND OG.jtime = IM.jtime AND OG.jstring = IM.jstring;


-- convert from EUC_JP to unicode with native2ascii
! 'native2ascii -encoding EUC_JP extinout/jap_EUC_JP.dump';

-- convert from SJIS to unicode with native2ascii
! 'native2ascii -encoding SJIS extinout/jap_SJIS.dump';
