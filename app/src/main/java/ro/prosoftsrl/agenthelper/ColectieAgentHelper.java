package ro.prosoftsrl.agenthelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;

import ro.prosoftsrl.agenti.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.renderscript.Type;
import android.util.Log;
	public class ColectieAgentHelper extends SQLiteOpenHelper {
		public static final int DATABASE_VERSION =103 ;
		public static final String DATABASE_NAME="ColectieAgenti";
		public long nIdStartGenunic=0;
		private Context context; 
		enum Tabele {
			GENUNIC ,
			ANTET 
		}

		public ColectieAgentHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context=context;
			int iIdDevice=Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_ecran1_id_agent), "0"));
			nIdStartGenunic=70000000 + (iIdDevice - 1) * 1000000;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			Class<?>[] classe= this.getClass().getClasses();
			Log.d("CRE", "ininte de CRE");
			for (Class<?> clasa : classe) {
				if (clasa.getName().toLowerCase().contains("table_")) {
					try {
						Log.d("CRE","Creat:"+clasa.getName());
						Field field=clasa.getField("SQL_CREATE_ENTRIES");
						try {
							this.batchSqlExec(field.get(field).toString(),db);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d("CRE","Err:"+e.getMessage());
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d("CRE","Err:"+e.getMessage());
					}
				}
			}
			initGenunic(db, this.nIdStartGenunic);
			initDiverse(db);
		}
		// se executa un sir ce contine comenzi sql separate de ;
		public void batchSqlExec(String sir, SQLiteDatabase db) {
			String[] comenzi = sir.split(";");
			for (String comanda : comenzi) {
				try {
					db.execSQL(comanda);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			beforeUpgrade(db);
			Class<?>[] classe= this.getClass().getClasses();
			for (Class<?> clasa : classe) {
				if (clasa.getName().toLowerCase().contains("table_")) {
					String tbname;
					String[][] struct;
					try {
						Field field=clasa.getField("SQL_CREATE_ENTRIES");
						try {
							this.batchSqlExec(field.get(field).toString(),db);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						field=clasa.getField("TABLE_NAME");
						tbname = field.get(field).toString();
						Field str=clasa.getField("STR_"+tbname.toUpperCase());
						struct = (String[][]) str.get(str);
						Cursor crs=db.rawQuery("SELECT * FROM "+tbname+" WHERE 1=0 ", null);
						String[] col=crs.getColumnNames();
						// se verifica daca coloanele din struct se gasesc in col
                        Log.d("PRO","tabela:"+tbname+" lung:"+struct.length+" comapar:"+col.length);
						for (int i = 0; i < struct.length; i++) {
							String colname=struct[i][0];
							String coltype=struct[i][1];
							boolean lGasit=false;
							for (int j = 0; j < col.length; j++) {
								Log.d("STRUCT","Str="+colname+"  Crt="+col[j]);
								if(colname.equals(col[j])) lGasit=true;
							}
							if (!lGasit){
                                Log.d("PRO","Gasita diferenta "+tbname+" coloana:"+colname);
								// coloana nu exista si se adauga
								String sqlSir=
									"ALTER TABLE "+tbname+" ADD COLUMN "+colname+" "+coltype; 
								if (coltype.equals(Types.DATE)) sqlSir=sqlSir+" ; UPDATE "+tbname+" SET "+colname+"= '' " ;
								else if (coltype.equals(Types.INTREG)) sqlSir=sqlSir+" ; UPDATE "+tbname+" SET "+colname+"= 0 " ;
								else if (coltype.equals(Types.PRIMARY)) sqlSir=sqlSir+" ; UPDATE "+tbname+" SET "+colname+"= 0 " ;
								else if (coltype.equals(Types.TEXT)) sqlSir=sqlSir+" ; UPDATE "+tbname+" SET "+colname+"='' " ;
								else if (coltype.equals(Types.VALOARE)) sqlSir=sqlSir+" ; UPDATE "+tbname+" SET "+colname+"=0 " ;
								
								batchSqlExec(sqlSir, db);
								Log.d("UPG",sqlSir);
							}
						}
						crs.close();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			}
			initDiverse(db);
		}
		// de facut inainte de initializare
		private void beforeUpgrade(SQLiteDatabase db) {
			// reface tabela de tip_doc pentru a elimina dublarile
			db.beginTransaction();
			db.execSQL(Table_TempContinutDocumente.SQL_DROP_TABLE);
			db.execSQL(Table_Tipdoc.SQL_DROP_TABLE);
// temporar eliminare inreg gresite
//			db.execSQL(" UPDATE "+Table_Antet.TABLE_NAME +" SET val_fara=0, val_tva=0 WHERE _id IN " +
//					"( 15101387,15101400,15101418,15101440,15101492,15101524)");
//			db.execSQL(" DELETE from "+Table_Pozitii.TABLE_NAME+" WHERE id_antet IN (15101387,15101400,15101418,15101440,15101492,15101524)");
			
//			db.execSQL("DROP TABLE IF EXIST temp_antet_sablon");
//			db.execSQL("DROP TABLE IF EXIST temp_pozitii_sablon");
//			db.execSQL("ALTER TABLE "+Table_Sablon_Antet.TABLE_NAME+" RENAME TO temp_antet_sablon");
//			db.execSQL("ALTER TABLE "+Table_Sablon_Pozitii.TABLE_NAME+" RENAME TO temp_pozitii_sablon");
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		
		// diverse initializari care se fac dupa upgrade sau init in afara de numarul unic
		private void initDiverse (SQLiteDatabase db) {
			// diverse instructiuni
//			Log.d("UPG","Ininte de insert");
//			db.execSQL("DELETE FROM "+Table_Sablon_Antet.TABLE_NAME);
//			db.execSQL("DELETE FROM "+Table_Sablon_Pozitii.TABLE_NAME);
//			db.execSQL("INSERT INTO "+Table_Sablon_Antet.TABLE_NAME +" SELECT " +
//					"_id,id_user,id_device,id_part,id_agent,id_ruta,id_cursa,data,id_tipdoc,transmis,blocat,s_timestamp,flag " +
//					"FROM temp_antet_sablon");
//			db.execSQL("INSERT INTO "+Table_Sablon_Pozitii.TABLE_NAME +" SELECT " +
//					"_id,id_antet,id_produs,cantitate,diferente,transmis,id_um,s_timestamp,flag " +
//					"FROM temp_pozitii_sablon");
			
			db.beginTransaction();
			ContentValues cval = new ContentValues();
			cval.put(Table_Clienti._ID, -1);
			cval.put(Table_Clienti.COL_DENUMIRE,"AVIZE INCARCARE - DESCARCARE" );
			cval.put(Table_Clienti.COL_ADRESA, "Introducere manuala avize de marfa");
			db.insertWithOnConflict(Table_Clienti.TABLE_NAME,null , cval,  SQLiteDatabase.CONFLICT_REPLACE);
			cval.put(Table_Clienti._ID, -3);
			cval.put(Table_Clienti.COL_DENUMIRE,"TRANSFER AMANUNT" );
			cval.put(Table_Clienti.COL_ADRESA, "Generare nota de transfer pt. amanunt");
			db.insertWithOnConflict(Table_Clienti.TABLE_NAME,null , cval,  SQLiteDatabase.CONFLICT_REPLACE);

			db.delete(Table_Clienti.TABLE_NAME,Table_Clienti._ID+"=-2" ,null);
			cval.clear();
			cval.put(Table_Tipdoc._ID, 1);
			cval.put(Table_Tipdoc.COL_DENUMIRE, "COMANDA");
			db.insertWithOnConflict(Table_Tipdoc.TABLE_NAME,null,cval,SQLiteDatabase.CONFLICT_REPLACE);
			cval.clear();
			cval.put(Table_Tipdoc._ID, 2);
			cval.put(Table_Tipdoc.COL_DENUMIRE, "FACTURA");
			db.insertWithOnConflict(Table_Tipdoc.TABLE_NAME,null,cval,SQLiteDatabase.CONFLICT_REPLACE);
			cval.clear();
			cval.put(Table_Tipdoc._ID, 3);
			cval.put(Table_Tipdoc.COL_DENUMIRE, "AVIZ CLIENT");
			db.insertWithOnConflict(Table_Tipdoc.TABLE_NAME,null,cval,SQLiteDatabase.CONFLICT_REPLACE);
			cval.clear();
			cval.put(Table_Tipdoc._ID, 4);
			cval.put(Table_Tipdoc.COL_DENUMIRE, "AVIZ DESC");
			db.insertWithOnConflict(Table_Tipdoc.TABLE_NAME,null,cval,SQLiteDatabase.CONFLICT_REPLACE);
			cval.clear();
			cval.put(Table_Tipdoc._ID, 5);
			cval.put(Table_Tipdoc.COL_DENUMIRE, "AVIZ INC");
			db.insertWithOnConflict(Table_Tipdoc.TABLE_NAME,null,cval,SQLiteDatabase.CONFLICT_REPLACE);
			cval.clear();
			cval.put(Table_Tipdoc._ID, 6);
			cval.put(Table_Tipdoc.COL_DENUMIRE, "BON FISCAL");
			db.insertWithOnConflict(Table_Tipdoc.TABLE_NAME,null,cval,SQLiteDatabase.CONFLICT_REPLACE);
			cval.clear();
			cval.put(Table_Tipdoc._ID, 7);
			cval.put(Table_Tipdoc.COL_DENUMIRE, "CERERE MARFA");
			db.insertWithOnConflict(Table_Tipdoc.TABLE_NAME,null,cval,SQLiteDatabase.CONFLICT_REPLACE);			
			db.setTransactionSuccessful();
			db.endTransaction();
			
		}
		
		//public void
		//stergerea continutului tabelelor si recrearea genunic numar dat in nIdStart 
		public void initTabele(SQLiteDatabase db, long nIdStart) {
			Class<?>[] classe= this.getClass().getClasses();
			for (Class<?> clasa : classe) {
				if (clasa.getName().toLowerCase().contains("table_")) {
					try {
						Field field=clasa.getField("SQL_DROP_TABLE");
						db.execSQL(field.get(field).toString());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			}
			this.onCreate(db);
			if (nIdStart!=0) this.nIdStartGenunic=nIdStart;
			this.initGenunic(db, nIdStart);
		}
		
		// initializeaza genunic astfel incat sa porneasca de la o anumita valoare
		public void initGenunic (SQLiteDatabase db, long nIdStart) {
			if (nIdStart>0) {
				db.execSQL("DELETE FROM "+Table_GenUnic.TABLE_NAME);
				ContentValues cvalues = new ContentValues() ;
				cvalues.put(Table_GenUnic._ID,nIdStart );
				long newRowId =db.insert(Table_GenUnic.TABLE_NAME, null, cvalues);
				Log.d("INI", ""+newRowId)  ;
			}
		}
		
		//creeaza o copie a fisirului de baza de date
		public void createCopieDb () {
			try {
	            File sd = Environment.getExternalStorageDirectory();
	            Log.d("Dir card:",sd.toString());
	            File data = Environment.getDataDirectory();
	            Log.d("Dir data:",data.toString());
	
	            if (sd.canWrite()) {
	                String currentDBPath = "//data//ro.prosoftsrl.agenti//databases//"+ColectieAgentHelper.DATABASE_NAME;
	                String backupDBPath = "backupname.db";
	                File currentDB = new File(data, currentDBPath);
	                File backupDB = new File(sd, backupDBPath);
	
	                if (currentDB.exists()) {
	                	Log.d("Copiere","Incepe");
	                    FileChannel src = new FileInputStream(currentDB).getChannel();
	                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
	                    dst.transferFrom(src, 0, src.size());
	                    src.close();
	                    dst.close();
	                }
	                else {
	                	Log.d("Copiere","nu exista fis:"+currentDB.getAbsolutePath()+currentDB.getName());
	                }
	            }
	            else {
	            	Log.d("Copiere","Nu se poate scrie pe card");
	            }
	        } catch (Exception e) {
	        	Log.d("COPIEDB",e.getMessage());
	        }		
		}
		//determina urmatorul id in genunic
		public long getNextId(SQLiteDatabase db) {
			ContentValues cvalues = new ContentValues() ;
			cvalues.put(Table_GenUnic.COL_FAKE,"" );
			db.beginTransaction();
			long newRowId =db.insert(Table_GenUnic.TABLE_NAME, null, cvalues);
			db.setTransactionSuccessful();
			db.endTransaction();
			Log.d("INI", ""+newRowId)  ;
			return newRowId;
		}

		// tipuri de date pt server
		public static abstract class STypes {
			public static final String INTREG="INTREG";
			public static final String STRING="STRING";
			public static final String VALOARE="VALOARE";
			public static final String DATA="DATA";
			public static final String DTOS="DTOS"; // data in format aaaallzz
		}
		
		// tipuri de date pt local
		public static abstract class Types {
			public static final String INTREG=" INTEGER NOT NULL DEFAULT 0 " ;
			public static final String PRIMARY=" INTEGER PRIMARY KEY ";
			public static final String PRIMARY_AUTO=" INTEGER PRIMARY KEY AUTOINCREMENT ";
			public static final String TEXT=" TEXT NOT NULL DEFAULT \'\' ";
			public static final String DATE=" DATE NOT NULL DEFAULT CURRENT_DATE ";
			public static final String VALOARE=" NUMERIC NOT NULL DEFAULT 0.000000 ";
			public static final String TIMESTAMP=" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ";
		}
		

		//propritati pe care trebuie sa le aiba orice tabela
		// table_name , col_s_timestamp, col_c_timestamp,stable_name,
		// scol_<nume coloana>
		//SCOL_PT_TIMESTAMP=pt_timestamp,
		// col_sincro_server , sql_drop_table, sql_create_entries		
		// "cast(timestamp as integer) as "+Table_Produse.SCOL_PT_TIMESTAMP;

		//tabela TIPDOC
		//nu participa la sincronizare
		public static abstract class Table_Tipdoc implements BaseColumns {
			public static final String TABLE_NAME="tipdoc";
			public static final String COL_DENUMIRE="denumire";
			public static final String COL_C_TIMESTAMP="flag";
			public static final String STABLE_NAME="";

			public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Tipdoc.TABLE_NAME;
			public static final String SQL_CREATE_ENTRIES=
				"CREATE TABLE IF NOT EXISTS " +
				Table_Tipdoc.TABLE_NAME+ " ( " +
				Table_Tipdoc._ID+Types.PRIMARY+" , "+
				Table_Tipdoc.COL_DENUMIRE+Types.TEXT+" , "+
				Table_Tipdoc.COL_C_TIMESTAMP+Types.TEXT+
				" ) ; " +
				" INSERT OR REPLACE INTO "+Table_Tipdoc.TABLE_NAME+" (_id,denumire) VALUES "+
				" (1,'COMANDA') ; "+
				" INSERT OR REPLACE INTO "+Table_Tipdoc.TABLE_NAME+" (_id,denumire) VALUES "+
				" (2,'FACTURA') ; " +
				" INSERT OR REPLACE INTO "+Table_Tipdoc.TABLE_NAME+" (_id,denumire) VALUES "+
				" (3,'AVIZ CLIENT') ; "+
				" INSERT OR REPLACE INTO "+Table_Tipdoc.TABLE_NAME+" (_id,denumire) VALUES "+
				" (4,'AVIZ DESC') ;" + 
				" INSERT OR REPLACE INTO "+Table_Tipdoc.TABLE_NAME+" (_id,denumire) VALUES "+
				" (5,'AVIZ INC') ;" +
				" INSERT OR REPLACE INTO "+Table_Tipdoc.TABLE_NAME+" (_id,denumire) VALUES "+
				" (6,'BON FISCAL') ;"+
				" INSERT OR REPLACE INTO "+Table_Tipdoc.TABLE_NAME+" (_id,denumire) VALUES "+
				" (7,'CERERE MARFA') "
				;
			
		}
			
		//tabela TEMPCONTINUTDOCUMENTE
		// nu participa la sincronizare 
		public static abstract class Table_TempContinutDocumente implements BaseColumns {
			public static final String TABLE_NAME="tempcontinutdocumente";
			public static final String COL_ID_PRODUS="id_produs";
			public static final String COL_CANTITATE="cantitate";
			public static final String COL_DIFERENTE="diferente";
			public static final String COL_FARA_PRET="fara_pret";
			public static final String COL_PRET_FARA="pret_fara";
			public static final String COL_PRET_CU="pret_cu";
            public static final String COL_ID_FA1="id_fa1";
            public static final String COL_ID_FA2="id_fa2";
			public static final String COL_C_TIMESTAMP="flag";
			public static final String COL_C_ESTE_BONUS="este_bonus";
			public static final String COL_S_TIMESTAMP="s_timestamp";
			public static final String STABLE_NAME="";
			public static final String COL_SINCRO_SERVER="";
			
			public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_TempContinutDocumente.TABLE_NAME;
			public static final String SQL_CREATE_ENTRIES=
				"CREATE TABLE IF NOT EXISTS " +
				Table_TempContinutDocumente.TABLE_NAME+ " ( " +
				Table_TempContinutDocumente._ID+Types.PRIMARY_AUTO+" , "+
				Table_TempContinutDocumente.COL_ID_PRODUS+Types.INTREG+" , "+
				Table_TempContinutDocumente.COL_CANTITATE+Types.VALOARE+" , "+
				Table_TempContinutDocumente.COL_DIFERENTE+Types.VALOARE+" , "+
				Table_TempContinutDocumente.COL_PRET_FARA+Types.VALOARE+" , "+
				Table_TempContinutDocumente.COL_PRET_CU+Types.VALOARE+" , "+
                Table_TempContinutDocumente.COL_ID_FA1+Types.INTREG+" , "+
                Table_TempContinutDocumente.COL_ID_FA2+Types.INTREG+" , "+
				Table_TempContinutDocumente.COL_C_ESTE_BONUS+Types.INTREG+","+
				Table_TempContinutDocumente.COL_FARA_PRET+Types.INTREG+","+
				Table_TempContinutDocumente.COL_S_TIMESTAMP+Types.INTREG+","+
				Table_TempContinutDocumente.COL_C_TIMESTAMP+Types.TEXT+
				" ) ";	
			
			
		}
		
		//tabela SOLDPART
		public static abstract class Table_Soldpart implements BaseColumns {
			public static final String TABLE_NAME="soldpart";
			public static final String COL_ID_PART="id_part";
			public static final String COL_NR_DOC="nr_doc";
			public static final String COL_DATA="data";
			public static final String COL_VAL_INI="val_ini";
			public static final String COL_REST="rest";
			public static final String COL_DATA_SCAD="data_scad";
			public static final String COL_S_TIMESTAMP="s_timestamp";
			public static final String COL_C_TIMESTAMP="flag";
			
			public static final String STABLE_NAME="soldpart";
			public static final String SCOL_COD_INT="cod_int";
			public static final String SCOL_ID_PART="id_part";
			public static final String SCOL_NR_DOC="nr_doc";
			public static final String SCOL_DATA="data";
			public static final String SCOL_VAL_INI="val_ini";
			public static final String SCOL_REST="rest";
			public static final String SCOL_DATA_SCAD="data_scad";
			public static final String SCOL_PT_TIMESTAMP="pt_timestamp" ;

			public static final String COL_SINCRO_SERVER=
				Table_Soldpart.SCOL_COD_INT+","+
				Table_Soldpart.SCOL_ID_PART+","+
				Table_Soldpart.SCOL_NR_DOC+","+
				Table_Soldpart.SCOL_DATA+","+
				Table_Soldpart.SCOL_VAL_INI+","+
				Table_Soldpart.SCOL_REST+","+
				Table_Soldpart.SCOL_DATA_SCAD+","+
				"cast(timestamp as integer) as "+Table_Soldpart.SCOL_PT_TIMESTAMP;
			public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Soldpart.TABLE_NAME;
			public static final String SQL_CREATE_ENTRIES=
				"CREATE TABLE IF NOT EXISTS " +
				Table_Soldpart.TABLE_NAME+ " ( " +
				Table_Soldpart._ID+Types.PRIMARY+" , "+
				Table_Soldpart.COL_ID_PART+Types.INTREG+" , "+
				Table_Soldpart.COL_NR_DOC+Types.TEXT+" , "+
				Table_Soldpart.COL_DATA+Types.DATE+" , "+
				Table_Soldpart.COL_VAL_INI+Types.VALOARE+" , "+
				Table_Soldpart.COL_REST+Types.VALOARE+" , "+
				Table_Soldpart.COL_DATA_SCAD+Types.DATE+" , "+
				Table_Soldpart.COL_S_TIMESTAMP+Types.INTREG +" , "+
				Table_Soldpart.COL_C_TIMESTAMP+Types.TEXT+
				" ) ";
			
		}

		//tabela DISCOUNT
		public static abstract class Table_Discount implements BaseColumns {
			public static final String TABLE_NAME = "discount";
			public static final String COL_ID_PRODUS="id_produs";
			public static final String COL_ID_CLIENT="id_client";
			public static final String COL_DISCOUNT="discount";
			public static final String COL_PRET_CU="pret_cu";
			public static final String COL_PRET_FARA="pret_fara";
			public static final String COL_S_TIMESTAMP="s_timestamp";
			public static final String COL_BLOCAT="blocat";
			public static final String COL_C_TIMESTAMP="flag";
			
			public static final String STABLE_NAME="discount";
			public static final String SCOL_COD_INT="cod_int";
			public static final String SCOL_ID_PRODUS="id_produs";
			public static final String SCOL_ID_CLIENT="id_client";
			public static final String SCOL_DISCOUNT="discount" ; // valoarea discountului
			public static final String SCOL_PRET_CU="pret_cu";
			public static final String SCOL_PRET_FARA="pret_fara";
			public static final String SCOL_BLOCAT="blocat";
			public static final String SCOL_PT_TIMESTAMP="pt_timestamp" ;
			
			public static final String COL_SINCRO_SERVER= 
				Table_Discount.SCOL_COD_INT+","+
				Table_Discount.SCOL_ID_PRODUS+","+
				Table_Discount.SCOL_ID_CLIENT+","+
				Table_Discount.SCOL_DISCOUNT+","+
				Table_Discount.SCOL_PRET_CU+","+
				Table_Discount.SCOL_PRET_FARA+","+
				Table_Discount.SCOL_BLOCAT+","+
				"cast(timestamp as integer) as "+Table_Discount.SCOL_PT_TIMESTAMP;

            public static final String [][] STR_DISCOUNT ={
                    {Table_Discount._ID,Types.INTREG,Table_Discount.SCOL_COD_INT,STypes.INTREG},
                    {Table_Discount.COL_DISCOUNT,Types.VALOARE,Table_Discount.SCOL_DISCOUNT,STypes.VALOARE},
                    {Table_Discount.COL_C_TIMESTAMP,Types.TEXT},
                    {Table_Discount.COL_ID_CLIENT,Types.INTREG,Table_Discount.SCOL_ID_CLIENT,STypes.INTREG},
                    {Table_Discount.COL_PRET_CU,Types.VALOARE,Table_Discount.SCOL_PRET_CU,STypes.VALOARE},
                    {Table_Discount.COL_PRET_FARA,Types.VALOARE,Table_Discount.SCOL_PRET_FARA,STypes.VALOARE},
                    {Table_Discount.COL_ID_PRODUS,Types.INTREG,Table_Discount.SCOL_ID_PRODUS,STypes.INTREG},
					{Table_Discount.COL_BLOCAT,Types.INTREG,Table_Discount.SCOL_BLOCAT,STypes.INTREG},
                    {Table_Discount.COL_S_TIMESTAMP,Types.INTREG}
            };
			public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Discount.TABLE_NAME;
			public static final String SQL_CREATE_ENTRIES=
					"CREATE TABLE IF NOT EXISTS " +
					Table_Discount.TABLE_NAME + " ( " +
					Table_Discount._ID+Types.PRIMARY+" , "+
					Table_Discount.COL_ID_PRODUS+Types.INTREG+" , "+
					Table_Discount.COL_ID_CLIENT+Types.INTREG+" , "+
					Table_Discount.COL_DISCOUNT+Types.VALOARE+" , "+
					Table_Discount.COL_PRET_CU+Types.VALOARE+" , "+
					Table_Discount.COL_PRET_FARA+Types.VALOARE+" , "+
					Table_Discount.COL_S_TIMESTAMP+Types.INTREG +" , "+
					Table_Discount.COL_BLOCAT+Types.INTREG+" , "+
					Table_Discount.COL_C_TIMESTAMP+Types.TEXT+
					" ) ";

		}
		// tabela PRODUSE
		public static abstract class Table_Produse implements BaseColumns {
			public static final String TABLE_NAME = "produse";
			public static final String COL_ORDONARE="ordonare";
			public static final String COL_COD_PROD="cod_prod";
			public static final String COL_DENUMIRE="denumire";
			public static final String COL_PRET_FARA="pret_fara";
			public static final String COL_PRET_CU="pret_cu";
			public static final String COL_PRET_FARA1="pret_fara1"; // pentru casa de marcat
			public static final String COL_PRET_CU1="pret_cu1";
			
			public static final String COL_COTA_TVA="cota_tva";
			public static final String COL_BLOCAT="blocat";
			public static final String COL_ID_MASTER="id_master";
			public static final String COL_ID_TIP="id_tip";
			public static final String COL_BUC_BOX="buc_box";
			public static final String COL_S_TIMESTAMP="s_timestamp";
			public static final String COL_C_TIMESTAMP="flag";
			public static final String COL_SUPLIM1="suplim1";
			public static final String COL_SUPLIM2="suplim2";
			public static final String COL_SUPLIM3="suplim3";
			public static final String COL_SUPLIM4="suplim4";
			//nume tabela in server
			public static final String STABLE_NAME="produse";
			// coloanele din server
			public static final String SCOL_COD_INT="cod_int";
			public static final String SCOL_ORDONARE="ordonare";
			public static final String SCOL_DENUMIRE="denumire";
			public static final String SCOL_COD_PROD="cod_prod";
			public static final String SCOL_PRET_FARA="pret_fara";
			public static final String SCOL_PRET_CU="pret_cu";
			public static final String SCOL_PRET_FARA1="pret_fara1";
			public static final String SCOL_PRET_CU1="pret_cu1";
			public static final String SCOL_COTA_TVA="cota_tva";
			public static final String SCOL_ID_TIP="id_tip";
			public static final String SCOL_ID_MASTER="id_master";
			public static final String SCOL_BUC_BOX="buc_box";
			public static final String SCOL_BLOCAT="blocat";
			//public static final String SCOL_FLAG="flag";
			public static final String SCOL_PT_TIMESTAMP="pt_timestamp";
			public static final String SCOL_SUPLIM1="suplim1";
			public static final String SCOL_SUPLIM2="suplim2";
			public static final String SCOL_SUPLIM3="suplim3";
			public static final String SCOL_SUPLIM4="suplim4";
			//coloanele ce se extrag din server
			public static final String 
				COL_SINCRO_SERVER=
					Table_Produse.SCOL_COD_INT+","+
					Table_Produse.SCOL_ORDONARE+","+
					Table_Produse.SCOL_DENUMIRE+","+
					Table_Produse.SCOL_PRET_FARA+","+
					Table_Produse.SCOL_PRET_CU+","+
					Table_Produse.SCOL_PRET_FARA1+","+
					Table_Produse.SCOL_PRET_CU1+","+
					Table_Produse.SCOL_COTA_TVA+","+
					Table_Produse.SCOL_ID_TIP+","+
					Table_Produse.SCOL_ID_MASTER+","+
					Table_Produse.SCOL_BUC_BOX+","+
					Table_Produse.SCOL_BLOCAT+","+
					Table_Produse.SCOL_COD_PROD+","+
					"cast(timestamp as integer) as "+Table_Produse.SCOL_PT_TIMESTAMP+","+
					Table_Produse.SCOL_SUPLIM1+","+
					Table_Produse.SCOL_SUPLIM2+","+
					Table_Produse.SCOL_SUPLIM3+","+
					Table_Produse.SCOL_SUPLIM4
					;

			public static final String[][] STR_PRODUSE = {
				{Table_Produse._ID,Types.INTREG,Table_Produse.SCOL_COD_INT,STypes.INTREG},
				{Table_Produse.COL_ORDONARE,Types.INTREG,Table_Produse.SCOL_ORDONARE,STypes.INTREG},
				{Table_Produse.COL_COD_PROD,Types.TEXT ,Table_Produse.SCOL_COD_PROD,STypes.STRING},
				{Table_Produse.COL_DENUMIRE,Types.TEXT,Table_Produse.SCOL_DENUMIRE,STypes.STRING},
				{Table_Produse.COL_PRET_FARA,Types.VALOARE,Table_Produse.SCOL_PRET_FARA,STypes.VALOARE},
				{Table_Produse.COL_PRET_CU,Types.VALOARE,Table_Produse.SCOL_PRET_CU,STypes.VALOARE},
				{Table_Produse.COL_PRET_FARA1,Types.VALOARE,Table_Produse.SCOL_PRET_FARA1,STypes.VALOARE},
				{Table_Produse.COL_PRET_CU1,Types.VALOARE,Table_Produse.SCOL_PRET_CU1,STypes.VALOARE},
				{Table_Produse.COL_COTA_TVA,Types.VALOARE,Table_Produse.SCOL_COTA_TVA,STypes.VALOARE},
				{Table_Produse.COL_BLOCAT,Types.INTREG,Table_Produse.SCOL_BLOCAT,STypes.INTREG},
				{Table_Produse.COL_ID_MASTER,Types.INTREG,Table_Produse.SCOL_ID_MASTER,STypes.INTREG},
				{Table_Produse.COL_ID_TIP,Types.INTREG,Table_Produse.SCOL_ID_TIP,STypes.INTREG},
				{Table_Produse.COL_BUC_BOX,Types.INTREG,Table_Produse.SCOL_BUC_BOX,STypes.INTREG},
				{Table_Produse.COL_S_TIMESTAMP,Types.INTREG},
				{Table_Produse.COL_SUPLIM1,Types.TEXT,Table_Produse.SCOL_SUPLIM1, STypes.STRING},
				{Table_Produse.COL_SUPLIM2,Types.TEXT,Table_Produse.SCOL_SUPLIM2, STypes.STRING},
				{Table_Produse.COL_SUPLIM3,Types.TEXT,Table_Produse.SCOL_SUPLIM3, STypes.STRING},
				{Table_Produse.COL_SUPLIM4,Types.TEXT,Table_Produse.SCOL_SUPLIM4, STypes.STRING}
			};
			
			
			public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Produse.TABLE_NAME;
			public static final String SQL_CREATE_ENTRIES=
					"CREATE TABLE IF NOT EXISTS " +
					Table_Produse.TABLE_NAME + " ( " +
					Table_Produse._ID+Types.PRIMARY+" , "+
					Table_Produse.COL_ORDONARE+Types.INTREG+" , "+
					Table_Produse.COL_COD_PROD+Types.TEXT+" , "+
					Table_Produse.COL_DENUMIRE+Types.TEXT+" , "+
					Table_Produse.COL_PRET_FARA+Types.VALOARE+" , "+
					Table_Produse.COL_PRET_CU+Types.VALOARE+" , "+
					Table_Produse.COL_PRET_FARA1+Types.VALOARE+" , "+
					Table_Produse.COL_PRET_CU1+Types.VALOARE+" , "+
					Table_Produse.COL_COTA_TVA+Types.VALOARE+" , "+
					Table_Produse.COL_BLOCAT+Types.INTREG+" , "+
					Table_Produse.COL_S_TIMESTAMP+Types.INTREG +" , "+
					Table_Produse.COL_C_TIMESTAMP+Types.TEXT+" , "+
					Table_Produse.COL_ID_MASTER+Types.INTREG+ " , "+
					Table_Produse.COL_ID_TIP+Types.INTREG+" , "+
					Table_Produse.COL_BUC_BOX+Types.INTREG+" , "+
					Table_Produse.COL_SUPLIM1+Types.TEXT+" , "+
					Table_Produse.COL_SUPLIM2+Types.TEXT+" , "+
					Table_Produse.COL_SUPLIM3+Types.TEXT+" , "+
					Table_Produse.COL_SUPLIM4+Types.TEXT+
					" ) ";
		}

		public static abstract class Table_GenUnic implements BaseColumns {
			public static final String TABLE_NAME = "genunic";
			public static final String COL_FAKE="fake";
			public static final String COL_C_TIMESTAMP="c_timestamp";
			
			public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+ Table_GenUnic.TABLE_NAME;
			public static final String SQL_CREATE_ENTRIES=
				"CREATE TABLE IF NOT EXISTS " +
				Table_GenUnic.TABLE_NAME + " ( " +
				Table_GenUnic._ID+Types.PRIMARY_AUTO+" , "+
				Table_GenUnic.COL_FAKE+Types.INTREG+" , "+
				Table_GenUnic.COL_C_TIMESTAMP+Types.TIMESTAMP+
				" )" ; 
		}
				
			public static abstract class Table_Rute implements BaseColumns {
				public static final String TABLE_NAME = "rute";
                public static final String COL_ID_DEVICE="id_device";
                public static final String COL_ID_AGENT="id_agent";
				public static final String COL_DENUMIRE="denumire";
				public static final String COL_COD_RUTA="cod_ruta";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";

                public static final String STABLE_NAME="rute";
                public static final String SCOL_COD_INT="cod_int";
                public static final String SCOL_ID_DEVICE="id_device";
                public static final String SCOL_ID_AGENT="id_agent";
                public static final String SCOL_DENUMIRE="denumire";
                public static final String SCOL_COD_RUTA="cod_ruta";
                public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Rute.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Rute.TABLE_NAME + " ( " +
						Table_Rute._ID+Types.PRIMARY+" , "+
                        Table_Rute.COL_ID_DEVICE+Types.INTREG+ " , "+
                        Table_Rute.COL_ID_AGENT+Types.INTREG+ " , "+
						Table_Rute.COL_DENUMIRE+Types.TEXT+" , "+
						Table_Rute.COL_COD_RUTA+Types.TEXT+" , "+
						Table_Rute.COL_S_TIMESTAMP+Types.INTREG+" , "+
						Table_Rute.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
                public static final String[][] STR_RUTE = {
                        {Table_Rute._ID, Types.INTREG, Table_Rute.SCOL_COD_INT, STypes.INTREG},
                        {Table_Rute.COL_ID_DEVICE, Types.INTREG, Table_Rute.SCOL_ID_DEVICE, STypes.INTREG},
                        {Table_Rute.COL_ID_AGENT, Types.INTREG, Table_Rute.SCOL_ID_AGENT, STypes.INTREG},
                        {Table_Rute.COL_DENUMIRE, Types.TEXT, Table_Rute.SCOL_DENUMIRE, STypes.STRING},
                        {Table_Rute.COL_COD_RUTA, Types.TEXT, Table_Rute.SCOL_COD_RUTA, STypes.STRING},
                        {Table_Rute.COL_S_TIMESTAMP, Types.INTREG}
                };
			}
			
			public static abstract class Table_Pozitii implements BaseColumns {
				public static final String TABLE_NAME = "pozitii";
				public static final String COL_ID_ANTET="id_antet";
				public static final String COL_ID_PRODUS="id_produs";
				public static final String COL_CANTITATE="cantitate";
				public static final String COL_PRET_FARA="pret_fara";
				public static final String COL_PRET_CU="pret_cu";
				public static final String COL_VAL_FARA="val_cu";
				public static final String COL_PROC_RED="proc_red";
				public static final String COL_VAL_RED="val_red";
				public static final String COL_TVA_RED="tva_red";
				public static final String COL_COTA_TVA="cota_tva";
				public static final String COL_BAZA_TVA="baza_tva";
				public static final String COL_VAL_TVA="val_tva";
				public static final String COL_TRANSMIS="transmis";
				public static final String COL_ID_UM="id_um";
                public static final String COL_ID_FA1="id_fa1";
                public static final String COL_ID_FA2="id_fa2";
                public static final String COL_BONUS="bonus";
				public static final String COL_PRET_FARA1="pret_fara1"; // pentru casa de marcat
				public static final String COL_PRET_CU1="pret_cu1";				
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
			
				public static final String STABLE_NAME="pozitii";
				public static final String SCOL_ID_ANTET="id_antet";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ID_PRODUS="id_produs";
				public static final String SCOL_CANTITATE="cantitate";
				public static final String SCOL_PRET_FARA="pret_fara";
				public static final String SCOL_PRET_CU="pret_cu";
				public static final String SCOL_VAL_FARA="val_fara";
				public static final String SCOL_PROC_RED="proc_red";
				public static final String SCOL_VAL_RED="val_red";
				public static final String SCOL_TVA_RED="tva_red";
				public static final String SCOL_COTA_TVA="cota_tva";
				public static final String SCOL_BAZA_TVA="baza_tva";
				public static final String SCOL_VAL_TVA="val_tva";
				public static final String SCOL_PRET_FARA1="pret_fara1"; // pentru casa de marcat
				public static final String SCOL_PRET_CU1="pret_cu1";
				public static final String SCOL_TRANSMIS="transmis";
				public static final String SCOL_ID_UM="id_um";
                public static final String SCOL_ID_FA1="id_fa1";
                public static final String SCOL_ID_FA2="id_fa2";
                public static final String SCOL_BONUS="bonus";
				public static final String[][] STR_POZITII = {
					{Table_Pozitii._ID,Types.INTREG,Table_Pozitii.SCOL_COD_INT,STypes.INTREG},
					{Table_Pozitii.COL_BAZA_TVA,Types.VALOARE,Table_Pozitii.SCOL_BAZA_TVA,STypes.VALOARE},
					{Table_Pozitii.COL_CANTITATE,Types.VALOARE,Table_Pozitii.SCOL_CANTITATE,STypes.VALOARE},
					{Table_Pozitii.COL_COTA_TVA,Types.VALOARE,Table_Pozitii.SCOL_COTA_TVA,STypes.VALOARE},
					{Table_Pozitii.COL_ID_ANTET,Types.INTREG,Table_Pozitii.SCOL_ID_ANTET,STypes.INTREG},
					{Table_Pozitii.COL_ID_PRODUS,Types.INTREG,Table_Pozitii.SCOL_ID_PRODUS,STypes.INTREG},
					{Table_Pozitii.COL_ID_UM,Types.INTREG,Table_Pozitii.SCOL_ID_UM,STypes.INTREG},
					{Table_Pozitii.COL_PRET_CU,Types.VALOARE,Table_Pozitii.SCOL_PRET_CU,STypes.VALOARE},
					{Table_Pozitii.COL_PRET_FARA,Types.VALOARE,Table_Pozitii.SCOL_PRET_FARA,STypes.VALOARE},
					{Table_Pozitii.COL_PRET_CU1,Types.VALOARE,Table_Pozitii.SCOL_PRET_CU1,STypes.VALOARE},
					{Table_Pozitii.COL_PRET_FARA1,Types.VALOARE,Table_Pozitii.SCOL_PRET_FARA1,STypes.VALOARE},
					{Table_Pozitii.COL_PROC_RED,Types.VALOARE,Table_Pozitii.SCOL_PROC_RED,STypes.VALOARE},
					{Table_Pozitii.COL_TVA_RED,Types.VALOARE,Table_Pozitii.SCOL_TVA_RED,STypes.VALOARE},
					{Table_Pozitii.COL_VAL_FARA,Types.VALOARE,Table_Pozitii.SCOL_VAL_FARA,STypes.VALOARE},
					{Table_Pozitii.COL_VAL_RED,Types.VALOARE,Table_Pozitii.SCOL_VAL_RED,STypes.VALOARE},
					{Table_Pozitii.COL_VAL_TVA,Types.VALOARE,Table_Pozitii.SCOL_VAL_TVA,STypes.VALOARE},
                    {Table_Pozitii.COL_ID_FA1,Types.INTREG,Table_Pozitii.SCOL_ID_FA1,STypes.INTREG},
                    {Table_Pozitii.COL_ID_FA2,Types.INTREG,Table_Pozitii.SCOL_ID_FA2,STypes.INTREG},
                    {Table_Pozitii.COL_BONUS,Types.INTREG,Table_Pozitii.SCOL_BONUS,STypes.INTREG}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Pozitii.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Pozitii.TABLE_NAME + " ( " +
						Table_Pozitii._ID+Types.PRIMARY+" , "+
						Table_Pozitii.COL_ID_ANTET+Types.INTREG+" , "+
						Table_Pozitii.COL_ID_PRODUS+Types.INTREG+" , "+
						Table_Pozitii.COL_CANTITATE+Types.VALOARE+" , "+
						Table_Pozitii.COL_PRET_FARA+Types.VALOARE+" , "+
						Table_Pozitii.COL_PRET_CU+Types.VALOARE+" , "+
						Table_Pozitii.COL_PRET_FARA1+Types.VALOARE+" , "+
						Table_Pozitii.COL_PRET_CU1+Types.VALOARE+" , "+
						Table_Pozitii.COL_VAL_FARA+Types.VALOARE+" , "+
						Table_Pozitii.COL_PROC_RED+Types.VALOARE+" , "+
						Table_Pozitii.COL_VAL_RED+Types.VALOARE+" , "+
						Table_Pozitii.COL_TVA_RED+Types.VALOARE+" , "+
						Table_Pozitii.COL_COTA_TVA+Types.VALOARE+" , "+
						Table_Pozitii.COL_BAZA_TVA+Types.VALOARE+" , "+
						Table_Pozitii.COL_VAL_TVA+Types.VALOARE+" , "+
						Table_Pozitii.COL_TRANSMIS+Types.INTREG+" , "+
						Table_Pozitii.COL_ID_UM+Types.INTREG+" , "+
                        Table_Pozitii.COL_ID_FA1+Types.INTREG+" , "+
                        Table_Pozitii.COL_ID_FA2+Types.INTREG+" , "+
                        Table_Pozitii.COL_BONUS+Types.INTREG+" , "+
						Table_Pozitii.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Pozitii.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
				
			}
			
			public static abstract class Table_Agent implements BaseColumns {
				public static final String TABLE_NAME = "agent";
				public static final String COL_COD_AG="cod_agent";
				public static final String COL_DENUMIRE="denumire";
				public static final String COL_CNP="cnp";
				public static final String COL_BI_SERIE="bi_serie";
				public static final String COL_BI_NUMAR="bi_numar";
				public static final String COL_ID_MASINA="id_masina";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_ACTIV="activ"; // se foloseste pt comenzi online si denumirea lui se afiseaza in meniul de inceput
				public static final String COL_SETARI="setari";
				public static final String COL_C_TIMESTAMP="flag";
				
				public static final String STABLE_NAME = "agent";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_COD_AG="cod_agent";
				public static final String SCOL_DENUMIRE="denumire";
				public static final String SCOL_CNP="cnp";
				public static final String SCOL_BI_SERIE="bi_serie";
				public static final String SCOL_BI_NUMAR="bi_numar";
				public static final String SCOL_ID_MASINA="id_masina";
				public static final String SCOL_SETARI="setari";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";
				
				public static String[][] STR_AGENT= {
					{Table_Agent._ID,Types.INTREG,Table_Agent.SCOL_COD_INT,STypes.INTREG},
					{Table_Agent.COL_BI_NUMAR,Types.TEXT},
					{Table_Agent.COL_BI_SERIE,Types.TEXT},
					{Table_Agent.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_Agent.COL_CNP,Types.TEXT},
					{Table_Agent.COL_ACTIV,Types.INTREG},
					{Table_Agent.COL_COD_AG,Types.TEXT,Table_Agent.SCOL_COD_AG,STypes.STRING},
					{Table_Agent.COL_DENUMIRE,Types.TEXT,Table_Agent.SCOL_DENUMIRE,STypes.STRING},
					{Table_Agent.COL_ID_MASINA,Types.INTREG,Table_Agent.SCOL_ID_MASINA,STypes.STRING},
						{Table_Agent.COL_SETARI,Types.TEXT,Table_Agent.SCOL_SETARI,STypes.STRING},
					{Table_Agent.COL_S_TIMESTAMP,Types.INTREG}
				};
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Agent.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Agent.TABLE_NAME + " ( " +
						Table_Agent._ID+Types.PRIMARY+" , "+
						Table_Agent.COL_COD_AG+Types.TEXT+" , "+
						Table_Agent.COL_DENUMIRE+Types.TEXT+" , "+
						Table_Agent.COL_CNP+Types.TEXT+" , "+
						Table_Agent.COL_BI_SERIE+Types.TEXT+" , "+
						Table_Agent.COL_BI_NUMAR+Types.TEXT+" , "+
						Table_Agent.COL_ID_MASINA+Types.INTREG+" , "+
                                Table_Agent.COL_SETARI+Types.TEXT+" , "+
						Table_Agent.COL_ACTIV+Types.INTREG+" , "+
						Table_Agent.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Agent.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
			}
			
			// table_name , col_s_timestamp, col_c_timestamp,stable_name,scol_pt_timestamp,
			// col_sincro_server , sql_drop_table, sql_create_entries			
			// "cast(timestamp as integer) as "+Table_Produse.SCOL_PT_TIMESTAMP;
			public static abstract class Table_Clienti implements BaseColumns {
				public static final String TABLE_NAME = "clienti";
				public static final String COL_ORDONARE="ordonare";
				public static final String COL_ID_PART="id_part";
				public static final String COL_DENUMIRE="denumire";
				public static final String COL_NR_FISc="nr_fisc";
				public static final String COL_NR_RC="nr_rc";
				public static final String COL_JUDET="judet";
				public static final String COL_LOC="loc";
				public static final String COL_ADRESA="adresa";
				public static final String COL_TEL1="tel1";
				public static final String COL_TEL2="tel2";
				public static final String COL_CONTACT="contact";
				public static final String COL_BANCA="BANCA";
				public static final String COL_CONT="cont";
				public static final String COL_ID_ZONA="id_zona";
				public static final String COL_ID_RUTA="id_ruta";
				public static final String COL_BLOCAT="blocat";
				public static final String COL_BLOCAT_VANZARE="blocat_vanzare";
				public static final String COL_PROC_RED="proc_red";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
				
				public static final String STABLE_NAME="client";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ORDONARE="ordonare";
				public static final String SCOL_ID_PART="id_part";
				public static final String SCOL_DENUMIRE="denumire";
				public static final String SCOL_NR_FISC="nr_fisc";
				public static final String SCOL_NR_RC="nr_rc";
				public static final String SCOL_JUDET="judet";
				public static final String SCOL_LOC="loc";
				public static final String SCOL_ADRESA="adresa";
				public static final String SCOL_TEL1="tel1";
				public static final String SCOL_TEL2="tel2";
				public static final String SCOL_CONTACT="contact";
				public static final String SCOL_BANCA="banca";
				public static final String SCOL_CONT="cont";
				public static final String SCOL_ID_ZONA="id_zona";
				public static final String SCOL_BLOCAT="blocat";
				public static final String SCOL_PROC_RED="proc_red";
				public static final String SCOL_BLOCAT_VANZARE="blocat_vanzare";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

				public static final String[][] STR_CLIENTI= {
					{Table_Clienti._ID,Types.INTREG,Table_Clienti.SCOL_COD_INT,STypes.INTREG },
					{Table_Clienti.COL_ADRESA,Types.TEXT,Table_Clienti.SCOL_ADRESA,STypes.STRING},
					{Table_Clienti.COL_BANCA,Types.TEXT,Table_Clienti.SCOL_BANCA,STypes.STRING},
					{Table_Clienti.COL_BLOCAT,Types.INTREG,Table_Clienti.SCOL_BLOCAT,STypes.INTREG},
					{Table_Clienti.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_Clienti.COL_CONT,Types.TEXT,Table_Clienti.SCOL_CONT,STypes.STRING},
					{Table_Clienti.COL_CONTACT,Types.TEXT,Table_Clienti.SCOL_CONTACT,STypes.STRING},
					{Table_Clienti.COL_DENUMIRE,Types.TEXT,Table_Clienti.SCOL_DENUMIRE,STypes.STRING},
					{Table_Clienti.COL_ID_PART,Types.TEXT,Table_Clienti.SCOL_ID_PART,STypes.STRING},
					{Table_Clienti.COL_ID_RUTA,Types.INTREG}, // ruta se da la client_agent
					{Table_Clienti.COL_JUDET,Types.TEXT,Table_Clienti.SCOL_JUDET,STypes.STRING},
					{Table_Clienti.COL_LOC,Types.TEXT,Table_Clienti.SCOL_LOC,STypes.STRING},
					{Table_Clienti.COL_NR_FISc,Types.TEXT,Table_Clienti.SCOL_NR_FISC,STypes.STRING},
					{Table_Clienti.COL_NR_RC,Types.TEXT,Table_Clienti.SCOL_NR_RC,STypes.STRING},
					{Table_Clienti.COL_ORDONARE,Types.INTREG},
					{Table_Clienti.COL_S_TIMESTAMP,Types.INTREG},
					{Table_Clienti.COL_TEL1,Types.TEXT,Table_Clienti.SCOL_TEL1,STypes.STRING},
					{Table_Clienti.COL_BLOCAT_VANZARE,Types.INTREG,Table_Clienti.SCOL_BLOCAT_VANZARE,STypes.INTREG},
					{Table_Clienti.COL_TEL2,Types.TEXT,Table_Clienti.SCOL_TEL2,STypes.STRING},
					{Table_Clienti.COL_PROC_RED,Types.VALOARE,Table_Clienti.SCOL_PROC_RED,STypes.VALOARE}
				};
				
				public static final String COL_SINCRO_SERVER=
						Table_Clienti.SCOL_COD_INT+","+
						Table_Clienti.SCOL_ORDONARE+","+
						Table_Clienti.SCOL_ID_PART+","+
						Table_Clienti.SCOL_DENUMIRE+","+
						Table_Clienti.SCOL_NR_FISC+","+
						Table_Clienti.SCOL_NR_RC+","+
						Table_Clienti.SCOL_JUDET+","+
						Table_Clienti.SCOL_LOC+","+
						Table_Clienti.SCOL_ADRESA+","+
						Table_Clienti.SCOL_TEL1+","+
						Table_Clienti.SCOL_TEL2+","+
						Table_Clienti.SCOL_CONTACT+","+
						Table_Clienti.SCOL_BANCA+","+
						Table_Clienti.SCOL_CONT+","+
						Table_Clienti.SCOL_ID_ZONA+","+
						Table_Clienti.SCOL_BLOCAT+","+
						Table_Clienti.SCOL_BLOCAT_VANZARE+","+
						Table_Clienti.SCOL_PROC_RED+","+
						"cast(timestamp as integer) as "+Table_Clienti.SCOL_PT_TIMESTAMP;

				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Clienti.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Clienti.TABLE_NAME + " ( " +
						Table_Clienti._ID+Types.PRIMARY+" , "+
						Table_Clienti.COL_ORDONARE+Types.INTREG+","+
						Table_Clienti.COL_ID_PART+Types.TEXT+" , "+
						Table_Clienti.COL_DENUMIRE+Types.TEXT+" , "+
						Table_Clienti.COL_NR_FISc+Types.TEXT+" , "+
						Table_Clienti.COL_NR_RC+Types.TEXT+" , "+
						Table_Clienti.COL_JUDET+Types.TEXT+" , "+
						Table_Clienti.COL_LOC+Types.TEXT+" , "+
						Table_Clienti.COL_ADRESA+Types.TEXT+" , "+
						Table_Clienti.COL_TEL1+Types.TEXT+" , "+
						Table_Clienti.COL_TEL2+Types.TEXT+" , "+
						Table_Clienti.COL_CONTACT+Types.TEXT+" , "+
						Table_Clienti.COL_BANCA+Types.TEXT+" , "+
						Table_Clienti.COL_CONT+Types.TEXT+" , "+
						Table_Clienti.COL_ID_ZONA+Types.INTREG+" , "+
						Table_Clienti.COL_ID_RUTA+Types.INTREG+" , "+
						Table_Clienti.COL_BLOCAT+Types.INTREG+" , "+
						Table_Clienti.COL_BLOCAT_VANZARE+Types.INTREG+" , "+
						Table_Clienti.COL_PROC_RED+Types.VALOARE+" , "+
						Table_Clienti.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Clienti.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
						
			} //clienti
			
			public static abstract class Table_Antet implements BaseColumns {
				public static final String TABLE_NAME = "antet";
				public static final String COL_ID_USER="id_user";
				public static final String COL_ID_DEVICE="id_device";
				public static final String COL_ID_PART="id_part";
				public static final String COL_ID_AUTO="id_auto";
				public static final String COL_ID_AGENT="id_agent";
				public static final String COL_ID_RUTA="id_ruta";
				public static final String COL_NR_DOC="nr_doc";
				public static final String COL_DATA="data";
				public static final String COL_VAL_FARA="val_fara";
				public static final String COL_VAL_TVA="val_tva";
				public static final String COL_LISTAT="listat";
				public static final String COL_ANULAT="anulat";
				public static final String COL_DATA_SCAD="data_scad";
				public static final String COL_CORESP="coresp";
				public static final String COL_ID_TIPDOC="id_tipdoc";
				public static final String COL_TRANSMIS="transmis";
				public static final String COL_TERM_PL="term_pl";
				public static final String COL_ID_MODPL="id_modpl";
				public static final String COL_INCASAT="incasat";
                public static final String COL_NR_CHITANTA="nr_chitanta";
				public static final String COL_BLOCAT="blocat";
				public static final String COL_FARA_SOLD="fara_sold"; // daca este 1 nu se mai ia in calcul al sold
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";

				public static final String STABLE_NAME="antet";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ID_USER="id_user";
				public static final String SCOL_ID_DEVICE="id_device";
				public static final String SCOL_ID_PART="id_part";
				public static final String SCOL_ID_AUTO="id_auto";
				public static final String SCOL_ID_AGENT="id_agent";
				public static final String SCOL_ID_RUTA="id_ruta";
				public static final String SCOL_NR_DOC="nr_doc";
				public static final String SCOL_DATA="data";
				public static final String SCOL_DATA_DOC="data_doc";
				public static final String SCOL_VAL_FARA="val_fara";
				public static final String SCOL_VAL_TVA="val_tva";
				public static final String SCOL_LISTAT="listat";
				public static final String SCOL_ANULAT="anulat";
				public static final String SCOL_DATA_SCAD="data_scad";
				public static final String SCOL_CORESP="coresp";
				public static final String SCOL_ID_TIPDOC="tip_doc";
				public static final String SCOL_TRANSMIS="transmis";
                public static final String SCOL_NR_CHITNTA="nr_chitanta";
				public static final String SCOL_TERM_PL="term_pl";
				public static final String SCOL_ID_MODPL="mod_pl";
				public static final String SCOL_INCASAT="incasat";
				public static final String SCOL_BLOCAT="blocat";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";
				
				public static final String [][] STR_ANTET= {
					{Table_Antet._ID,Types.INTREG,Table_Antet.SCOL_COD_INT,STypes.INTREG},
					{Table_Antet.COL_ANULAT,Types.INTREG,Table_Antet.SCOL_ANULAT,STypes.INTREG},
					{Table_Antet.COL_CORESP,Types.TEXT,Table_Antet.SCOL_CORESP,STypes.STRING},
					{Table_Antet.COL_DATA,Types.DATE,Table_Antet.SCOL_DATA,STypes.DATA},
					{Table_Antet.COL_DATA,Types.DATE,Table_Antet.SCOL_DATA_DOC,STypes.DTOS},
					{Table_Antet.COL_DATA_SCAD,Types.DATE,Table_Antet.SCOL_DATA_SCAD,STypes.DATA},
					{Table_Antet.COL_ID_AGENT,Types.INTREG,Table_Antet.SCOL_ID_AGENT,STypes.INTREG},
					{Table_Antet.COL_ID_AUTO,Types.INTREG,Table_Antet.SCOL_ID_AUTO,STypes.INTREG},
					{Table_Antet.COL_ID_MODPL,Types.INTREG,Table_Antet.SCOL_ID_MODPL,STypes.INTREG},
					{Table_Antet.COL_ID_PART,Types.INTREG,Table_Antet.SCOL_ID_PART,STypes.INTREG},
					{Table_Antet.COL_ID_RUTA,Types.INTREG,Table_Antet.SCOL_ID_RUTA,STypes.INTREG},
					{Table_Antet.COL_ID_TIPDOC,Types.INTREG,Table_Antet.SCOL_ID_TIPDOC,STypes.INTREG},
					{Table_Antet.COL_ID_USER,Types.INTREG,Table_Antet.SCOL_ID_USER,STypes.INTREG},
					{Table_Antet.COL_INCASAT,Types.VALOARE,Table_Antet.SCOL_INCASAT,STypes.VALOARE},
					{Table_Antet.COL_LISTAT,Types.INTREG,Table_Antet.SCOL_LISTAT,STypes.INTREG},
					{Table_Antet.COL_NR_DOC,Types.TEXT,Table_Antet.SCOL_NR_DOC,STypes.STRING},
					{Table_Antet.COL_TERM_PL,Types.INTREG,Table_Antet.SCOL_TERM_PL,STypes.INTREG},
					{Table_Antet.COL_VAL_FARA,Types.VALOARE,Table_Antet.SCOL_VAL_FARA,STypes.VALOARE},
					{Table_Antet.COL_VAL_TVA,Types.VALOARE,Table_Antet.SCOL_VAL_TVA,STypes.VALOARE},
                    {Table_Antet.COL_NR_CHITANTA,Types.TEXT,Table_Antet.SCOL_NR_CHITNTA,STypes.STRING},
					{Table_Antet.COL_BLOCAT,Types.INTREG,Table_Antet.SCOL_BLOCAT,STypes.INTREG},
					{Table_Antet.COL_FARA_SOLD,Types.INTREG,"",""}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Antet.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES="CREATE TABLE IF NOT EXISTS " +
						Table_Antet.TABLE_NAME + " ( " +
						Table_Antet._ID+Types.PRIMARY+" , "+
						Table_Antet.COL_ID_USER+Types.INTREG+" , "+
						Table_Antet.COL_ID_DEVICE+Types.INTREG+" , "+
						Table_Antet.COL_ID_PART+Types.INTREG+" , "+
						Table_Antet.COL_ID_AUTO+Types.INTREG+" , "+
						Table_Antet.COL_ID_AGENT+Types.INTREG+" , "+
						Table_Antet.COL_ID_RUTA+Types.INTREG+" , "+
						Table_Antet.COL_NR_DOC+Types.TEXT+" , "+
						Table_Antet.COL_DATA+Types.TIMESTAMP+" , "+
						Table_Antet.COL_VAL_FARA+Types.VALOARE+" , "+
						Table_Antet.COL_VAL_TVA+Types.VALOARE+" , "+
						Table_Antet.COL_LISTAT+Types.INTREG+" , "+
						Table_Antet.COL_ANULAT+Types.INTREG+" , "+
						Table_Antet.COL_DATA_SCAD+Types.DATE+" , "+
						Table_Antet.COL_CORESP+Types.TEXT+" , "+
						Table_Antet.COL_ID_TIPDOC+Types.INTREG+" , "+
						Table_Antet.COL_TRANSMIS+Types.INTREG+" , "+
						Table_Antet.COL_TERM_PL+Types.INTREG+" , "+
						Table_Antet.COL_ID_MODPL+Types.INTREG+" , "+
						Table_Antet.COL_INCASAT+Types.VALOARE+" , "+
						Table_Antet.COL_BLOCAT+Types.INTREG+" , "+
                        Table_Antet.COL_NR_CHITANTA+Types.TEXT+" , "+
						Table_Antet.COL_FARA_SOLD+Types.INTREG+" , "+
						Table_Antet.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Antet.COL_C_TIMESTAMP+Types.TEXT +
						" )";
				
			}
			public static abstract class Table_Sablon_Antet implements BaseColumns {
				public static final String TABLE_NAME = "sablon_antet";
				public static final String COL_ID_USER="id_user";
				public static final String COL_ID_DEVICE="id_device";
				public static final String COL_ID_PART="id_part";
				public static final String COL_ID_AGENT="id_agent";
				public static final String COL_ID_RUTA="id_ruta";
				public static final String COL_ID_CURSA="id_cursa";
				public static final String COL_DATA="data";
				public static final String COL_ID_TIPDOC="id_tipdoc";
				public static final String COL_TRANSMIS="transmis";
				public static final String COL_BLOCAT="blocat";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";

				public static final String STABLE_NAME="sablon_antet";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ID_USER="id_user";
				public static final String SCOL_ID_DEVICE="id_device";
				public static final String SCOL_ID_PART="id_part";
				public static final String SCOL_ID_AGENT="id_agent";
				public static final String SCOL_ID_RUTA="id_ruta";
				public static final String SCOL_ID_CURSA="id_cursa";
				public static final String SCOL_DATA="data";
				public static final String SCOL_DATA_DOC="data_doc";
				public static final String SCOL_ID_TIPDOC="id_tipdoc";
				public static final String SCOL_TRANSMIS="transmis";
				public static final String SCOL_BLOCAT="blocat";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";
				
				public static final String [][] STR_SABLON_ANTET= {
					{Table_Sablon_Antet._ID,Types.INTREG,Table_Sablon_Antet.SCOL_COD_INT,STypes.INTREG},
					{Table_Sablon_Antet.COL_DATA,Types.DATE,Table_Sablon_Antet.SCOL_DATA,STypes.DATA},
					{Table_Sablon_Antet.COL_DATA,Types.DATE,Table_Sablon_Antet.SCOL_DATA_DOC,STypes.DTOS},
					{Table_Sablon_Antet.COL_ID_AGENT,Types.INTREG,Table_Sablon_Antet.SCOL_ID_AGENT,STypes.INTREG},
					{Table_Sablon_Antet.COL_ID_PART,Types.INTREG,Table_Sablon_Antet.SCOL_ID_PART,STypes.INTREG},
					{Table_Sablon_Antet.COL_ID_RUTA,Types.INTREG,Table_Sablon_Antet.SCOL_ID_RUTA,STypes.INTREG},
					{Table_Sablon_Antet.COL_ID_CURSA,Types.INTREG,Table_Sablon_Antet.SCOL_ID_CURSA,STypes.INTREG},
					{Table_Sablon_Antet.COL_ID_TIPDOC,Types.INTREG,Table_Sablon_Antet.SCOL_ID_TIPDOC,STypes.INTREG},
					{Table_Sablon_Antet.COL_ID_USER,Types.INTREG,Table_Sablon_Antet.SCOL_ID_USER,STypes.INTREG},
					{Table_Sablon_Antet.COL_BLOCAT,Types.INTREG,Table_Sablon_Antet.SCOL_BLOCAT,STypes.INTREG}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Sablon_Antet.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES="CREATE TABLE IF NOT EXISTS " +
						Table_Sablon_Antet.TABLE_NAME + " ( " +
						Table_Sablon_Antet._ID+Types.PRIMARY+" , "+
						Table_Sablon_Antet.COL_ID_USER+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_ID_DEVICE+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_ID_PART+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_ID_AGENT+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_ID_RUTA+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_ID_CURSA+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_DATA+Types.TIMESTAMP+" , "+
						Table_Sablon_Antet.COL_ID_TIPDOC+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_TRANSMIS+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_BLOCAT+Types.INTREG+" , "+
						Table_Sablon_Antet.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Sablon_Antet.COL_C_TIMESTAMP+Types.TEXT +
						" )";
				
			}
			public static abstract class Table_Sablon_Pozitii implements BaseColumns {
				public static final String TABLE_NAME = "sablon_pozitii";
				public static final String COL_ID_ANTET="id_antet";
				public static final String COL_ID_PRODUS="id_produs";
				public static final String COL_CANTITATE="cantitate";
				public static final String COL_DIFERENTE="diferente";
				public static final String COL_TRANSMIS="transmis";
				public static final String COL_STERS="sters";
				public static final String COL_ID_UM="id_um";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
			
				public static final String STABLE_NAME="sablon_pozitii";
				public static final String SCOL_ID_ANTET="id_antet";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ID_PRODUS="id_produs";
				public static final String SCOL_CANTITATE="cantitate";
				public static final String SCOL_DIFERENTE="diferente";
				public static final String SCOL_TRANSMIS="transmis";
				public static final String SCOL_STERS="sters";
				public static final String SCOL_ID_UM="id_um";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

				public static final String[][] STR_SABLON_POZITII = {
					{Table_Sablon_Pozitii._ID,Types.INTREG,Table_Sablon_Pozitii.SCOL_COD_INT,STypes.INTREG},
					{Table_Sablon_Pozitii.COL_CANTITATE,Types.VALOARE,Table_Sablon_Pozitii.SCOL_CANTITATE,STypes.VALOARE},
					{Table_Sablon_Pozitii.COL_DIFERENTE,Types.VALOARE,Table_Sablon_Pozitii.SCOL_DIFERENTE,STypes.VALOARE},
					{Table_Sablon_Pozitii.COL_ID_ANTET,Types.INTREG,Table_Sablon_Pozitii.SCOL_ID_ANTET,STypes.INTREG},
					{Table_Sablon_Pozitii.COL_ID_PRODUS,Types.INTREG,Table_Sablon_Pozitii.SCOL_ID_PRODUS,STypes.INTREG},
					{Table_Sablon_Pozitii.COL_ID_UM,Types.INTREG,Table_Sablon_Pozitii.SCOL_ID_UM,STypes.INTREG}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Sablon_Pozitii.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Sablon_Pozitii.TABLE_NAME + " ( " +
						Table_Sablon_Pozitii._ID+Types.PRIMARY+" , "+
						Table_Sablon_Pozitii.COL_ID_ANTET+Types.INTREG+" , "+
						Table_Sablon_Pozitii.COL_ID_PRODUS+Types.INTREG+" , "+
						Table_Sablon_Pozitii.COL_CANTITATE+Types.VALOARE+" , "+
						Table_Sablon_Pozitii.COL_DIFERENTE+Types.VALOARE+" , "+
						Table_Sablon_Pozitii.COL_TRANSMIS+Types.INTREG+" , "+
						Table_Sablon_Pozitii.COL_ID_UM+Types.INTREG+" , "+
						Table_Sablon_Pozitii.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Sablon_Pozitii.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
				
			}

			// pentru blocare cursa sa nu poata fi folosita decat o data pe zi
			// pentru cursa 0
			public static abstract class Table_Bloc_Cursa implements BaseColumns {
				public static final String TABLE_NAME = "bloc_cursa";
				public static final String COL_ID_PART="id_part";
				public static final String COL_ID_RUTA="id_ruta";
				public static final String COL_ID_CURSA="id_cursa";
				public static final String COL_DATA="data";
			
				public static final String[][] STR_BLOC_CURSA = {
					{Table_Bloc_Cursa._ID,Types.PRIMARY_AUTO,"",""},
					{Table_Bloc_Cursa.COL_ID_CURSA,Types.INTREG,"",""},
					{Table_Bloc_Cursa.COL_ID_PART,Types.INTREG,"",""},
					{Table_Bloc_Cursa.COL_ID_RUTA,Types.INTREG,"",""},
					{Table_Bloc_Cursa.COL_DATA,Types.TEXT,"",""}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Bloc_Cursa.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Bloc_Cursa.TABLE_NAME + " ( " +
						Table_Bloc_Cursa._ID+Types.PRIMARY_AUTO+" , "+
						Table_Bloc_Cursa.COL_ID_CURSA+Types.INTREG+" , "+
						Table_Bloc_Cursa.COL_ID_PART+Types.INTREG+" , "+
						Table_Bloc_Cursa.COL_ID_RUTA+Types.INTREG+" , "+
						Table_Bloc_Cursa.COL_DATA+Types.TEXT+
						" ) ";
				
			}
			
			public static abstract class Table_Client_Agent implements BaseColumns {
				public static final String TABLE_NAME = "client_agent";
				public static final String COL_ID_CLIENT="id_client";
				public static final String COL_ID_AGENT="id_agent";
				public static final String COL_BLOCAT="blocat";
				public static final String COL_ORDONARE="ordonare";
                public static final String COL_ID_RUTA="id_ruta";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
			
				public static final String STABLE_NAME="client_agent";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ID_CLIENT="id_client";
				public static final String SCOL_ID_AGENT="id_agent";
				public static final String SCOL_BLOCAT="blocat";
				public static final String SCOL_ORDONARE="ordonare";
                public static final String SCOL_ID_RUTA="id_ruta";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

				public static final String[][] STR_CLIENT_AGENT = {
					{Table_Client_Agent._ID,Types.INTREG,Table_Client_Agent.SCOL_COD_INT,STypes.INTREG},
					{Table_Client_Agent.COL_ID_CLIENT,Types.VALOARE,Table_Client_Agent.SCOL_ID_CLIENT,STypes.INTREG},
					{Table_Client_Agent.COL_ID_AGENT,Types.VALOARE,Table_Client_Agent.SCOL_ID_AGENT,STypes.INTREG},
					{Table_Client_Agent.COL_BLOCAT,Types.INTREG,Table_Client_Agent.SCOL_BLOCAT,STypes.INTREG},
					{Table_Client_Agent.COL_ORDONARE,Types.INTREG,Table_Client_Agent.SCOL_ORDONARE,STypes.INTREG},
                    {Table_Client_Agent.COL_ID_RUTA,Types.INTREG,Table_Client_Agent.SCOL_ID_RUTA,STypes.INTREG},
					{Table_Client_Agent.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_Client_Agent.COL_S_TIMESTAMP,Types.INTREG},
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Client_Agent.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Client_Agent.TABLE_NAME + " ( " +
						Table_Client_Agent._ID+Types.PRIMARY+" , "+
						Table_Client_Agent.COL_ID_CLIENT+Types.INTREG+" , "+
						Table_Client_Agent.COL_ID_AGENT+Types.INTREG+" , "+
						Table_Client_Agent.COL_BLOCAT+Types.INTREG+" , "+
                        Table_Client_Agent.COL_ID_RUTA+Types.INTREG+" , "+
						Table_Client_Agent.COL_ORDONARE+Types.INTREG+" , "+
						Table_Client_Agent.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Client_Agent.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
				
			}


			public static abstract class Table_Partener implements BaseColumns {
				public static final String TABLE_NAME = "partener";
				public static final String COL_ID_PART="id_part";
				public static final String COL_DENUMIRE="denumire";
				public static final String COL_NR_FISc="nr_fisc";
				public static final String COL_NR_RC="nr_rc";
				public static final String COL_JUDET="judet";
				public static final String COL_LOC="loc";
				public static final String COL_ADRESA="adresa";
				public static final String COL_TEL1="tel1";
				public static final String COL_TEL2="tel2";
				public static final String COL_CONTACT="contact";
				public static final String COL_BANCA="BANCA";
				public static final String COL_CONT="cont";
				public static final String COL_ID_ZONA="id_zona";
				public static final String COL_BLOCAT="blocat";
				public static final String COL_BLOCAT_VANZARE="blocat_vanzare";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
				
				public static final String STABLE_NAME="partener";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ID_PART="id_part";
				public static final String SCOL_DENUMIRE="denumire";
				public static final String SCOL_NR_FISC="nr_fisc";
				public static final String SCOL_NR_RC="nr_rc";
				public static final String SCOL_JUDET="judet";
				public static final String SCOL_LOC="loc";
				public static final String SCOL_ADRESA="adresa";
				public static final String SCOL_TEL1="tel1";
				public static final String SCOL_TEL2="tel2";
				public static final String SCOL_CONTACT="contact";
				public static final String SCOL_BANCA="banca";
				public static final String SCOL_CONT="cont";
				public static final String SCOL_ID_ZONA="id_zona";
				public static final String SCOL_BLOCAT="blocat";
				public static final String SCOL_BLOCAT_VANZARE="blocat_vanzare";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

				public static final String[][] STR_PARTENER= {
					{Table_Partener._ID,Types.INTREG,Table_Partener.SCOL_COD_INT,STypes.INTREG },
					{Table_Partener.COL_ADRESA,Types.TEXT,Table_Partener.SCOL_ADRESA,STypes.STRING},
					{Table_Partener.COL_BANCA,Types.TEXT,Table_Partener.SCOL_BANCA,STypes.STRING},
					{Table_Partener.COL_BLOCAT,Types.INTREG,Table_Partener.SCOL_BLOCAT,STypes.INTREG},
					{Table_Partener.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_Partener.COL_CONT,Types.TEXT,Table_Partener.SCOL_CONT,STypes.STRING},
					{Table_Partener.COL_CONTACT,Types.TEXT,Table_Partener.SCOL_CONTACT,STypes.STRING},
					{Table_Partener.COL_DENUMIRE,Types.TEXT,Table_Partener.SCOL_DENUMIRE,STypes.STRING},
					{Table_Partener.COL_ID_PART,Types.TEXT,Table_Partener.SCOL_ID_PART,STypes.STRING},
					{Table_Partener.COL_ID_ZONA,Types.INTREG,Table_Partener.SCOL_ID_ZONA,STypes.INTREG},
					{Table_Partener.COL_JUDET,Types.TEXT,Table_Partener.SCOL_JUDET,STypes.STRING},
					{Table_Partener.COL_LOC,Types.TEXT,Table_Partener.SCOL_LOC,STypes.STRING},
					{Table_Partener.COL_NR_FISc,Types.TEXT,Table_Partener.SCOL_NR_FISC,STypes.STRING},
					{Table_Partener.COL_NR_RC,Types.TEXT,Table_Partener.SCOL_NR_RC,STypes.STRING},
					{Table_Partener.COL_S_TIMESTAMP,Types.INTREG},
					{Table_Partener.COL_TEL1,Types.TEXT,Table_Partener.SCOL_TEL1,STypes.STRING},
					{Table_Partener.COL_TEL2,Types.TEXT,Table_Partener.SCOL_TEL2,STypes.STRING},
					{Table_Partener.COL_BLOCAT_VANZARE,Types.INTREG,Table_Partener.SCOL_BLOCAT_VANZARE,STypes.INTREG}
				};
				
				public static final String COL_SINCRO_SERVER=
						Table_Partener.SCOL_COD_INT+","+
						Table_Partener.SCOL_ID_PART+","+
						Table_Partener.SCOL_DENUMIRE+","+
						Table_Partener.SCOL_NR_FISC+","+
						Table_Partener.SCOL_NR_RC+","+
						Table_Partener.SCOL_JUDET+","+
						Table_Partener.SCOL_LOC+","+
						Table_Partener.SCOL_ADRESA+","+
						Table_Partener.SCOL_TEL1+","+
						Table_Partener.SCOL_TEL2+","+
						Table_Partener.SCOL_CONTACT+","+
						Table_Partener.SCOL_BANCA+","+
						Table_Partener.SCOL_CONT+","+
						Table_Partener.SCOL_ID_ZONA+","+
						Table_Partener.SCOL_BLOCAT+","+
						Table_Partener.SCOL_BLOCAT_VANZARE+","+
						"cast(timestamp as integer) as "+Table_Partener.SCOL_PT_TIMESTAMP;

				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Partener.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Partener.TABLE_NAME + " ( " +
						Table_Partener._ID+Types.PRIMARY+" , "+
						Table_Partener.COL_ID_PART+Types.TEXT+" , "+
						Table_Partener.COL_DENUMIRE+Types.TEXT+" , "+
						Table_Partener.COL_NR_FISc+Types.TEXT+" , "+
						Table_Partener.COL_NR_RC+Types.TEXT+" , "+
						Table_Partener.COL_JUDET+Types.TEXT+" , "+
						Table_Partener.COL_LOC+Types.TEXT+" , "+
						Table_Partener.COL_ADRESA+Types.TEXT+" , "+
						Table_Partener.COL_TEL1+Types.TEXT+" , "+
						Table_Partener.COL_TEL2+Types.TEXT+" , "+
						Table_Partener.COL_CONTACT+Types.TEXT+" , "+
						Table_Partener.COL_BANCA+Types.TEXT+" , "+
						Table_Partener.COL_CONT+Types.TEXT+" , "+
						Table_Partener.COL_ID_ZONA+Types.INTREG+" , "+
						Table_Partener.COL_BLOCAT+Types.INTREG+" , "+
						Table_Partener.COL_BLOCAT_VANZARE+Types.INTREG+" , "+
						Table_Partener.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Partener.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
						
			} //partener
			// se foloseste pentru transmiterea diferitelor mesaje intre module
			// nu are corespondent pe server
			public static abstract class Table_Mesaje implements BaseColumns {
				public static final String TABLE_NAME = "mesaje";
				public static final String COL_ID_MESAJ="id_mesaj";
				public static final String COL_TEXT_MESAJ="text_mesaj";
				public static final String COL_VAL_DOUBLE="val_double"; // valoare de test din mesaj
				public static final String COL_VAL_INTREG="val_intreg"; // valoare de test din mesaj
				public static final String COL_VAL_BOOL="val_bool"; // valoare de test din mesaj
				public static final String COL_VAL_STRING="val_string"; // valoare de test din mesaj
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
			
				public static final String STABLE_NAME="";

				public static final String[][] STR_MESAJE = {
					{Table_Mesaje._ID,Types.INTREG},
					{Table_Mesaje.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_Mesaje.COL_ID_MESAJ,Types.INTREG},
					{Table_Mesaje.COL_S_TIMESTAMP,Types.INTREG},
					{Table_Mesaje.COL_TEXT_MESAJ,Types.TEXT},
					{Table_Mesaje.COL_VAL_BOOL,Types.INTREG},
					{Table_Mesaje.COL_VAL_DOUBLE,Types.VALOARE},
					{Table_Mesaje.COL_VAL_INTREG,Types.INTREG},
					{Table_Mesaje.COL_VAL_STRING,Types.TEXT}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Mesaje.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Mesaje.TABLE_NAME + " ( " +
						Table_Mesaje._ID+Types.PRIMARY_AUTO+" , "+
						Table_Mesaje.COL_C_TIMESTAMP+Types.TIMESTAMP+" , "+
						Table_Mesaje.COL_ID_MESAJ+Types.INTREG+" , "+
						Table_Mesaje.COL_S_TIMESTAMP+Types.INTREG+" , "+
						Table_Mesaje.COL_TEXT_MESAJ+Types.TEXT+" , "+
						Table_Mesaje.COL_VAL_BOOL+Types.INTREG+" , "+
						Table_Mesaje.COL_VAL_DOUBLE+Types.VALOARE+" , "+
						Table_Mesaje.COL_VAL_INTREG+Types.INTREG+" , "+
						Table_Mesaje.COL_VAL_STRING+Types.TEXT+
						" ) ";
				
			}
			
			public static abstract class Table_Ambalaje implements BaseColumns {
				public static final String TABLE_NAME = "ambalaje";
				public static final String COL_DENUMIRE ="denumire";
				public static final String COL_BLOCAT ="blocat";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
	
				public static final String STABLE_NAME="ambalaje";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_DENUMIRE ="denumire";
				public static final String SCOL_BLOCAT ="blocat";
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

				public static final String[][] STR_AMBALAJE= {
					{Table_Ambalaje._ID,Types.INTREG,Table_Ambalaje.SCOL_COD_INT,STypes.INTREG },
					{Table_Ambalaje.COL_DENUMIRE,Types.TEXT,Table_Ambalaje.SCOL_DENUMIRE,STypes.STRING},
					{Table_Ambalaje.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_Ambalaje.COL_S_TIMESTAMP,STypes.INTREG}
				};
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Ambalaje.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_Ambalaje.TABLE_NAME + " ( " +
						Table_Ambalaje._ID+Types.PRIMARY+" , "+
						Table_Ambalaje.COL_DENUMIRE+Types.TEXT+" , "+
						Table_Ambalaje.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_Ambalaje.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
			}
			public static abstract class Table_PozAmbalaje implements BaseColumns {
				public static final String TABLE_NAME = "pozambalaje";
				public static final String COL_ID_ANTET ="id_antet";
				public static final String COL_CANTITATE_DAT="cantitate_dat";
				public static final String COL_CANTITATE_LUAT="cantitate_luat";
				public static final String COL_ID_AMBALAJ="id_ambalaj";
				public static final String COL_PRET_FARA="pret_fara";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
	
				public static final String STABLE_NAME="pozambalaje";
				public static final String SCOL_COD_INT="cod_int";
				public static final String SCOL_ID_ANTET ="id_antet";
				public static final String SCOL_CANTITATE_DAT="cantitate_dat";
				public static final String SCOL_CANTITATE_LUAT="cantitate_luat";
				public static final String SCOL_ID_AMBALAJ="id_ambalaj";
				public static final String SCOL_PRET_FARA="pret_fara";				
				public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

				public static final String[][] STR_POZAMBALAJE= {
					{Table_PozAmbalaje._ID,Types.INTREG,Table_PozAmbalaje.SCOL_COD_INT,STypes.INTREG },
					{Table_PozAmbalaje.COL_ID_ANTET,Types.INTREG,Table_PozAmbalaje.SCOL_ID_ANTET,STypes.INTREG},
					{Table_PozAmbalaje.COL_CANTITATE_DAT,Types.VALOARE,Table_PozAmbalaje.SCOL_CANTITATE_DAT,STypes.VALOARE},
					{Table_PozAmbalaje.COL_CANTITATE_LUAT,Types.VALOARE,Table_PozAmbalaje.SCOL_CANTITATE_LUAT,STypes.VALOARE},
					{Table_PozAmbalaje.COL_ID_AMBALAJ,Types.INTREG,Table_PozAmbalaje.SCOL_ID_AMBALAJ,STypes.INTREG},
					{Table_PozAmbalaje.COL_PRET_FARA,Types.VALOARE,Table_PozAmbalaje.SCOL_PRET_FARA,STypes.VALOARE},
					{Table_PozAmbalaje.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_PozAmbalaje.COL_S_TIMESTAMP,Types.INTREG}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_PozAmbalaje.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_PozAmbalaje.TABLE_NAME + " ( " +
						Table_PozAmbalaje._ID+Types.PRIMARY+" , "+
						Table_PozAmbalaje.COL_CANTITATE_DAT+Types.VALOARE+" , "+
						Table_PozAmbalaje.COL_CANTITATE_LUAT+Types.VALOARE+" , "+
						Table_PozAmbalaje.COL_ID_ANTET+Types.INTREG+" , "+
						Table_PozAmbalaje.COL_ID_AMBALAJ+Types.INTREG+" , "+
						Table_PozAmbalaje.COL_PRET_FARA+Types.VALOARE+" , "+
						Table_PozAmbalaje.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_PozAmbalaje.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
			}

			public static abstract class Table_SoldCurentAmbalaje implements BaseColumns {
				public static final String TABLE_NAME = "sold_curent_ambalaje";
				public static final String COL_ID_AMBALAJ ="id_ambalaj";
				public static final String COL_ID_CLIENT ="id_client";
				public static final String COL_ID_AGENT ="id_agent";
				public static final String COL_CANTITATE="cantitate";
				public static final String COL_DATA ="data";
				public static final String COL_S_TIMESTAMP="s_timestamp";
				public static final String COL_C_TIMESTAMP="flag";
	

				public static final String[][] STR_SOLD_AMBALAJE= {
					{Table_SoldCurentAmbalaje._ID,Types.INTREG },
					{Table_SoldCurentAmbalaje.COL_ID_AMBALAJ,Types.INTREG},
					{Table_SoldCurentAmbalaje.COL_ID_CLIENT,Types.INTREG},
					{Table_SoldCurentAmbalaje.COL_ID_AGENT,Types.INTREG},
					{Table_SoldCurentAmbalaje.COL_CANTITATE,Types.VALOARE},
					{Table_SoldCurentAmbalaje.COL_DATA,Types.DATE},
					{Table_SoldCurentAmbalaje.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_SoldCurentAmbalaje.COL_S_TIMESTAMP,Types.INTREG}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_SoldCurentAmbalaje.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_SoldCurentAmbalaje.TABLE_NAME + " ( " +
						Table_SoldCurentAmbalaje._ID+Types.PRIMARY_AUTO+" , "+
						Table_SoldCurentAmbalaje.COL_CANTITATE+Types.VALOARE+" , "+
						Table_SoldCurentAmbalaje.COL_ID_CLIENT+Types.INTREG+" , "+
						Table_SoldCurentAmbalaje.COL_ID_AGENT+Types.INTREG+" , "+
						Table_SoldCurentAmbalaje.COL_ID_AMBALAJ+Types.INTREG+" , "+
						Table_SoldCurentAmbalaje.COL_DATA+Types.DATE+" , "+
						Table_SoldCurentAmbalaje.COL_S_TIMESTAMP+Types.INTREG +" , "+
						Table_SoldCurentAmbalaje.COL_C_TIMESTAMP+Types.TEXT+
						" ) ";
			}
			
			// se foloseste pentru continutul creat la ambalaje
			// nu are corespondent pe server
			public static abstract class Table_TempPozAmbalaje implements BaseColumns {
				public static final String TABLE_NAME = "temppozambalaje";
				public static final String COL_ID_AMBALAJ="id_ambalaj";
				public static final String COL_CANTITATE_DAT="cantitate_dat";
				public static final String COL_CANTITATE_LUAT="cantitate_luat";
			
				public static final String STABLE_NAME="";

				public static final String[][] STR_TEMPPOZAMBALAJE = {
					{Table_TempPozAmbalaje._ID,Types.INTREG},
					{Table_TempPozAmbalaje.COL_ID_AMBALAJ,Types.TIMESTAMP},
					{Table_TempPozAmbalaje.COL_CANTITATE_DAT,Types.VALOARE},
					{Table_TempPozAmbalaje.COL_CANTITATE_LUAT,Types.VALOARE}
				};
				
				public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_TempPozAmbalaje.TABLE_NAME;
				public static final String SQL_CREATE_ENTRIES=
						"CREATE TABLE IF NOT EXISTS " +
						Table_TempPozAmbalaje.TABLE_NAME + " ( " +
						Table_TempPozAmbalaje._ID+Types.PRIMARY_AUTO+" , "+
						Table_TempPozAmbalaje.COL_CANTITATE_DAT+Types.VALOARE+" , "+
						Table_TempPozAmbalaje.COL_CANTITATE_LUAT+Types.VALOARE+" , "+
						Table_TempPozAmbalaje.COL_ID_AMBALAJ+Types.INTREG+
						" ) ";
				
			}

		public static abstract class Table_Cod_Bare implements BaseColumns {
			public static final String TABLE_NAME = "cod_bare";
			public static final String COL_ID_CLIENT="id_part";
			public static final String COL_ID_PRODUS="id_produs";
			public static final String COL_COD="cod";
			public static final String COL_S_TIMESTAMP="s_timestamp";
			public static final String COL_C_TIMESTAMP="flag";

			public static final String STABLE_NAME="cod_bare";
			public static final String SCOL_COD_INT="cod_int";
			public static final String SCOL_ID_CLIENT="id_part";
			public static final String SCOL_ID_PRODUS="id_produs";
			public static final String SCOL_COD="cod";
			public static final String SCOL_PT_TIMESTAMP="pt_timestamp";

			public static final String[][] STR_COD_BARE = {
					{Table_Cod_Bare._ID,Types.INTREG,Table_Cod_Bare.SCOL_COD_INT,STypes.INTREG},
					{Table_Cod_Bare.COL_ID_CLIENT,Types.VALOARE,Table_Cod_Bare.SCOL_ID_CLIENT,STypes.INTREG},
					{Table_Cod_Bare.COL_ID_PRODUS,Types.VALOARE,Table_Cod_Bare.SCOL_ID_PRODUS,STypes.INTREG},
					{Table_Cod_Bare.COL_COD,Types.TEXT,Table_Cod_Bare.SCOL_COD,STypes.STRING},
					{Table_Cod_Bare.COL_C_TIMESTAMP,Types.TIMESTAMP},
					{Table_Cod_Bare.COL_S_TIMESTAMP,Types.INTREG},
			};

			public static final String SQL_DROP_TABLE="DROP TABLE IF EXISTS "+Table_Cod_Bare.TABLE_NAME;
			public static final String SQL_CREATE_ENTRIES=
					"CREATE TABLE IF NOT EXISTS " +
							Table_Cod_Bare.TABLE_NAME + " ( " +
							Table_Cod_Bare._ID+Types.PRIMARY+" , "+
							Table_Cod_Bare.COL_ID_CLIENT+Types.INTREG+" , "+
							Table_Cod_Bare.COL_ID_PRODUS+Types.INTREG+" , "+
							Table_Cod_Bare.COL_COD+Types.TEXT+" , "+
							Table_Cod_Bare.COL_S_TIMESTAMP+Types.INTREG +" , "+
							Table_Cod_Bare.COL_C_TIMESTAMP+Types.TEXT+
							" ) ";

		}



	}
