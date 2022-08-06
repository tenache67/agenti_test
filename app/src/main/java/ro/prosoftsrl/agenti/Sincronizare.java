// pentru adaugare in server
// se apeleaza adaugInServer(tabela_locala,tabela_server,structura,filtru suplimentar)
//		filtrul de timestamp=A se subintelege
// pentru preluare din server pe baza de timnestamp

package ro.prosoftsrl.agenti;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.*;
import ro.prosoftsrl.diverse.Siruri;
import ro.prosoftsrl.sqlserver.MySQLDBadapter;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class Sincronizare {
	public MySQLDBadapter sqldb;
	public SQLiteDatabase db ; // este cu getwritable
	int idDevice;

    //preia stocul initial zilnic pentru varianta fara descarcare masina
    // stocul initial apare ca un aviz de incarcare generat

    public void sincroPreiaStocIniZi (int idDevice,Context context) {
        // sterge avizul anterior
        int nBlocat=0 ; // se preia flagul blocat de la imaginea anterioara
        String sCmd="";
/*
        sCmd=" DELETE FROM " +
                Table_Pozitii.TABLE_NAME +
                "WHERE "+Table_Pozitii.COL_ID_ANTET +" IN ("+
                " SELECT "+Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" FROM "+Table_Antet.TABLE_NAME+
                " WHERE "+"Trim("+Table_Antet.COL_CORESP+")='STOCINIZI')";
*/


        sCmd="select "+Table_Antet._ID+","+Table_Antet.COL_BLOCAT+" from "+Table_Antet.TABLE_NAME +" where " +
                "Trim("+Table_Antet.COL_CORESP+")='STOCINIZI' and "+"substr("+Table_Antet.COL_DATA+",1,10)"+"='"+
                Siruri.dtos(Siruri.getDateTime(),"-")+"'";
        Log.d("PRO&","Extrage pt sterg aviz : "+sCmd);

		try {
			Cursor crs=db.rawQuery(sCmd,null);
			crs.moveToFirst();
			db.beginTransaction();
			while (!crs.isAfterLast()) {
                Log.d("PRO&","Inainte de styerg aviz.");
                db.delete(Table_Antet.TABLE_NAME, "_id="+crs.getLong(crs.getColumnIndex(Table_Antet._ID)),null);
                db.delete(Table_Pozitii.TABLE_NAME, Table_Pozitii.COL_ID_ANTET+"="+crs.getLong(crs.getColumnIndex(Table_Antet._ID)), null);
                crs.moveToNext();
            }
			db.setTransactionSuccessful();
			db.endTransaction();
			crs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


		Calendar c=Siruri.getDateTime();
        c.add(Calendar.DATE, -1);
        sCmd="EXECUTE det_stoc_zi_agent '"+Siruri.dtos(c)+"',"+idDevice;
        // se preia tabelul din rezultatyul procedurii
        ResultSet res =sqldb.query(sCmd, 0);
        ContentValues cvalues = new ContentValues() ;
        cvalues.put(Table_GenUnic.COL_FAKE,"" );
        long nIdAntet =db.insert(Table_GenUnic.TABLE_NAME, null, cvalues);
        db.beginTransaction();
        try {
            while (res.next()) {
                double nCant=res.getDouble(Table_Pozitii.SCOL_CANTITATE);
                if (nCant!=0) {
                    long idPoz=db.insert(Table_GenUnic.TABLE_NAME, null, cvalues);
                    Bundle poz =new Bundle();
                    poz.putLong(Table_Pozitii._ID, idPoz);
                    poz.putLong(Table_Pozitii.COL_ID_ANTET, nIdAntet);
                    poz.putLong(Table_Pozitii.COL_ID_PRODUS,res.getLong(Table_Pozitii.SCOL_ID_PRODUS));
                    poz.putDouble(Table_Pozitii.COL_CANTITATE, nCant );
                    ContentValues cvalpoz=Biz.getInsertPozitii(poz);
                    cvalpoz.put(Table_Pozitii.COL_C_TIMESTAMP, "U");
                    Log.d("PRO&","5");

                    db.insertOrThrow(Table_Pozitii.TABLE_NAME, null, cvalpoz);
                    cvalpoz.clear();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.d("PRO&","Stocini 1");
        Bundle arg=new Bundle();
        arg.putLong(Table_Antet._ID,nIdAntet);
        arg.putString(Table_Antet.COL_CORESP,"STOCINIZI");
        arg.putString(Table_Antet.COL_DATA,Siruri.ttos(Siruri.getDateTime()));
        Log.d("PRO&","2");
        long nIdAgent= idDevice;
        arg.putLong(Table_Antet.COL_ID_AGENT,nIdAgent);
        arg.putLong(Table_Antet.COL_ID_DEVICE,nIdAgent);
        arg.putLong(Table_Antet.COL_ID_PART,-1);
        //Spinner spn=(Spinner) view.findViewById(R.id.spnFrgDocTipDoc);
        arg.putLong(Table_Antet.COL_ID_TIPDOC,Biz.TipDoc.ID_TIPDOC_AVIZINC);
        arg.putInt(Table_Antet.COL_LISTAT, 0);
        arg.putString(Table_Antet.COL_NR_DOC, "1");
		arg.putString(Table_Antet.COL_NR_CHITANTA,"");
        ContentValues cval = Biz.getInsertAntet(arg);
        cval.put(Table_Antet.COL_C_TIMESTAMP, "U");
        Log.d("PRO&","3");

        db.insertOrThrow(Table_Antet.TABLE_NAME, null, cval);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

	// preia avizul de incarcare generat automat
	public void sincroPreiaAvizGenerat (int idDevice) {
		// sterge avizul preluat anterior
		int nBlocat=0 ; // se preia flagul blocat de la imaginea anterioara
		String sCmd=""; 
		sCmd="select "+Table_Antet._ID+","+Table_Antet.COL_BLOCAT+" from "+Table_Antet.TABLE_NAME +" where " + 
				"Trim("+Table_Antet.COL_CORESP+")='A' and "+Table_Antet.COL_DATA+"='"+
				Siruri.dtos(Siruri.getDateTime(),"-")+"'";
		Log.d("PRO","Extrage pt sterg aviz : "+sCmd);
		Cursor crs=db.rawQuery(sCmd,null);
		crs.moveToFirst();
		db.beginTransaction();
		if (!crs.isAfterLast()) {
			
			nBlocat=crs.getInt(crs.getColumnIndex(Table_Antet.COL_BLOCAT));
			Log.d("PRO","Inainte de styerg aviz. Blocat= "+nBlocat);
			db.delete(Table_Antet.TABLE_NAME, "_id="+crs.getLong(crs.getColumnIndex(Table_Antet._ID)),null);
			db.delete(Table_Pozitii.TABLE_NAME, Table_Pozitii.COL_ID_ANTET+"="+crs.getLong(crs.getColumnIndex(Table_Antet._ID)), null);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		crs.close();		
		// preia din server
		sincroPreiaDinServer(Table_Antet.TABLE_NAME, Table_Antet.STABLE_NAME, Table_Antet.STR_ANTET, idDevice,
				"gen_auto=1 and id_agent="+idDevice+" and data_doc='"+Siruri.dtos(Siruri.getDateTime())+"'");
		sincroPreiaDinServer(Table_Pozitii.TABLE_NAME, Table_Pozitii.STABLE_NAME, Table_Pozitii.STR_POZITII, idDevice, 
				"id_antet in ( select cod_int from antet where gen_auto=1 and id_agent="+idDevice+" and data_doc='"+
				Siruri.dtos(Siruri.getDateTime())+"')");
		//pune flagul S
		blocheazaAvizInc(idDevice);
		puneDataAvizPreluat(idDevice,nBlocat);
	}
	// pune data preluata din server la avizul de incarcare . Data vine in forma DTOS iar aici e nevoie de data 
	// de forma aaaa-ll-zz
	public void puneDataAvizPreluat(int idDevice, int nBlocat) {
		String sCmd=""; 
		sCmd="select "+Table_Antet._ID+" from "+Table_Antet.TABLE_NAME +" where " + 
				"Trim("+Table_Antet.COL_CORESP+")='A' and "+Table_Antet.COL_DATA+"='"+
				Siruri.dtos(Siruri.getDateTime())+"'";
		Log.d("PRO","Extrage pt trans data : "+sCmd);
		Cursor crs=db.rawQuery(sCmd,null);
		crs.moveToFirst();
		db.beginTransaction();
		while (!crs.isAfterLast()) {
			sCmd=" UPDATE antet SET data='"+Siruri.dtos(Calendar.getInstance(),"-")+"'"+
					", "+Table_Antet.COL_BLOCAT+" = "+nBlocat+
					" WHERE _id="+crs.getLong(crs.getColumnIndex(Table_Antet._ID));
			Log.d("PRO","Update antet local:" + sCmd);
			db.execSQL(sCmd);
			
			crs.moveToNext();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		crs.close();		
	}
		
	
	public void blocheazaAvizInc (int idDevice) {
		// se blocheaza avizul in server
		String sCmd=""; 
		sCmd="select "+Table_Antet._ID+" from "+Table_Antet.TABLE_NAME +" where " + 
				"Trim("+Table_Antet.COL_CORESP+")='A' and "+Table_Antet.COL_DATA+"='"+
				Siruri.dtos(Siruri.getDateTime())+"'";
		Log.d("PRO","Extrage pt blocare: "+sCmd);
		Cursor crs=db.rawQuery(sCmd,null);
		crs.moveToFirst();
		while (!crs.isAfterLast()) {
			sCmd=" UPDATE antet SET flag='S' WHERE flag<>'S' and cod_int="+crs.getLong(crs.getColumnIndex(Table_Antet._ID));
			Log.d("PRO","Update server:" + sCmd);
			sqldb.exec(sCmd);
			crs.moveToNext();
		}
		crs.close();
	}
	public void sincroPreiaSoldAmb(int idDevice) {
		Calendar c=Siruri.getDateTime();
		c.add(Calendar.DATE, -1);
		String sCmd="EXECUTE detsoldamb 1,'"+Siruri.dtos(c)+"',"+idDevice+",0";
		// se preia tabelul din rezultatyul procedurii
		ResultSet res =sqldb.query(sCmd, 30);
		db.beginTransaction();
		try {
			db.delete(Table_SoldCurentAmbalaje.TABLE_NAME, null, null);
			if (res!=null) {
				ContentValues cVal = new ContentValues();
				while (res.next()){
					cVal.put(Table_SoldCurentAmbalaje.COL_CANTITATE, res.getInt("sold"));
					cVal.put(Table_SoldCurentAmbalaje.COL_ID_AGENT, idDevice);
					cVal.put(Table_SoldCurentAmbalaje.COL_ID_AMBALAJ, res.getString("cod_int"));
					cVal.put(Table_SoldCurentAmbalaje.COL_ID_CLIENT, res.getString("id_part"));					
					try {
						db.insertWithOnConflict(Table_SoldCurentAmbalaje.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cVal.clear();
				}
			}
		else{
			Log.d("SINC","res soldambalaje nul");
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		try {
			res.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	// se executa comenzile de stergere din server din tabela stergeri
	public void sincroSterge (int idDevice) {
		// preia din server inreg ce trebuie sterse in local
		String scmd="";
		String listaId="";
		ResultSet res =sqldb.query("SELECT cod_int,tabela,id_pozitie FROM stergeri WHERE sters=0 and sterge_in_mobil=1 and id_device="+idDevice );
		if (res != null && !sqldb.err) {
			db.beginTransaction();
			try {
				while (res.next()) {
					db.delete(res.getString("tabela"), "_id="+res.getLong("id_pozitie"), null);
					listaId=listaId+res.getLong("cod_int")+",";
				}
				listaId=listaId+"0";
				db.setTransactionSuccessful();
				// se transmit stergerile in sever
				scmd="UPDATE stergeri SET sters=1 , data_sters='"+Siruri.ttos(Siruri.getDateTime())+"'"+
						" WHERE cod_int IN ("+listaId+")";
				Log.d("PRO","STERGERI update "+scmd);
				sqldb.exec(scmd);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			db.endTransaction();
		}
	}
	
	public void sincroSablon (int idDevice) {
		ResultSet res =null;
		ResultSet respoz=null;
		String scmd="";
		Cursor crs=null;
		Cursor crspoz=null;
		ContentValues cval=new ContentValues();
		// preluare din server
		// a fost necesar sa scad 1 din id_ruta din server
		long nlastTime=getmaxServerTimeDinLocal(Table_Sablon_Antet.TABLE_NAME);
		scmd="SELECT *,cast(timestamp as integer) as "+Table_Sablon_Antet.SCOL_PT_TIMESTAMP+
				" FROM "+Table_Sablon_Antet.STABLE_NAME+
				" WHERE cast(timestamp as integer)>"+nlastTime+ " and "+
					Table_Sablon_Antet.COL_ID_AGENT+"="+idDevice;
		Log.d("PRO","Lat time="+scmd);
		res=sqldb.query(scmd);
		if (res!=null) {
			try {
				boolean lActual=false;
				db.beginTransaction();
				while (res.next()) {
					Log.d("PRO","AD SAB row "+res.getRow());
					lActual=false;
					scmd="SELECT "+
							Table_Sablon_Antet._ID+","+
							Table_Sablon_Antet.COL_C_TIMESTAMP+
							" FROM "+Table_Sablon_Antet.TABLE_NAME+
							" WHERE "+
							Table_Sablon_Antet.COL_ID_AGENT+"="+res.getLong(Table_Sablon_Antet.SCOL_ID_AGENT)+" AND "+
							Table_Sablon_Antet.COL_ID_PART+"="+(res.getLong(Table_Sablon_Antet.SCOL_ID_PART))+" AND "+
							Table_Sablon_Antet.COL_ID_RUTA+"="+(res.getLong(Table_Sablon_Antet.SCOL_ID_RUTA)-1)+" AND "+
							Table_Sablon_Antet.COL_ID_CURSA+"="+res.getLong(Table_Sablon_Antet.SCOL_ID_CURSA);
					Log.d("PRO","SEL antet = "+scmd);
					crs=db.rawQuery(scmd, null);
					// daca exista inreg se inlocuieste daca nu are A
					cval.clear();
					cval.put(Table_Sablon_Antet._ID, res.getLong(Table_Sablon_Antet.SCOL_COD_INT));
					cval.put(Table_Sablon_Antet.COL_BLOCAT, res.getInt(Table_Sablon_Antet.SCOL_BLOCAT));
					cval.put(Table_Sablon_Antet.COL_DATA, res.getString(Table_Sablon_Antet.SCOL_DATA));
					cval.put(Table_Sablon_Antet.COL_ID_AGENT, res.getLong(Table_Sablon_Antet.SCOL_ID_AGENT));
					cval.put(Table_Sablon_Antet.COL_ID_CURSA, res.getLong(Table_Sablon_Antet.SCOL_ID_CURSA));
					cval.put(Table_Sablon_Antet.COL_ID_DEVICE, res.getLong(Table_Sablon_Antet.SCOL_ID_DEVICE));
					cval.put(Table_Sablon_Antet.COL_ID_PART, res.getLong(Table_Sablon_Antet.SCOL_ID_PART));
					cval.put(Table_Sablon_Antet.COL_ID_RUTA, res.getLong(Table_Sablon_Antet.SCOL_ID_RUTA)-1);
					cval.put(Table_Sablon_Antet.COL_ID_TIPDOC, res.getLong(Table_Sablon_Antet.SCOL_ID_TIPDOC));
					cval.put(Table_Sablon_Antet.COL_C_TIMESTAMP, "U");
					cval.put(Table_Sablon_Antet.COL_S_TIMESTAMP, res.getLong(Table_Sablon_Antet.SCOL_PT_TIMESTAMP ));
					Log.d("PRO","nr inreg "+crs.getCount());
					if (crs.getCount()==1) {
						crs.moveToFirst();
						if (!(crs.getString(crs.getColumnIndex(Table_Sablon_Antet.COL_C_TIMESTAMP))).equals("A")) {
							// exista inreg si nu este marcata cu A . se face update din server
							db.update(Table_Sablon_Antet.TABLE_NAME, cval,Table_Sablon_Antet._ID+"="+
									crs.getLong(crs.getColumnIndex(Table_Sablon_Antet._ID))
									, null);
							// se sterg pozitiile
							db.delete(Table_Sablon_Pozitii.TABLE_NAME,Table_Sablon_Pozitii.COL_ID_ANTET+"="+
									crs.getLong(crs.getColumnIndex(Table_Sablon_Pozitii._ID)) , null);
							lActual=true;
							Log.d("PRO","updt "+cval.getAsLong("_ID"));
						} else {
							Log.d("PRO","nimic");
						}
					} else {
						lActual=true;
						db.insert(Table_Sablon_Antet.TABLE_NAME, null, cval);
						Log.d("PRO","inse "+cval.getAsLong("_ID"));
					}
					crs.close();
					if (lActual) {
						// se insereaza pozitiile
						respoz=sqldb.query("SELECT * FROM "+Table_Sablon_Pozitii.TABLE_NAME +" WHERE "+
								Table_Sablon_Pozitii.SCOL_ID_ANTET+"="+res.getLong(Table_Sablon_Antet.SCOL_COD_INT));
						if (respoz!=null) {
							while (respoz.next()) {
								cval.clear();
								cval.put(Table_Sablon_Pozitii._ID, respoz.getLong(Table_Sablon_Pozitii.SCOL_COD_INT));
								cval.put(Table_Sablon_Pozitii.COL_C_TIMESTAMP, "U");
								cval.put(Table_Sablon_Pozitii.COL_CANTITATE, respoz.getDouble(Table_Sablon_Pozitii.SCOL_CANTITATE));
								cval.put(Table_Sablon_Pozitii.COL_DIFERENTE, respoz.getDouble(Table_Sablon_Pozitii.SCOL_DIFERENTE));
								cval.put(Table_Sablon_Pozitii.COL_ID_ANTET, respoz.getLong(Table_Sablon_Pozitii.SCOL_ID_ANTET));
								cval.put(Table_Sablon_Pozitii.COL_ID_PRODUS, respoz.getLong(Table_Sablon_Pozitii.SCOL_ID_PRODUS));
								cval.put(Table_Sablon_Pozitii.COL_ID_UM, respoz.getLong(Table_Sablon_Pozitii.SCOL_ID_UM));
								Log.d("PRO"," adauga poz");
								db.insertWithOnConflict(Table_Sablon_Pozitii.TABLE_NAME, null, cval,SQLiteDatabase.CONFLICT_REPLACE);
							}
						}
						respoz.close();
					}
				} // de la while
				db.setTransactionSuccessful();
				db.endTransaction();
				res.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// transmite in server
		// a fost necesar saadauhg 1 la ruta 
		crs=db.rawQuery(
				"SELECT " +
				"_id,id_user,id_device,id_part,id_agent,id_ruta+1 as id_ruta,id_cursa,data,id_tipdoc,transmis,blocat,s_timestamp,flag " + 
				"FROM "+Table_Sablon_Antet.TABLE_NAME+" WHERE "+
			Table_Sablon_Antet.COL_C_TIMESTAMP+"<>'U' ",null);
		
		if (crs.getCount()>0) {
			db.beginTransaction();
			crs.moveToFirst();
			long nIdServer=0;
			long ntime=0;
			while (!crs.isAfterLast()) {
				nIdServer=0;
				scmd="SELECT cod_int FROM "+Table_Sablon_Antet.STABLE_NAME+
					" WHERE "+
						Table_Sablon_Antet.SCOL_ID_AGENT+"="+idDevice +" AND "+
						Table_Sablon_Antet.SCOL_ID_PART+"="+crs.getLong(crs.getColumnIndex(Table_Sablon_Antet.COL_ID_PART)) +" AND "+
						Table_Sablon_Antet.SCOL_ID_RUTA+"="+crs.getLong(crs.getColumnIndex(Table_Sablon_Antet.COL_ID_RUTA)) +" AND "+
						Table_Sablon_Antet.SCOL_ID_CURSA+"="+crs.getLong(crs.getColumnIndex(Table_Sablon_Antet.COL_ID_CURSA));
				Log.d("SINCROSABLON","1 "+scmd);
				res=sqldb.query( scmd);
				if (!sqldb.err) {
					try {
						if (res.next()) {
							nIdServer=res.getLong("cod_int");
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					res.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (nIdServer!=0) {
					// se sterge sablonul
					Log.d("PRO","STERGSAB="+nIdServer);
					sqldb.exec("DELETE FROM "+Table_Sablon_Antet.STABLE_NAME +" WHERE cod_int="+nIdServer);
					sqldb.exec("DELETE FROM "+Table_Sablon_Pozitii.STABLE_NAME+" WHERE id_antet="+nIdServer);
					Log.d("PRO","STERGSABLON "+"sterge");
				} 
				// se adauga comenzile
				scmd=creaInsert(crs, Table_Sablon_Antet.STR_SABLON_ANTET, Table_Sablon_Antet.STABLE_NAME);
				Log.d("INSERT",scmd);
				sqldb.exec(scmd);
				// se preia timestamp din server
				ntime=0;
				scmd="SELECT cast(timestamp as integer) as time FROM "+Table_Sablon_Antet.STABLE_NAME+
						" WHERE "+
						Table_Sablon_Antet.SCOL_ID_AGENT+"="+idDevice +" AND "+
						Table_Sablon_Antet.SCOL_ID_PART+"="+crs.getLong(crs.getColumnIndex(Table_Sablon_Antet.COL_ID_PART)) +" AND "+
						Table_Sablon_Antet.SCOL_ID_RUTA+"="+crs.getLong(crs.getColumnIndex(Table_Sablon_Antet.COL_ID_RUTA)) +" AND "+
						Table_Sablon_Antet.SCOL_ID_CURSA+"="+crs.getLong(crs.getColumnIndex(Table_Sablon_Antet.COL_ID_CURSA));
				Log.d("SINCROSABLON","2 "+scmd);
				res=sqldb.query( scmd);
				if (!sqldb.err) {
						try {
							if (res.next()) {
								ntime=res.getLong("time");
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				try {
						res.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
					// se face update cu timestamp din server
				if (ntime!=0) {
					cval.clear();
					cval.put(Table_Sablon_Antet.COL_S_TIMESTAMP,ntime);
					cval.put(Table_Sablon_Antet.COL_C_TIMESTAMP, "U");
					db.update(Table_Sablon_Antet.TABLE_NAME, cval, "_id="+crs.getLong(crs.getColumnIndex("_id")), null);
					crspoz=db.rawQuery("SELECT * FROM "+Table_Sablon_Pozitii.TABLE_NAME+
						" WHERE "+Table_Sablon_Pozitii.COL_ID_ANTET+"="+crs.getLong(crs.getColumnIndex("_id")), null);
					crspoz.moveToFirst();
					while (!crspoz.isAfterLast()) {
						scmd=creaInsert(crspoz, Table_Sablon_Pozitii.STR_SABLON_POZITII, Table_Sablon_Pozitii.STABLE_NAME);
						Log.d("INSERTPOZ",scmd);
						sqldb.exec(scmd);
						crspoz.moveToNext();
					}
				}
					
				crs.moveToNext();
			} // de la while
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		// preia din server
		
	}

	public Sincronizare(SQLiteDatabase db ,MySQLDBadapter sqldb, int idDevice) {
		// TODO Auto-generated constructor stub
		// conectarea la server sql
		this.sqldb=sqldb;
		// conectare la sqlite
		this.db = db;
		this.idDevice=idDevice;
	}
	
//	public void sincroToate() {
//		sincroPreiaSoldpart(idDevice);
//		Log.d("SINCRO","dupa soldpart");
//		sincroPreiaDiscount(idDevice);
//		Log.d("SINCRO","dupa discount");
//		sincroPreiaProduse(-1);
//		Log.d("SINCRO","dupa produse");
//		sincroPreiaClient(idDevice);
//		Log.d("SINCRO","dupa client");
//	}
	
	public void sincroPreiaSablon (int idAgent) {
		// preia sabloanele modificate ci timestamp mai mare
		//??
		ResultSet res=sincroGetSetDinServer(Table_Sablon_Antet.TABLE_NAME, Table_Sablon_Antet.STABLE_NAME,
				Table_Sablon_Antet.STR_SABLON_ANTET, idAgent,false,"",0);
		if (res!=null) {
			ContentValues cval = new ContentValues();
			try {
				while (res.next()){
					cval.put(Table_Antet._ID, res.getLong(Table_Antet.SCOL_COD_INT));
					cval.put(Table_Sablon_Antet.COL_BLOCAT, res.getInt(Table_Sablon_Antet.SCOL_BLOCAT));
					cval.put(Table_Sablon_Antet.COL_S_TIMESTAMP, res.getLong(Table_Sablon_Antet.SCOL_PT_TIMESTAMP));
					cval.put(Table_Sablon_Antet.COL_DATA, res.getString(Table_Sablon_Antet.SCOL_DATA_DOC));
					cval.put(Table_Sablon_Antet.COL_ID_AGENT, res.getLong(Table_Sablon_Antet.SCOL_ID_AGENT));
					cval.put(Table_Sablon_Antet.COL_ID_DEVICE, res.getLong(Table_Sablon_Antet.SCOL_ID_DEVICE));
					cval.put(Table_Sablon_Antet.COL_ID_PART, res.getLong(Table_Sablon_Antet.SCOL_ID_PART));
					cval.put(Table_Sablon_Antet.COL_ID_RUTA, res.getLong(Table_Sablon_Antet.SCOL_ID_RUTA));
					cval.put(Table_Sablon_Antet.COL_ID_TIPDOC, res.getInt(Table_Sablon_Antet.SCOL_ID_TIPDOC));
					cval.put(Table_Sablon_Antet.COL_ID_CURSA, res.getInt(Table_Sablon_Antet.SCOL_ID_RUTA));
					cval.put(Table_Sablon_Antet.COL_TRANSMIS, res.getLong(Table_Sablon_Antet.SCOL_TRANSMIS));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void sincroPreiaSoldpart(int idDevice) {
		Log.d("PRO","SOLDPART Ininte de get res");
		String sCmd="EXECUTE detsoldpart 1,'"+Siruri.dtos(Siruri.getDateTime())+"',"+idDevice;
		sqldb.exec(sCmd,100);
		Log.d("PRO","SOLDPART 2");
		ResultSet res = sincroGetSetDinServer(Table_Soldpart.TABLE_NAME, Table_Soldpart.STABLE_NAME, Table_Soldpart.COL_SINCRO_SERVER,idDevice);
		Log.d("PRO","SOLDPART 1");
		db.beginTransaction();
		try {
			db.delete(Table_Soldpart.TABLE_NAME, null, null);
			Log.d("PRO","Ininte de parcurgere dsoldpart:");
			if (res!=null) {
				ContentValues cVal = new ContentValues();
				while (res.next()){
					cVal.put(Table_Soldpart._ID, res.getInt(Table_Soldpart.SCOL_COD_INT));
					cVal.put(Table_Soldpart.COL_ID_PART, res.getInt(Table_Soldpart.SCOL_ID_PART));
					cVal.put(Table_Soldpart.COL_NR_DOC, res.getString(Table_Soldpart.SCOL_NR_DOC));
					cVal.put(Table_Soldpart.COL_DATA, res.getString(Table_Soldpart.SCOL_DATA));
					cVal.put(Table_Soldpart.COL_DATA_SCAD, res.getString(Table_Soldpart.SCOL_DATA_SCAD));
					cVal.put(Table_Soldpart.COL_VAL_INI, res.getDouble(Table_Soldpart.SCOL_VAL_INI));
					cVal.put(Table_Soldpart.COL_REST, res.getDouble(Table_Soldpart.SCOL_REST));
					cVal.put(Table_Soldpart.COL_S_TIMESTAMP,res.getInt(Table_Soldpart.SCOL_PT_TIMESTAMP));
					
					try {
						db.insertWithOnConflict(Table_Soldpart.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
						Log.d("SINCRO","4-soldpart");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d("SINCRO","Err: "+res.getInt(Table_Soldpart.SCOL_COD_INT)+" "+e.getMessage());
					}
					cVal.clear();
				}
				Log.d("SINC","dupa sold");
				// dupa preluarea de discount se pune 1 la fara_sold in antet ca sa nu se mai ia la sold
				db.beginTransaction();
				db.execSQL("UPDATE "+Table_Antet.TABLE_NAME+" SET "+Table_Antet.COL_FARA_SOLD+"=1"+
						" WHERE "+Table_Antet.COL_FARA_SOLD+"=0");
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		else{
			Log.d("SINC","res soldpart nul");
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		Log.d("SINCRO","Ininte de inchid res");
		try {
			res.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("SINCRO","Dupa inchid res");
	}

	
	public void sincroPreiaDiscount(int idDevice) {
		ResultSet res = sincroGetSetDinServer(Table_Discount.TABLE_NAME, Table_Discount.STABLE_NAME, Table_Discount.COL_SINCRO_SERVER,idDevice);
		Log.d("DISC","1");
		db.beginTransaction();
		try {
			Log.d("Sinc","Ininte de parcurgere discount:");
			if (res!=null) {
				while (res.next()){
					ContentValues cVal = new ContentValues();
					cVal.put(Table_Discount._ID, res.getInt(Table_Discount.SCOL_COD_INT));
					cVal.put(Table_Discount.COL_ID_CLIENT,res.getInt(Table_Discount.SCOL_ID_CLIENT));
					cVal.put(Table_Discount.COL_ID_PRODUS,res.getInt(Table_Discount.SCOL_ID_PRODUS));
					cVal.put(Table_Discount.COL_PRET_CU,res.getDouble(Table_Discount.SCOL_PRET_CU));
					cVal.put(Table_Discount.COL_PRET_FARA,res.getDouble(Table_Discount.SCOL_PRET_FARA));
					cVal.put(Table_Discount.COL_DISCOUNT,res.getDouble(Table_Discount.SCOL_DISCOUNT));
					cVal.put(Table_Discount.COL_BLOCAT,res.getInt(Table_Discount.SCOL_BLOCAT));
					cVal.put(Table_Discount.COL_S_TIMESTAMP,res.getInt(Table_Discount.SCOL_PT_TIMESTAMP));
					try {
						db.insertWithOnConflict(Table_Discount.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
						Log.d("SINCRO","4-discount");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d("SINCRO","Err: "+res.getInt(Table_Discount.SCOL_COD_INT)+" "+e.getMessage());
					}
				}
			}
		else{
			Log.d("SINC","res discount nul");
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		Log.d("SINCRO","Ininte de inchid res");
		try {
			res.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("SINCRO","Dupa inchid res");
	}
    // special pt clienti update in server
//    public void sincroUpdateClientInServer (int idDevice) {
//        Cursor crs =db.rawQuery(" SELECT * FROM "+Table_Clienti.TABLE_NAME+" WHERE flag='A'",null);
//        String scmd="";
//        if (crs.getCount()>0) {
//            crs.moveToFirst();
//            while (!crs.isAfterLast()) {
//                scmd=""
//            }
//        }
//    }


	public void sincroPreiaClientAgent(int idDevice) {
		ResultSet res=sincroGetSetDinServer(Table_Client_Agent.TABLE_NAME, Table_Client_Agent.STABLE_NAME,
				Table_Client_Agent.STR_CLIENT_AGENT, -1, true, "", 0);
		db.beginTransaction();
		try {
			ContentValues cVal = new ContentValues();
			while (res.next()) {
				cVal=setval(Table_Client_Agent.STR_CLIENT_AGENT,res);
				cVal.put(Table_Client_Agent.COL_C_TIMESTAMP, "U");
				cVal.put(Table_Client_Agent.COL_S_TIMESTAMP, res.getInt(Table_Client_Agent.SCOL_PT_TIMESTAMP));

				db.insertWithOnConflict(Table_Client_Agent.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
				cVal.clear();
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.endTransaction();
		try {
			res.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
//	public void sincroPreiaPartener(int idDevice) {
//		Log.d("PRO"
// );
//		ResultSet res=sincroGetSetDinServer(Table_Partener.TABLE_NAME, Table_Partener.STABLE_NAME,
//				Table_Partener.STR_PARTENER, -1, true, "", 0);
//		Log.d("PRO","2");
//		db.beginTransaction();
//		try {
//			Log.d("PRO","Sinc : Inainte de parcurgere partener:"+res.getRow());
//			ContentValues cVal = new ContentValues();
//			while (res.next()) {
//				cVal=setval(Table_Partener.STR_PARTENER,res);
//				cVal.put(Table_Partener.COL_C_TIMESTAMP, "U");
//				cVal.put(Table_Partener.COL_S_TIMESTAMP, res.getInt(Table_Partener.SCOL_PT_TIMESTAMP));
//
//				db.insertWithOnConflict(Table_Partener.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
//				cVal.clear();
//			}
//			db.setTransactionSuccessful();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		db.endTransaction();
//		try {
//			res.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	public void sincroPreiaAgent(int idDevice) {
		Log.d("PRO","PreiaAgent 1");
		ResultSet res=sincroGetSetDinServer(Table_Agent.TABLE_NAME, Table_Agent.STABLE_NAME,
				Table_Agent.STR_AGENT, -1, true, "", 0);
		Log.d("PRO","2");
		db.beginTransaction();
		try {
			Log.d("PRO","Sinc : Inainte de parcurgere agent:"+res.getRow());
			ContentValues cVal = new ContentValues();
			while (res.next()) {
				cVal=setval(Table_Agent.STR_AGENT,res);
				cVal.put(Table_Agent.COL_C_TIMESTAMP, "U");
				cVal.put(Table_Agent.COL_S_TIMESTAMP, res.getInt(Table_Agent.SCOL_PT_TIMESTAMP));
				
				db.insertWithOnConflict(Table_Agent.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
				cVal.clear();
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.endTransaction();
		try {
			res.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


//	public void sincroPreiaClient(int idDevice) {
//		// TODO Auto-generated method stub
//		ResultSet res = sincroGetSetDinServer(Table_Clienti.TABLE_NAME, Table_Clienti.STABLE_NAME, Table_Clienti.COL_SINCRO_SERVER,idDevice,true);
//		Log.d("sinc","Dupa get din server pt clienti");
//		db.beginTransaction();
//		Log.d("sinc","Dupa begin trans");
//		try {
//			//Log.d("Sinc","Ininte de parcurgere clienti:"+res.getRow());
//			if (res!=null) {
//			while (res.next()){
//				ContentValues cVal = new ContentValues();
//				cVal.put(Table_Clienti._ID, res.getInt(Table_Clienti.SCOL_COD_INT));
//				cVal.put(Table_Clienti.COL_ORDONARE, res.getInt(Table_Clienti.SCOL_ORDONARE));
//				cVal.put(Table_Clienti.COL_ID_PART,res.getString(Table_Clienti.SCOL_ID_PART));
//				cVal.put(Table_Clienti.COL_DENUMIRE,res.getString(Table_Clienti.SCOL_DENUMIRE));
//				cVal.put(Table_Clienti.COL_NR_FISc,res.getString(Table_Clienti.SCOL_NR_FISC));
//				cVal.put(Table_Clienti.COL_NR_RC,res.getString(Table_Clienti.SCOL_NR_RC));
//				cVal.put(Table_Clienti.COL_JUDET,res.getString(Table_Clienti.SCOL_JUDET));
//				cVal.put(Table_Clienti.COL_LOC,res.getString(Table_Clienti.SCOL_LOC));
//				cVal.put(Table_Clienti.COL_ADRESA,res.getString(Table_Clienti.SCOL_ADRESA));
//				cVal.put(Table_Clienti.COL_TEL1,res.getString(Table_Clienti.SCOL_TEL1));
//				cVal.put(Table_Clienti.COL_TEL2,res.getString(Table_Clienti.SCOL_TEL2));
//				cVal.put(Table_Clienti.COL_CONTACT,res.getString(Table_Clienti.SCOL_CONTACT));
//				cVal.put(Table_Clienti.COL_BANCA,res.getString(Table_Clienti.SCOL_BANCA));
//				cVal.put(Table_Clienti.COL_CONT,res.getString(Table_Clienti.SCOL_CONT));
//				cVal.put(Table_Clienti.COL_ID_ZONA,res.getInt(Table_Clienti.SCOL_ID_ZONA));
//				cVal.put(Table_Clienti.COL_BLOCAT,res.getInt(Table_Clienti.SCOL_BLOCAT));
//				cVal.put(Table_Clienti.COL_BLOCAT_VANZARE,res.getInt(Table_Clienti.SCOL_BLOCAT_VANZARE));
//				cVal.put(Table_Clienti.COL_PROC_RED,res.getInt(Table_Clienti.SCOL_PROC_RED));
//				cVal.put(Table_Clienti.COL_S_TIMESTAMP,res.getInt(Table_Clienti.SCOL_PT_TIMESTAMP));
//				Log.d("SINCRO","3/ "+res.getInt(Table_Clienti.SCOL_COD_INT)+" den:"+res.getString(Table_Clienti.SCOL_DENUMIRE));
//				try {
//					db.insertWithOnConflict(Table_Clienti.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
//					Log.d("SINCRO","4");
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Log.d("SINCRO","Err: "+res.getInt(Table_Clienti.SCOL_COD_INT)+" "+e.getMessage());
//				}
//			}
//			} else {
//				Log.d("sincro","res este null la clienti");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		db.setTransactionSuccessful();
//		db.endTransaction();
//		Log.d("SINCRO","Ininte de inchid res");
//		try {
//			res.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Log.d("SINCRO","Dupa inchid res");
//			}
	
	
		
	public void sincroPreiaProduse (int idDevice) {
		// se preiau din server inreg pt sincronizare . Se preiau numai produsele cu versiune=1
		db.beginTransaction();
		try {
			ResultSet res=sincroGetSetDinServer(Table_Produse.TABLE_NAME,Table_Produse.STABLE_NAME,Table_Produse.COL_SINCRO_SERVER,idDevice,false,"versiune=1",0); 
			Log.d("Sinc","Ininte de parcurgere produse:"+res.getRow());
			ContentValues cVal = new ContentValues();

			while (res.next()) {
				Log.d("Sinc","denumire="+res.getString(Table_Produse.SCOL_DENUMIRE));
				cVal.put(Table_Produse._ID, res.getInt(Table_Produse.SCOL_COD_INT));
				cVal.put(Table_Produse.COL_ORDONARE, res.getInt(Table_Produse.SCOL_ORDONARE));
				cVal.put(Table_Produse.COL_DENUMIRE, res.getString(Table_Produse.SCOL_DENUMIRE));
				cVal.put(Table_Produse.COL_COD_PROD, res.getString(Table_Produse.SCOL_COD_PROD));
				cVal.put(Table_Produse.COL_PRET_FARA, res.getDouble(Table_Produse.SCOL_PRET_FARA));
				cVal.put(Table_Produse.COL_PRET_CU, res.getDouble(Table_Produse.SCOL_PRET_CU));
				cVal.put(Table_Produse.COL_COTA_TVA, res.getDouble(Table_Produse.SCOL_COTA_TVA));
				cVal.put(Table_Produse.COL_PRET_FARA1, res.getDouble(Table_Produse.SCOL_PRET_FARA1));
				cVal.put(Table_Produse.COL_PRET_CU1, res.getDouble(Table_Produse.SCOL_PRET_CU1));
				cVal.put(Table_Produse.COL_ID_TIP, res.getInt(Table_Produse.SCOL_ID_TIP));
				cVal.put(Table_Produse.COL_ID_MASTER, res.getInt(Table_Produse.SCOL_ID_MASTER));
				cVal.put(Table_Produse.COL_BUC_BOX, res.getDouble(Table_Produse.SCOL_BUC_BOX));
				cVal.put(Table_Produse.COL_BLOCAT, res.getInt(Table_Produse.SCOL_BLOCAT));
				cVal.put(Table_Produse.COL_SUPLIM1, res.getString(Table_Produse.SCOL_SUPLIM1));
				cVal.put(Table_Produse.COL_SUPLIM2, res.getString(Table_Produse.SCOL_SUPLIM2));
				cVal.put(Table_Produse.COL_SUPLIM3, res.getString(Table_Produse.SCOL_SUPLIM3));
				cVal.put(Table_Produse.COL_SUPLIM4, res.getString(Table_Produse.SCOL_SUPLIM4));
				cVal.put(Table_Produse.COL_S_TIMESTAMP, res.getInt(Table_Produse.SCOL_PT_TIMESTAMP));
				try {
					db.insertWithOnConflict(Table_Produse.TABLE_NAME, null, cVal, SQLiteDatabase.CONFLICT_REPLACE);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cVal.clear();
			}; 
			Log.d("sinc:","dupa insert");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}



	// adaugare inreg noi in server
	public void sincroAdaugInServer (String tbLocala, String tbServer, String [][] str, String filtru, boolean lPreiaTime)  {
		Cursor crs =db.rawQuery(" SELECT * FROM "+tbLocala+" WHERE upper(flag)='A' " +
                (filtru.equals("") ? "" : " AND ")+filtru+" order by _id ", null);
		if (crs.getCount()>0) {
                db.beginTransaction();
                crs.moveToFirst();
                while (!crs.isAfterLast()) {
                    String sqlSir = creaInsert(crs, str, tbServer);
                    Log.d("INSERT", sqlSir);
                    sqldb.exec(sqlSir,10);
                    if (true || !sqldb.err) {
                        // se pune flag=U pt cele reusite
                        Long iId = crs.getLong(crs.getColumnIndexOrThrow("_id"));
                        Log.d("PRO&","Insert in :"+tbLocala+" Id="+iId);
                        long nTime = 0;
                        if (lPreiaTime) {
                            // 	se preia timestamp din server pt inreg adaugata ca sa nu mai vina inapoi
                            ResultSet res = sqldb.query("select cast(timestamp as integer) as pt_timestamp from " + tbServer +
                                    " where cod_int=" + iId);
                            if (res != null)
                                try {
                                    res.next();
                                    nTime = res.getLong("pt_timestamp");
                                } catch (SQLException e) {
                                    // TODO Auto-generated catch block
                                    Log.d("PRO&","Eroare la preluarea timestamp:"+e.getMessage());
                                    e.printStackTrace();
                                }
                        }

                        ContentValues cval = new ContentValues();
                        cval.put("flag", "U");
                        cval.put("s_timestamp", nTime);
                        db.update(tbLocala, cval, "_id=" + iId, null);
                    } else {
                        Log.d("PRO&", "Eroare la:" + sqlSir);
                    }
                    crs.moveToNext();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
        }

		crs.close();
	}
    public void sincroAdaugInServerNou (String tbLocala, String tbServer, String [][] str, String filtru) {
        sincroAdaugInServerNou(tbLocala,tbServer,str,filtru,true);
    }


    public void sincroAdaugInServerNou (String tbLocala, String tbServer, String [][] str, String filtru,boolean lCuIdDevice) {
        Cursor crs = db.rawQuery(" SELECT _id FROM " + tbLocala + " WHERE (upper(flag)='A' or upper(flag)='M') " +
                (filtru.equals("") ? "" : " AND ") + filtru + " order by _id ", null);
        if (crs.getCount() > 0) {
            Log.d("PRO","AdaugIn server nou 1");
            // se creeaza filtru de tip in(..) pt a prelua lista de iduri din server pe baza careia se va stabili actiunea in server ( ad sau mod)
            String sFiltruIn = Biz.getFiltruIn(crs, "");
            String sqlCmd = " SELECT cod_int from " + tbServer + " where cod_int in (" + sFiltruIn + ") order by cod_int";
            Log.d("PRO","SELE ids= "+sqlCmd);
            ResultSet resLista = sqldb.query(sqlCmd,20);
            if (resLista != null) {
                Log.d("PRO&","2");
                // pentru inregistrarile care mai sunt si in server se face modificare
                String sFiltruMod = Biz.getFiltruIn(resLista, "");
                // se creeaza cursorul pt modificare
                Log.d("PRO&","3 "+" SELECT * FROM " + tbLocala + " WHERE (upper(flag)='A' or upper(flag)='M') " +
                        " AND " + "_id in (" + sFiltruMod + ") " +
                        (filtru.equals("") ? "" : " AND ") + filtru + " order by _id ");
                Cursor crsMod = db.rawQuery(" SELECT * FROM " + tbLocala + " WHERE (upper(flag)='A' or upper(flag)='M') " +
                        " AND " + "_id in (" + sFiltruMod + ") " +
                        (filtru.equals("") ? "" : " AND ") + filtru + " order by _id ", null);
                if (crsMod.getCount() > 0) {
                    // transmite comenzi de modificare
                    crsMod.moveToFirst();
                    while (!crsMod.isAfterLast()) {
                        sqlCmd = creaUpdate(crsMod, str, tbServer, lCuIdDevice); // se presupune ca tabela are id_device

                        sqldb.exec(sqlCmd);
                        crsMod.moveToNext();
                    }

                }
                crsMod.close();
                Log.d("PRO&","4");
                // se creeaza cursorul pt adaugare
                Cursor crsAd = db.rawQuery(" SELECT * FROM " + tbLocala + " WHERE (upper(flag)='A' or upper(flag)='M') " +
                        " AND " + "_id NOT in (" + sFiltruMod + ") " +
                        (filtru.equals("") ? "" : " AND ") + filtru + " order by _id ", null);
                if (crsAd.getCount() > 0) {
                    // transmite comenzi de adaugare
                    crsAd.moveToFirst();
                    while(!crsAd.isAfterLast()) {
                        sqlCmd = creaInsert(crsAd, str, tbServer,lCuIdDevice); // tabela trebuie sa aiba id_device
                        sqldb.exec(sqlCmd);
                        crsAd.moveToNext();

                    }
                }
                Log.d("PRO&","5");
                crsAd.close();
                // se preiau timestamp din server si se face flag=U in local
                // se foloseste sfiltruin determinat la inceput
                try {
                    resLista.close();
                    sqlCmd = " SELECT cod_int, cast(timestamp as integer) as timestamp from " + tbServer + " where cod_int in (" + sFiltruIn + ") order by cod_int";
                    resLista = sqldb.query(sqlCmd);
                    if (resLista != null) {
                        db.beginTransaction();
                        while (resLista.next()) {
                            db.execSQL("UPDATE " + tbLocala + " SET flag='U',s_timestamp=" + resLista.getInt("timestamp"));
                        }
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                Log.d("PRO&1","reslista=null la "+sqlCmd);
            }
            crs.close();
        }
    }
    // creeaza instr de insert in server
    public String creaInsert (Cursor crs,String [][] str, String tbServer) {
        return creaInsert(crs,str,tbServer,true);
    }

	public String creaInsert (Cursor crs,String [][] str, String tbServer,boolean lCuIdDevice) {
		// pe prima pozitie in str stau campurile de legatura (_id si cod_int)
		// id device nu exista in structuri si se trimite in server din preferinte
		String sqlCamp="";
		String sqlVal="";
		String sval="";
        String sqlSir="";
		for (int i = 0; i < str.length; i++) {
			if (str[i].length>2) {
				if(!str[i][3].equals("")) {
					// denumirile campurilor din tabela server sunt in coloana a treia (index 2)
					sqlCamp=sqlCamp+(sqlCamp.equals("") ? "" : ",")+str[i][2];
					if (str[i][3].equals(STypes.INTREG)) {
						sval=Integer.toString(crs.getInt(crs.getColumnIndexOrThrow(str[i][0])));
					} else if (str[i][3].equals(STypes.DATA)) {
						sval="'"+crs.getString(crs.getColumnIndexOrThrow(str[i][0]))+"'";
					} else if (str[i][3].equals(STypes.DTOS)) {
						sval=crs.getString(crs.getColumnIndexOrThrow(str[i][0]));
						sval="'"+sval.substring(0,4)+sval.subSequence(5, 7)+sval.substring(8, 10)+"'";
					} else if (str[i][3].equals(STypes.STRING)) {
						sval="'"+crs.getString(crs.getColumnIndexOrThrow(str[i][0]))+"'";
					} else if (str[i][3].equals(STypes.VALOARE)) {
						sval=Double.toString(crs.getDouble(crs.getColumnIndexOrThrow(str[i][0])));
					}
					sqlVal=sqlVal+(sqlVal.equals("") ? "" : ",")+sval;
				}
			}
		}
        if (lCuIdDevice) {
            sqlSir=" INSERT INTO "+tbServer+" ( "+sqlCamp+",id_device ) VALUES ("+sqlVal+","+idDevice+")";
        } else {
            sqlSir=" INSERT INTO "+tbServer+" ( "+sqlCamp+") VALUES ("+sqlVal+")";
        }
		return sqlSir;
	}

    public String creaUpdate (Cursor crs,String [][] str, String tbServer,boolean lCuIddevice) {
        // pe prima pozitie in str stau campurile de legatura (_id si cod_int)
        // id device nu exista in structuri si se trimite in server din preferinte

        String sqlCamp="";
        String sqlVal="";
        String sval="";
        for (int i = 0; i < str.length; i++) {
            if (str[i].length>2) {
                if(!str[i][3].equals("") && !str[i][2].toUpperCase().equals("COD_INT")) {
                    // denumirile campurilor din tabela server sunt in coloana a treia (index 2)
                    // se sare peste _id
                    if (str[i][3].equals(STypes.INTREG)) {
                        sval=Integer.toString(crs.getInt(crs.getColumnIndexOrThrow(str[i][0])));
                    } else if (str[i][3].equals(STypes.DATA)) {
                        sval="'"+crs.getString(crs.getColumnIndexOrThrow(str[i][0]))+"'";
                    } else if (str[i][3].equals(STypes.DTOS)) {
                        sval=crs.getString(crs.getColumnIndexOrThrow(str[i][0]));
                        sval="'"+sval.substring(0,4)+sval.subSequence(5, 7)+sval.substring(8, 10)+"'";
                    } else if (str[i][3].equals(STypes.STRING)) {
                        sval="'"+crs.getString(crs.getColumnIndexOrThrow(str[i][0]))+"'";
                    } else if (str[i][3].equals(STypes.VALOARE)) {
                        sval=Double.toString(crs.getDouble(crs.getColumnIndexOrThrow(str[i][0])));
                    }
                    sqlCamp=sqlCamp+(sqlCamp.equals("") ? "" : ",")+str[i][2]+"="+sval;
                }
            }
        }
        if (lCuIddevice)
            sqlCamp=sqlCamp+(sqlCamp.equals("") ? "" : ",")+"id_device"+"="+idDevice;
        String sqlSir=" UPDATE "+tbServer+" SET "+sqlCamp+" WHERE cod_int="+crs.getInt(crs.getColumnIndexOrThrow("_id")) ;
        Log.d("PRO","Update= "+sqlSir);
        return sqlSir;
    }


	public ResultSet sincroGetSetDinServer(String tbLocala,String tbServer,String colSincroServer, String cWhere) {
		return sincroGetSetDinServer(tbLocala, tbServer, colSincroServer, idDevice, false,cWhere,0);
	}
	
	public ResultSet sincroGetSetDinServer(String tbLocala,String tbServer,String colSincroServer, int idDevice) {
		return sincroGetSetDinServer(tbLocala, tbServer, colSincroServer, idDevice, false,"",0);
	}
	
	public ResultSet sincroGetSetDinServer(String tbLocala,String tbServer,String colSincroServer, int idDevice,boolean faraBlocate) {
		return sincroGetSetDinServer(tbLocala, tbServer, colSincroServer, idDevice, faraBlocate,"",0);
	}
	
	
	public ResultSet sincroGetSetDinServer(String tbLocala,String tbServer,String[][] struct, int idDevice,boolean faraBlocate,
			String cFiltru,
			long nLastTime) {
		String colSincroServer="";
		for (int i = 0; i < struct.length; i++) {
			if (struct[i].length>2 && !struct[i][2].equals("")) {
			colSincroServer=colSincroServer+struct[i][2]+",";
			}
		}
		Log.d("PRO","GETSET "+colSincroServer);
		colSincroServer=colSincroServer+" CAST(timestamp as bigint) as pt_timestamp ";
		return sincroGetSetDinServer(tbLocala, tbServer, colSincroServer, idDevice, faraBlocate,cFiltru,nLastTime);
	}

	
	
	// creeaza cursorul cu datele de preluat din server
	public ResultSet sincroGetSetDinServer(String tbLocala,String tbServer,String colSincroServer, int idDevice, boolean farablocate,
		String cWhere,long nLastTime) {
		long nTime=nLastTime;
		String sqlSir ="";
		ResultSet res=null;
		String cFiltru="";
		Log.d("sinc:","Ininte de query pt "+tbLocala+"  nTimestamp="+nTime);
		// daca idDevice=-1 se preia toata tabela fara exceptie
		if (nLastTime!=0) {
			Cursor liteCrs = this.db.query(	tbLocala,new String[] {"max(s_timestamp) as maxtime"}, null, null, null, null, null);
			if (liteCrs.getCount()>0) {
				liteCrs.moveToFirst();
				nTime=liteCrs.getInt(0);
			}
			liteCrs.close();
		}		
		cFiltru=" "+
				(farablocate ? " AND blocat=0 ": "")+
				(cWhere.equals("") ? "" : " AND "+cWhere);

		if (idDevice==-1) {
			sqlSir="SELECT "+colSincroServer+" FROM "+
						tbServer+
						" WHERE "+
						"cast(timestamp as integer) > "+nTime+
						cFiltru;
		} else {
			sqlSir="SELECT "+colSincroServer+" FROM "+
						tbServer+
						" WHERE id_device=" +idDevice+
						" and "+"cast(timestamp as integer) > "+nTime
						+cFiltru;
		}
		Log.d("sinc:","Inainte de select server "+sqlSir);
		// se preiau din server inreg pt sincronizare
		res=sqldb.query(sqlSir);
		Log.d("sinc:","Dupa select server "+sqlSir);
		
		return res;
	}
	// se pune 1 la id_masina astfel incat programul de preluare sa stie la ce agent sa lucreze
	public void sincro_activeaza_agent () {
		String sqlSir="UPDATE agent SET id_masina=1 WHERE id_device=" +idDevice;
		sqldb.exec(sqlSir);
	}
	
	// determina vloarea maxima a timestamp aferent serverului din baza locala
	public long getmaxServerTimeDinLocal (String ctabela) {
		Cursor liteCrs = this.db.query(	ctabela,new String[] {"max(s_timestamp) as maxtime"}, null, null, null, null, null);
		long nMax=0;
		if (liteCrs!=null) {
			if (liteCrs.getCount()>0) {
				liteCrs.moveToFirst();
				nMax=liteCrs.getLong(liteCrs.getColumnIndex("maxtime"));
			}
			liteCrs.close();
		}
		return nMax;
	}
	
	public void sincroPreiaDinServer(String tbLocala, String tbServer, String[][] struct, int idAgent,String cFiltru) {
		sincroPreiaDinServer(tbLocala,tbServer,struct,0,idAgent,cFiltru) ;
	}
	
	public void sincroPreiaDinServer(String tbLocala, String tbServer, String[][] struct, long nLastTime, int idAgent,String cFiltru) {
		// se extrag inreg cu timestamp mai mare din server 
		// unele se adauga altele se modifica
		// in set se regaseste si coloana timestamp cu numele pt_timestamp
		ResultSet res=sincroGetSetDinServer(tbLocala, tbServer,struct, idAgent,false,cFiltru,nLastTime);
		Log.d("PRO","dupa revenirea din getset");
		if (res!=null) {
			db.beginTransaction();
			try {
				ContentValues cval=new ContentValues();
				while (res.next()) {
					for (int i = 0; i < struct.length; i++) {
						if (struct[i].length>2) {
						// se exclude _id
						try {
							if(!struct[i][0].toUpperCase().equals("_ID")) {

								if (struct[i][3].equals(STypes.INTREG)) {
									cval.put(struct[i][0], res.getLong(struct[i][2]));
                                    Log.d("PRO&"," Camp="+struct[i][0]+" val="+res.getLong(struct[i][2]));
                                } else if (struct[i][3].equals(STypes.DATA)) {
									cval.put(struct[i][0], res.getString(struct[i][2]));
                                    Log.d("PRO&"," Camp="+struct[i][0]+" val="+res.getString(struct[i][2]));
								} else if (struct[i][3].equals(STypes.DTOS)) {
									cval.put(struct[i][0], res.getString(struct[i][2]));
                                    Log.d("PRO&"," Camp="+struct[i][0]+" val="+res.getString(struct[i][2]));
								} else if (struct[i][3].equals(STypes.STRING)) {
									cval.put(struct[i][0], res.getString(struct[i][2]));
                                    Log.d("PRO&"," Camp="+struct[i][0]+" val="+res.getString(struct[i][2]));
								} else if (struct[i][3].equals(STypes.VALOARE)) {
									cval.put(struct[i][0], res.getDouble(struct[i][2]));
                                    Log.d("PRO&"," Camp="+struct[i][0]+" val="+res.getDouble(struct[i][2]));
								}

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

                        }
						}
					}
					cval.put("s_timestamp", res.getLong("pt_timestamp"));
					long nid=res.getLong("cod_int");
					Cursor crs= db.rawQuery("select _id from "+tbLocala+" where _id="+nid,null);

                    if (crs.getCount()==1) {
						// pentru modif
						db.update(tbLocala, cval, "_ID="+nid, null);
                        Log.d("PRO&","Valori update:"+cval.toString());
                    } else {
						// pentru adaugare
						cval.put("_ID", nid);
                        long nRez=db.insert(tbLocala, null, cval);
                        Log.d("PRO&", "Rezultat insert in :"+tbLocala+"="+nRez);
                        Log.d("PRO&", "Valori insert:"+cval.toString());
                    }
                    cval.clear();
					crs.close();

                }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
                Log.d("PRO&","Eroare la preluare din server"+e.getMessage());
				e.printStackTrace();
			}
			db.setTransactionSuccessful();
			db.endTransaction();

			try {
				res.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
            Log.d("PRO"," res = null");
        }
		
	}
	
	private ContentValues setval (String[][] str , ResultSet res) throws SQLException {
		ContentValues cVal=new ContentValues();
		for (int i = 0; i < str.length; i++) {
			if (str[i].length>2) {
				if (str[i][3].equals(STypes.INTREG)) {
					cVal.put(str[i][0], res.getInt(str[i][2]));
				} else if (str[i][3].equals(STypes.DATA)) {
					cVal.put(str[i][0], res.getString(str[i][2]));
				} else if (str[i][3].equals(STypes.DTOS)) {
					String sval=res.getString(str[i][2]);
					sval=sval.substring(0,4)+sval.subSequence(5, 7)+sval.substring(8, 10);
					cVal.put(str[i][0], sval);
				} else if (str[i][3].equals(STypes.STRING)) {
					cVal.put(str[i][0], res.getString(str[i][2]));
				} else if (str[i][3].equals(STypes.VALOARE)) {
					cVal.put(str[i][0], res.getDouble(str[i][2]));
				}
			}
		}
		return cVal;	
	}
    // sincronizare totala tabela in functie de timestamp din server si de flag din local
    // - tabela trebuie sa aiba declarata obligatoriu structura
    // - PREIA DIN S
    // - se preiau inreg din server cu timestamp > decat maximul stocat in tabela locala in campul s_timestamp
    // - inreg pot fi noi sau existente ( se verifica daca _id - ul local exista printre cele din server
    // -    daca exista se face modif daca nu adaugare
    // - pentru toate inreg din server se pune flag u
    // - TRANSNMITE IN S
    // - se preiau inreg din local crea au a sau m la flag
    // - se preia din server o lista de iduri care se regasesc in lista de mai sus
    // - se face modif pt acestea si adaugare pt celelalte
    // - la inreg modif se trece u la la flag si timestamp din server ca sa nu mai fie luate inca o data la sincronizare
    // - se intorc anumite coduri privind realizarea executiei
    // - 0 - executie corecta
    // - daca se trimite idDevice >0 este obligatoriu ca tabelele sa aiba in structura id_device
    // - daca se trimite idDevice=-1 se transfera tabela netinand cont de iddevice

//    public int sincroTotalTabela (String sTbLocal, String sTbServer,  String[][] str, int idDevice, String cFiltru ) {
//        return sincroTotalTabela(sTbLocal,sTbServer,sTbServer,str,idDevice,cFiltru);
//    }

//    public int sincroTotalTabela (String sTbLocal, String sTbServerAdaug,String sTbServerPreia,  String[][] str, int idDevice, String cFiltru ) {
//        // PREIA DIN SERVER
//        // se det maximul de timestamp aferent inreg care au venit dinerver
//        int nRez=0;
//        try {
//            sincroAdaugInServerNou(sTbLocal, sTbServerAdaug, str, cFiltru);
//            Log.d("PRO","Dupa adaug");
////            sincroPreiaDinServer(sTbLocal, sTbServerPreia, str, idDevice, cFiltru);
//            Log.d("PRO","Dupa transmit");
//        } catch (Exception e) {
//            nRez=1;
//        }
//        return nRez;
//    }

}

