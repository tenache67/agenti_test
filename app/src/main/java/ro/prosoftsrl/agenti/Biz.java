package ro.prosoftsrl.agenti;
// idAntet pentru documente soeciale
// -1 gen aviz inc
// -2 gen aviz stoc
// -3 gen transfer am 


import java.sql.ResultSet;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import ro.prosoftsrl.agenthelper.*;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Agent;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Ambalaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Discount;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_PozAmbalaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_SoldCurentAmbalaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Soldpart;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempContinutDocumente;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Tipdoc;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Cod_Bare ;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Types;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.Siruri;


public class Biz {
	
	// determina sql pt soldul pe ambalaje pe client si agent
	public static String getSqlSoldAmbalajeClient (long id_part,long id_agent) {
		String sData1=Siruri.dtos(Calendar.getInstance(),"-");
		String sData2=Siruri.dtos(Calendar.getInstance(),"-");
		String scmd=
			 " SELECT "+
					Table_Ambalaje.TABLE_NAME+"."+Table_Ambalaje.COL_DENUMIRE+","+
					"sum(ad.cantitate) as cantitate "+
				" FROM "+Table_Ambalaje.TABLE_NAME+
				" INNER JOIN "+
				" ( SELECT "+
						Table_SoldCurentAmbalaje.TABLE_NAME+"."+Table_SoldCurentAmbalaje.COL_ID_AMBALAJ+","+
						Table_SoldCurentAmbalaje.TABLE_NAME+"."+Table_SoldCurentAmbalaje.COL_CANTITATE+
					" FROM "+Table_SoldCurentAmbalaje.TABLE_NAME+
					" WHERE "+Table_SoldCurentAmbalaje.TABLE_NAME+"."+Table_SoldCurentAmbalaje.COL_ID_CLIENT+"="+id_part+
					" AND "+Table_SoldCurentAmbalaje.TABLE_NAME+"."+Table_SoldCurentAmbalaje.COL_ID_AGENT+"="+id_agent+
					" UNION ALL "+
					" SELECT "+
						Table_PozAmbalaje.TABLE_NAME+"."+Table_PozAmbalaje.COL_ID_AMBALAJ+","+
						Table_PozAmbalaje.TABLE_NAME+"."+Table_PozAmbalaje.COL_CANTITATE_DAT+"-"+
						Table_PozAmbalaje.TABLE_NAME+"."+Table_PozAmbalaje.COL_CANTITATE_LUAT+" as "+
						Table_SoldCurentAmbalaje.COL_CANTITATE+
					" FROM "+Table_PozAmbalaje.TABLE_NAME+
					" INNER JOIN "+Table_Antet.TABLE_NAME+" ON "+Table_PozAmbalaje.TABLE_NAME+"."+Table_PozAmbalaje.COL_ID_ANTET+"="+
						Table_Antet.TABLE_NAME+"."+Table_Antet._ID+
					" WHERE "+
					" SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)>='"+sData1+"'"+
					" AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)<='"+sData2+"'";
		// daca id_part < 0 este soldul masinii
					if (id_part>0) {
						scmd=scmd+" AND "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_PART+"="+id_part;
					} 
					scmd=scmd+" AND "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_AGENT+"="+id_agent +") ad "+
				" on "+Table_Ambalaje.TABLE_NAME+"."+Table_Ambalaje._ID+"= ad."+Table_PozAmbalaje.COL_ID_AMBALAJ+
				" GROUP BY "+Table_Ambalaje.TABLE_NAME+"."+Table_Ambalaje._ID ;
		Log.d("PRO","Select sold ambalaje: "+scmd);
		return scmd;
	}
	
	public static String getSqlCautaSablon (long id_part,long id_agent, long id_ruta , int id_cursa) {
		String scmd=
			"SELECT "+
				Table_Sablon_Antet._ID+
			" FROM "+Table_Sablon_Antet.TABLE_NAME+
			" WHERE "+
				Table_Sablon_Antet.COL_ID_PART+"="+id_part+" and "+
				Table_Sablon_Antet.COL_ID_AGENT+"="+id_agent+" and "+
				Table_Sablon_Antet.COL_ID_RUTA+"="+id_ruta+" and "+
				Table_Sablon_Antet.COL_ID_CURSA+"="+id_cursa;
		Log.d("CAUTASAB",scmd);
		return scmd;
	}
	
	// pune o valoare potrivita pentru o coloana din baza de date. Se da un string cu structura si un string pt valoare
	// si o val pt content
    // returneaza un contentval care se va adauga cu putall la contentul mare
	public static ContentValues putValPtColoana (String sColName, String [][] str, String sval) {
		// se cauta pozitia pt colana
        ContentValues cval =new ContentValues();
		String sTip="";
		for (int i = 0; i < str.length; i++) {
			if (sColName.equals(str[i][0].toUpperCase())) {
				sTip=str[i][1];
				Log.d("PUTVAL","Tip:"+sTip+"  Val de pus:"+sval+" in coloana:"+sColName);
				break;
			}
		}
		
		if (sTip.equals(Types.DATE)) 
			cval.put(sColName, sval);
		else if (sTip.equals(Types.INTREG))
			cval.put(sColName, Siruri.getIntDinString(sval));
		else if (sTip.equals(Types.TEXT))
			cval.put(sColName, sval);
		else if (sTip.equals(Types.TIMESTAMP))
			cval.put(sColName, sval);
		else if (sTip.equals(Types.VALOARE))
			cval.put(sColName, Siruri.getDoubleDinString(sval));
        return cval;
	}
	
	// determina tipurile coloanelor din tabela
	public static String[][] getStruct (ColectieAgentHelper colectie, String tbName) {
		Cursor crs = colectie.getReadableDatabase().rawQuery("PRAGMA table_info("+tbName+")",null);
		if (crs.getCount()>0) {
			String[][] str =new String[crs.getCount()-1][2]; 
			crs.moveToFirst();
			for (int i=0; i< crs.getCount();i++) {
				str[i][0]=crs.getString(crs.getColumnIndexOrThrow("name"));
				str[i][1]=crs.getString(crs.getColumnIndexOrThrow("type"));
			}
			crs.close();
			return str;
		} else {
			crs.close();
			return null;
		}
	}
	
	//pune numarul de document urmator in preference. Daca se da sNumarDoc se foloseste acesta
	// daca nu , se foloseste numarul curent
	public static void setNumarCrtDoc (Context context,String sTipDoc, int nNumarDoc) {
		int nNumar= nNumarDoc==0 ? Biz.getNumarCrtDoc(context, sTipDoc)+1 : nNumarDoc+1;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		int idPref=Biz.TipDoc.getIdPrefDoc(sTipDoc);
		Log.d("SETNR",""+idPref);
		Log.d("SETNR","Tip doc="+sTipDoc);
		if (idPref>0) {
			Log.d("SETNR","Numar="+nNumar);
			Log.d("SETNR","String="+context.getString(idPref));
			settings.edit().putString(context.getString(idPref),""+nNumar ).commit();
			Log.d("SETNR","Salvat="+settings.getString(context.getString(idPref), "?"));
		}
	}
	
	// determina numarul curent de document in fuinctie de tipul documentului 
	public static int getNumarCrtDoc (Context context,String sTipDoc) {
		int iRez=1,iRes=0;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		iRes=Biz.TipDoc.ID_PREF_DOCCRT[Biz.TipDoc.getTipDocPoz(sTipDoc)];
		if (iRes>0)
			iRez= Integer.valueOf(settings.getString(context.getString(Biz.TipDoc.ID_PREF_DOCCRT[Biz.TipDoc.getTipDocPoz(sTipDoc)]), "1"));
		return iRez;
	}
	
	//determina id_tip_doc in functie de denumire
	//nTipComanda = 1, nTipFactura = 2, nTipAvizCli = 3, nTipAvizDes = 4, nTipAvizInc = 5
	public static long getIdTipDoc(String denumire) {
		return Biz.TipDoc.ID_TIPDOC[Biz.TipDoc.getTipDocPoz(denumire)];
	}

	
	// creeaza un sir pt folosit in select de genul tbname.camp as tbname_camp , 
	// daca lFinal=true nu se mai adauga virgula la ultima poz
	public static String getPtSelect (String tbName , String[] campuri, Boolean lFinal) {
		String sir="";
		for (int i = 0; i < campuri.length; i++) {
			sir=sir+tbName+"."+campuri[i]+" as "+tbName+"_"+campuri[i]+",";
		}
		if (sir.length()>1 && lFinal) sir=sir.substring(0,sir.length()-1);
		return sir;
	}
	// determina sql pentru preluarea imaginea documentului salvat pe baza idului de antet
	public static String getSqlImagineDoc (Long nIdantet) {
		String sir;
		sir ="SELECT  "+
			Biz.getPtSelect(Table_Antet.TABLE_NAME, 
					new String[] {
					Table_Antet.COL_CORESP,
					Table_Antet.COL_DATA,
					Table_Antet.COL_DATA_SCAD,
					Table_Antet.COL_ID_AGENT,
					Table_Antet.COL_ID_AUTO,
					Table_Antet.COL_ID_MODPL,
					Table_Antet.COL_ID_PART,
					Table_Antet.COL_ID_TIPDOC,
					Table_Antet.COL_INCASAT,
					Table_Antet.COL_NR_DOC,
					Table_Antet.COL_NR_CHITANTA,
					Table_Antet.COL_TERM_PL,
					Table_Antet.COL_VAL_FARA,
                    Table_Antet.COL_LISTAT ,
					Table_Antet.COL_VAL_TVA,
					}
					, false)+
			Biz.getPtSelect(Table_Clienti.TABLE_NAME, 
					new String[]{
					Table_Clienti.COL_DENUMIRE,
					Table_Clienti.COL_ADRESA,
					Table_Clienti.COL_BANCA,
					Table_Clienti.COL_CONT,
					Table_Clienti.COL_JUDET,
					Table_Clienti.COL_LOC,
					Table_Clienti.COL_NR_FISc,
					Table_Clienti.COL_NR_RC			
			}, false)+
			Biz.getPtSelect(Table_Pozitii.TABLE_NAME,
					new String[]{
					Table_Pozitii.COL_CANTITATE,
					Table_Pozitii.COL_COTA_TVA,
					Table_Pozitii.COL_ID_PRODUS,
					Table_Pozitii.COL_ID_UM,
					Table_Pozitii.COL_PRET_CU,
					Table_Pozitii.COL_PRET_FARA,
					Table_Pozitii.COL_PRET_CU1,
					Table_Pozitii.COL_PRET_FARA1,
					Table_Pozitii.COL_PROC_RED,
					Table_Pozitii.COL_TVA_RED,
					Table_Pozitii.COL_VAL_FARA,
					Table_Pozitii.COL_VAL_RED,
					Table_Pozitii.COL_VAL_TVA
			}, false)+
				"IFNULL("+Table_Cod_Bare.TABLE_NAME+"."+Table_Cod_Bare.COL_COD+","+"'            '"+") as "+Table_Cod_Bare.TABLE_NAME+"_"+Table_Cod_Bare.COL_COD+ ","+
			Biz.getPtSelect(Table_Produse.TABLE_NAME,
					new String[] {
					Table_Produse.COL_DENUMIRE ,
					Table_Produse.COL_SUPLIM1,
					Table_Produse.COL_SUPLIM2,
					Table_Produse.COL_SUPLIM3,
					Table_Produse.COL_SUPLIM4

			}, true)+
			" FROM "+Table_Antet.TABLE_NAME +
				" INNER JOIN "+Table_Clienti.TABLE_NAME+" ON "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_PART+" = "
					+Table_Clienti.TABLE_NAME+"."+Table_Clienti._ID+
				" INNER JOIN "+Table_Pozitii.TABLE_NAME+" ON "+Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" = "
					+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_ANTET+
				" INNER JOIN "+Table_Produse.TABLE_NAME+" ON "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+" = "
					+Table_Produse.TABLE_NAME+"."+Table_Produse._ID+
				" LEFT JOIN "+ "( select distinct b.id_produs,b.id_part, (select cb.cod from cod_bare cb where cb.id_produs=b.id_produs and  cb.id_part=b.id_part limit 1 ) as cod from cod_bare b) as "+Table_Cod_Bare.TABLE_NAME +" ON "+
					Table_Cod_Bare.TABLE_NAME+"."+ Table_Cod_Bare.COL_ID_PRODUS+" = " +Table_Produse.TABLE_NAME+"."+Table_Produse._ID+
					" and "+Table_Cod_Bare.TABLE_NAME+"."+Table_Cod_Bare.COL_ID_CLIENT+"="+Table_Clienti.TABLE_NAME+"."+Table_Clienti.COL_ID_PART+
			" WHERE "+Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" = "+nIdantet+
				" AND "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE+">0 "+
			" ORDER BY " +
				Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE ;
		Log.d("SQLIMAGINEDEOC",sir);
		return sir;
	}
	
	public static ContentValues getInsertPozitiiSablon (Bundle arg) {
		ContentValues val =new ContentValues();
		val.put(Table_Sablon_Pozitii._ID, arg.getLong("_id"));
		val.put(Table_Sablon_Pozitii.COL_ID_ANTET, arg.getLong(Table_Sablon_Pozitii.COL_ID_ANTET));
		val.put(Table_Sablon_Pozitii.COL_ID_PRODUS, arg.getLong(Table_Sablon_Pozitii.COL_ID_PRODUS));
		val.put(Table_Sablon_Pozitii.COL_CANTITATE, arg.getDouble(Table_Sablon_Pozitii.COL_CANTITATE));
		val.put(Table_Sablon_Pozitii.COL_DIFERENTE, arg.getDouble(Table_Sablon_Pozitii.COL_DIFERENTE));
		return val;
		
	}
	
	//determina sirul de update pt continutul documentului in fisierul pozitii
	public static ContentValues getInsertPozitii (Bundle arg) {
		boolean lFaraRedInFact=true; // ca sa nu se vada reducerea in factura
		ContentValues val =new ContentValues();
		val.put(Table_Pozitii._ID, arg.getLong("_id"));
		val.put(Table_Pozitii.COL_ID_ANTET, arg.getLong(Table_Pozitii.COL_ID_ANTET));
		val.put(Table_Pozitii.COL_ID_PRODUS, arg.getLong(Table_Pozitii.COL_ID_PRODUS));
        val.put(Table_Pozitii.COL_ID_FA1,arg.getLong(Table_Pozitii.COL_ID_FA1));
        val.put(Table_Pozitii.COL_ID_FA2,arg.getLong(Table_Pozitii.COL_ID_FA2));
        val.put(Table_Pozitii.COL_BONUS,arg.getInt(Table_Pozitii.COL_BONUS));
		int nBonus=arg.getInt(Table_TempContinutDocumente.COL_C_ESTE_BONUS);
		int nFaraPret=arg.getInt(Table_TempContinutDocumente.COL_FARA_PRET);
		int nCuTVA=arg.getInt("cu_tva");
		double nPretFara=arg.getDouble(Table_Pozitii.COL_PRET_FARA);
		double nPretCu=arg.getDouble(Table_Pozitii.COL_PRET_CU);
		double nPret=arg.getDouble("pret");
		if (lFaraRedInFact && nPret!=0) nPretCu=nPret;
		double nCantitate=arg.getDouble(Table_Pozitii.COL_CANTITATE);
		double nCotaTva=arg.getDouble(Table_Pozitii.COL_COTA_TVA);
		double nProcRed=0.0;
		double nValBaza=0.0;
		double nValTva=0.0;
		double nValRed=0.0;
		double nTvaRed=0.0;
		// in npret este pretul redus cu sau fara tva. in pret_fara si pret_cu sunt preturile nereduse
		// se determina procentul de reducere
		if ((nBonus==0) && (nPret!=-1) && (nFaraPret==0)) {
			if (nCuTVA==1) {
				nPretFara=Biz.round(nPretCu/(1+nCotaTva/100), Biz.ConstCalcul.ZEC_PRET_FARA);
				nValBaza=Biz.round(nCantitate*nPretFara,Biz.ConstCalcul.ZEC_VAL_FARA);
				nValTva=Biz.round(nCantitate*nPretFara*nCotaTva/100, Biz.ConstCalcul.ZEC_VAL_TVA);
				// procentul la pretul cu tva
				if (nPretCu!=0 && nPret>0) {
					nProcRed=Biz.round((1-nPret/nPretCu)*100,Biz.ConstCalcul.ZEC_PRET_FARA);
					nValRed=Biz.round(nCantitate*(nPretCu-nPret), Biz.ConstCalcul.ZEC_VAL_FARA);
					nTvaRed=Biz.round(nValRed-nValRed/(1+nCotaTva/100), Biz.ConstCalcul.ZEC_VAL_TVA);
					nValRed=nValRed-nTvaRed;
				}
			} else {
				nPretCu=Biz.round(nPretFara*(1+nCotaTva/100), Biz.ConstCalcul.ZEC_PRET_FARA);
				nValBaza=Biz.round(nCantitate*nPretFara,Biz.ConstCalcul.ZEC_VAL_FARA);
				nValTva=Biz.round(nCantitate*nPretFara*nCotaTva/100, Biz.ConstCalcul.ZEC_VAL_TVA);
				// procentul la pretul fara tva
				if (nPretFara!=0 && nPret>0) {
					nProcRed=Biz.round((1-nPret/nPretFara)*100,Biz.ConstCalcul.ZEC_PRET_FARA);
					nValRed=Biz.round(nCantitate*(nPretFara-nPret), Biz.ConstCalcul.ZEC_VAL_FARA);
					nTvaRed=Biz.round(nValRed*(nCotaTva/100), Biz.ConstCalcul.ZEC_VAL_TVA);
				}
			}
			// daca preturile alternative sun 0 se pun preturile de vanzare ca sa iasa discount 0
			if (arg.getDouble(Table_Pozitii.COL_PRET_FARA1)==0)
				val.put(Table_Pozitii.COL_PRET_FARA1,nPretFara);
			else
				val.put(Table_Pozitii.COL_PRET_FARA1, arg.getDouble(Table_Pozitii.COL_PRET_FARA1));
			if (arg.getDouble(Table_Pozitii.COL_PRET_CU1)==0)
				val.put(Table_Pozitii.COL_PRET_CU1,nPretCu);
			else
				val.put(Table_Pozitii.COL_PRET_CU1, arg.getDouble(Table_Pozitii.COL_PRET_CU1));
			
		} else {
			nPretFara=0.0;
			nPretCu=0.0;
			val.put(Table_Pozitii.COL_PRET_FARA1,0.0);
			val.put(Table_Pozitii.COL_PRET_CU1,0.0);
		}
		val.put(Table_Pozitii.COL_CANTITATE, nCantitate);
		val.put(Table_Pozitii.COL_COTA_TVA, nCotaTva);
		val.put(Table_Pozitii.COL_PRET_FARA,nPretFara);
		val.put(Table_Pozitii.COL_PRET_CU, nPretCu);
		
		val.put(Table_Pozitii.COL_PROC_RED, nProcRed);
		val.put(Table_Pozitii.COL_VAL_FARA,nValBaza);
		val.put(Table_Pozitii.COL_VAL_TVA,nValTva);
		val.put(Table_Pozitii.COL_VAL_RED,nValRed);
		val.put(Table_Pozitii.COL_TVA_RED,nTvaRed);
		return val;
		
	}
	
	//determina contentvalues pt insert in antet
	public static ContentValues getInsertAntet (Bundle arg) {
		ContentValues val=new ContentValues();
		val.put(Table_Antet._ID, arg.getLong(Table_Antet._ID));
		val.put(Table_Antet.COL_CORESP, arg.getString(Table_Antet.COL_CORESP));
		val.put(Table_Antet.COL_DATA, arg.getString(Table_Antet.COL_DATA));
		val.put(Table_Antet.COL_ID_AGENT, arg.getLong(Table_Antet.COL_ID_AGENT, 0));
		val.put(Table_Antet.COL_ID_DEVICE, arg.getLong(Table_Antet.COL_ID_DEVICE,0));
		val.put(Table_Antet.COL_ID_MODPL, arg.getLong(Table_Antet.COL_ID_MODPL,0));
		val.put(Table_Antet.COL_ID_PART, arg.getLong(Table_Antet.COL_ID_PART,0));
		val.put(Table_Antet.COL_ID_TIPDOC, arg.getLong(Table_Antet.COL_ID_TIPDOC,0));
		val.put(Table_Antet.COL_INCASAT, arg.getDouble(Table_Antet.COL_INCASAT, 0.00));
		val.put(Table_Antet.COL_LISTAT, arg.getInt(Table_Antet.COL_LISTAT, 0));
		val.put(Table_Antet.COL_NR_DOC, arg.getString(Table_Antet.COL_NR_DOC));
		val.put(Table_Antet.COL_NR_CHITANTA, arg.getString(Table_Antet.COL_NR_CHITANTA));
		val.put(Table_Antet.COL_TERM_PL, arg.getInt(Table_Antet.COL_TERM_PL,0));
		val.put(Table_Antet.COL_VAL_FARA, arg.getDouble(Table_Antet.COL_VAL_FARA,0.00));
		val.put(Table_Antet.COL_VAL_TVA, arg.getDouble(Table_Antet.COL_VAL_TVA,0.00));
		return val;
	}

	//determina sirul sql pt aflarea soldului unui partener pe baza idului
	public static String getSqlSoldPart (long _id) {
		Log.d("SOLD","Id="+_id);
		String sqlSir =
				" SELECT "+
						"ptsold._id"+","+
						"sum(ptsold.sold)+sum(ptsold.rulaj) as soldc "+
				" FROM "+
				" ( SELECT "+
						Table_Soldpart.TABLE_NAME+"."+Table_Soldpart.COL_ID_PART +" as _id ,"+
						Table_Soldpart.TABLE_NAME+"."+Table_Soldpart.COL_VAL_INI+" as sold ,"+
						"00000000.0000 as rulaj "+
				" FROM "+Table_Soldpart.TABLE_NAME+
				" WHERE "+Table_Soldpart.TABLE_NAME+"."+Table_Soldpart.COL_ID_PART+"="+_id+
				" UNION ALL "+
				" SELECT "+
						Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_PART+" as _id ,"+
						"00000000.0000 as sold ,"+
						Table_Antet.TABLE_NAME+"."+Table_Antet.COL_VAL_FARA+
							"+"+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_VAL_TVA+
							"-"+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_INCASAT +" as rulaj "+
				" FROM "+Table_Antet.TABLE_NAME+
				" WHERE "+Table_Antet.TABLE_NAME+'.'+Table_Antet.COL_ID_PART+" = "+_id+" AND "+
					Table_Antet.TABLE_NAME+"."+Table_Antet.COL_FARA_SOLD+"=0 "+" ) as ptsold" +
				" GROUP BY 1 ";
		Log.d("SOLD",sqlSir);
		return sqlSir;
	}	

	//determina valoare totala a documentului
	public static double getTotalCuTVA (Cursor crsc,Boolean lPvCuTva) {
		double nval=(double) 0.00;
		if (crsc.getCount()>0) {
			crsc.moveToFirst();
			Log.d("ACTIUNI","1");
			while (!crsc.isAfterLast()) {
				double ncant=crsc.getDouble(crsc.getColumnIndexOrThrow("cantitate"));
				double npret=crsc.getDouble(crsc.getColumnIndexOrThrow("pret"));
				double ncota=crsc.getDouble(crsc.getColumnIndexOrThrow("cota_tva"));
				nval=nval+Biz.getValfaraTVA(ncant, npret, ncota, lPvCuTva)+Biz.getValTVA(ncant, npret, ncota, lPvCuTva);
				crsc.moveToNext();
			}
		}				
		return nval;		
	}
	
	//determina numai valoarea tva din cursor de continut
	public static double getTotalTva (Cursor crsc,Boolean lPvCuTva) {
		double nval=(double) 0.00;
		if (crsc.getCount()>0) {
			crsc.moveToFirst();
			Log.d("ACTIUNI","1");
			while (!crsc.isAfterLast()) {
				double ncant=crsc.getDouble(crsc.getColumnIndexOrThrow("cantitate"));
				double npret=crsc.getDouble(crsc.getColumnIndexOrThrow("pret"));
				double ncota=crsc.getDouble(crsc.getColumnIndexOrThrow("cota_tva"));
				nval=nval+Biz.getValTVA(ncant, npret, ncota, lPvCuTva);
				crsc.moveToNext();
			}
		}				
		return Biz.round(nval, Biz.ConstCalcul.ZEC_VAL_CU) ;
		
	}
	
	//determina valoarea fara tva din cursor de continut
	public static double getTotalFara (Cursor crsc,Boolean lPvCuTva) {
		double nval=(double) 0.00;
		if (crsc.getCount()>0) {
			crsc.moveToFirst();
			Log.d("ACTIUNI","1");
			while (!crsc.isAfterLast()) {
				double ncant=crsc.getDouble(crsc.getColumnIndexOrThrow("cantitate"));
				double npret=crsc.getDouble(crsc.getColumnIndexOrThrow("pret"));
				double ncota=crsc.getDouble(crsc.getColumnIndexOrThrow("cota_tva"));
				nval=nval+Biz.getValfaraTVA(ncant, npret, ncota, lPvCuTva);
				crsc.moveToNext();
			}
		}				
		return nval;
	}
	
	//determina valoarea tva din cantitate si pret
	public static double getValTVA(double ncant, double npret, double nCota, Boolean lPretCuTva) {
		if (lPretCuTva)
			// se extrage tva din valoare cu pret cu tva
			return Biz.round(ncant*npret, Biz.ConstCalcul.ZEC_VAL_CU)-Biz.getValfaraTVA(ncant, npret, nCota, lPretCuTva);
		else
			// se determina tva la valoarea cand se da pretul fara tva
			return Biz.round(ncant*npret*nCota/100, ConstCalcul.ZEC_VAL_TVA);
	}
	
	// determina pretul cu tva din fara tva
	public static double getPretCuTVA (double nPret, double nCota, int nZec) {
		return Biz.round(nPret*(1+nCota/100), nZec);
	}
	
	// extrage pretul fara tva din pretul cu tva
	public static double getPretFaraTVA (double nPret, double nCota, int nZec) {
		return Biz.round(nPret/(1+nCota/100),  nZec);
	}
	// determina valoarea fara tva di cant si pret cu indicatia privind tipul pretului 
	public static double getValfaraTVA ( double ncant, double npret, double nCota, Boolean lPretCuTva) {
		double nVal=0.0;
		if (lPretCuTva)
			// pretul este cu tva
			nVal=Biz.round(ncant*Biz.getPretFaraTVA(npret, nCota, Biz.ConstCalcul.ZEC_PRET_FARA) , Biz.ConstCalcul.ZEC_VAL_FARA);
		else
			nVal=Biz.round(ncant*npret, 4);
		return nVal; 
	}
	
	//efectueaza rotunjire
	public static double round (double nVal, int nZec) {
		return (double) Math.round(nVal*Math.pow(10, nZec))/Math.pow(10, nZec);
	}
	//calculeaza valoarea din cantitate si pret 
	public static double getValDinCantSiPret( double cant, double pret) {
		return Biz.round(cant*pret,4);
	}
	
	// determina tipul de pret de vanzare folosit pt afisare in diverse liste din preferinte
	public static Boolean pretCuTva (Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(context.getString(R.string.key_ecran4_pret_vanzare), true);
	}
	
	
	//determina instructiunea de update pt actualizarea tabelei temporare la continutul unui document
	public static ContentValues getUpdateContinutDocument (Cursor crs, Long idProdus , double cant, int nBonus) {
		ContentValues con = new ContentValues();
		Long _id=(long) 0;
		if (crs.getCount()>0) {
			crs.moveToFirst();
			//se cauta in continut o pozitie corespunzatoare pt linia curenta dupa id_produs si este_bonus
			while (!crs.isAfterLast()) {
				if (crs.getLong(crs.getColumnIndexOrThrow("id_produs"))==idProdus && 
						crs.getInt(crs.getColumnIndexOrThrow("este_bonus"))==nBonus ) {
					_id=crs.getLong(crs.getColumnIndexOrThrow("_id"));
				}
				crs.moveToNext();
			}
		}
		if (_id!=0)	con.put(Table_TempContinutDocumente._ID,_id);
		con.put(Table_TempContinutDocumente.COL_ID_PRODUS, idProdus);
		con.put(Table_TempContinutDocumente.COL_CANTITATE,cant);
		con.put(Table_TempContinutDocumente.COL_C_ESTE_BONUS, nBonus);
				
		return con;
//		String sqlsir = "INSERT OR REPLACE INTO "+
//				Table_TempContinutDocumente.TABLE_NAME +
//				" ("+Table_TempContinutDocumente._ID+","+Table_TempContinutDocumente.COL_ID_PRODUS+","+
//					Table_TempContinutDocumente.COL_CANTITATE+")"+" VALUES "+" ( "+
//				_id+","+idProdus+","+cant+" )";
//		return sqlsir;
	}

	public static Cursor getCursorListaDenumiri (ColectieAgentHelper colectie,int iTLD, long iIdMaster, long iIdClient, Boolean lPVcuTVA,int nTipTva) {
		Cursor crs=colectie.getReadableDatabase().rawQuery(Biz.getSqlListaDenumiri(iTLD,iIdMaster,iIdClient,lPVcuTVA,nTipTva), null);
		Log.d("GETCURSOR","1");
		return crs;
	}
	
	// determina cursorul pentru cautarea in lista de denumiri
	public static Cursor getCursorListaDenumiri (ColectieAgentHelper colectie,int iTLD, long iIdMaster, long iIdClient, Boolean lPVcuTVA) {
//		SQLiteDatabase db=colectie.getReadableDatabase();
		Cursor crs=colectie.getReadableDatabase().rawQuery(Biz.getSqlListaDenumiri(iTLD,iIdMaster,iIdClient,lPVcuTVA,-1), null);
//		db.close();
		return crs;
	}
	
	
	//determina instr sql pt cursorul de denumiri in liste in functie de id client si dicounturi
	public static String getSqlListaDenumiri(int iTLD, long iIdMaster, long iIdClient, Boolean lPVcuTVA,int nTipTva) {
		String sqlSir="";
		String sData1=Siruri.dtos(Calendar.getInstance(),"-");
		String sData2=Siruri.dtos(Calendar.getInstance(),"-");
		String hhhh = "";
		Log.d("PEAICI","iTLD= "+iTLD);
		switch (iTLD) {
       //pentru extragere info din client_agent
            // iIdMaster - id agent , iIdClient - id client
            case TipListaDenumiri.TLD_CLIENT_AGENT: {
                sqlSir = "SELECT " +
                        ColectieAgentHelper.Table_Client_Agent._ID + "," +
                        ColectieAgentHelper.Table_Client_Agent.COL_ID_RUTA + "," +
                        ColectieAgentHelper.Table_Client_Agent.COL_BLOCAT + "," +
                        ColectieAgentHelper.Table_Client_Agent.COL_C_TIMESTAMP + "," +
                        ColectieAgentHelper.Table_Client_Agent.COL_ID_AGENT + "," +
                        ColectieAgentHelper.Table_Client_Agent.COL_ID_CLIENT +
                        " FROM " + ColectieAgentHelper.Table_Client_Agent.TABLE_NAME +
                        " WHERE 1=1 " +
                        ((iIdMaster != 0) ? " and " + ColectieAgentHelper.Table_Client_Agent.COL_ID_AGENT + "=" + iIdMaster : "") +
                        ((iIdClient != 0) ? " and " + ColectieAgentHelper.Table_Client_Agent.COL_ID_CLIENT + "=" + iIdClient : "");
            }
            break;
        // prntru rute se da iIdClient echivalent cu id_agent
            case TipListaDenumiri.TLD_RUTE: {
                sqlSir="SELECT "+
                        ColectieAgentHelper.Table_Rute._ID+","+
                        ColectieAgentHelper.Table_Rute.COL_DENUMIRE+
                        " FROM "+ ColectieAgentHelper.Table_Rute.TABLE_NAME+
                        " WHERE "+ ColectieAgentHelper.Table_Rute.COL_ID_AGENT+"="+iIdClient;
            }
            break;
		// pentru linia de continut din sablon
		case Biz.TipListaDenumiri.TLD_LINIE_SABLON:
			if (iIdMaster==0) {
				sqlSir="SELECT "+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+","+
					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+","+
					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID +" as _id_temp "+","+
					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_ID_PRODUS+","+
					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_DIFERENTE+","+
					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_S_TIMESTAMP+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+ 
					" FROM "+Table_Produse.TABLE_NAME+
					" INNER JOIN "+Table_TempContinutDocumente.TABLE_NAME+" ON "+
							Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_TempContinutDocumente.TABLE_NAME+"."+
							Table_TempContinutDocumente.COL_ID_PRODUS+
					" ORDER BY "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID;
			} else {
				// imaginea doc din pozitii
				sqlSir="SELECT "+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
					"0 as "+Table_Produse.COL_ID_MASTER+","+
					Table_Sablon_Pozitii.TABLE_NAME+"."+Table_Sablon_Pozitii.COL_CANTITATE+","+
					Table_Sablon_Pozitii.TABLE_NAME+"."+Table_Sablon_Pozitii._ID +" as _id_temp "+","+
					Table_Sablon_Pozitii.TABLE_NAME+"."+Table_Sablon_Pozitii.COL_ID_PRODUS+","+
					Table_Sablon_Pozitii.TABLE_NAME+"."+Table_Sablon_Pozitii.COL_DIFERENTE+","+
					Table_Sablon_Pozitii.TABLE_NAME+"."+Table_Sablon_Pozitii.COL_S_TIMESTAMP+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+ 
					" FROM "+Table_Sablon_Antet.TABLE_NAME+
					" INNER JOIN "+ Table_Sablon_Pozitii.TABLE_NAME+" ON "+ Table_Sablon_Antet.TABLE_NAME+"."+Table_Sablon_Antet._ID+"="+
							Table_Sablon_Pozitii.TABLE_NAME+"."+Table_Sablon_Pozitii.COL_ID_ANTET+
					" INNER JOIN "+Table_Produse.TABLE_NAME+" ON "+	Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+
							Table_Sablon_Pozitii.TABLE_NAME+"."+Table_Sablon_Pozitii.COL_ID_PRODUS+
					" WHERE "+Table_Sablon_Antet.TABLE_NAME+"."+Table_Sablon_Antet._ID+" = "+iIdMaster;
			}
		
			break;
		// creeaza aviz pt descarcare stoc
		case Biz.TipListaDenumiri.TLD_GEN_AVIZ_STOC_0:
        sqlSir = "SELECT " +
        		Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+","+
        		Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_COTA_TVA+","+
                " round(sum(case "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_TIPDOC+
                " when "+Biz.TipDoc.ID_TIPDOC_AVIZINC+ " then "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE +
                " when "+Biz.TipDoc.ID_TIPDOC_COMANDA+ " then -"+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE +
                " when "+Biz.TipDoc.ID_TIPDOC_BONFISC+ " then -"+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE +
                " when "+Biz.TipDoc.ID_TIPDOC_FACTURA+" then -"+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE +
                " when "+Biz.TipDoc.ID_TIPDOC_AVIZDESC+" then -"+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE +
                " when "+Biz.TipDoc.ID_TIPDOC_AVIZCLIENT+" then -"+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE +
                " end " +
                "),0) as cantitate  " +
                " FROM " +Table_Antet.TABLE_NAME+
            	" JOIN "+Table_Pozitii.TABLE_NAME +" ON "+
            	Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" = "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_ANTET+
            " WHERE " +Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" > 0" ;
            // la iIdMaster se primeste valoarea pt zilele de pastrare date
                if(iIdMaster<0) {
                    // nu se blocheaza documentele transmise
                } else {
                    sqlSir=sqlSir+" AND "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_BLOCAT+"=0 ";
                }
            sqlSir=sqlSir+
                    " AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)>='"+sData1+"'"+
                    " AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)<='"+sData2+"'"+
                    " GROUP BY "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS ;

        break;
		case Biz.TipListaDenumiri.TLD_TRANSFER_AMANUNT:
	        sqlSir = "SELECT " +
	        		Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+","+
	        		Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_COTA_TVA+","+
	        		Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_CU+","+
	                " round(sum(case "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_TIPDOC+
	                " when "+Biz.TipDoc.ID_TIPDOC_BONFISC+" then "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE +
	                " end " +
	                "),0) as cantitate  " +
	                " FROM " +Table_Antet.TABLE_NAME+
	            	" JOIN "+Table_Pozitii.TABLE_NAME +" ON "+
	            	Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" = "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_ANTET+
	            	" WHERE " +Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" > 0" +
	            		" AND "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_TIPDOC+"="+Biz.TipDoc.ID_TIPDOC_BONFISC+
	            		" AND "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_BLOCAT+"=0 "+
	    				" AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)>='"+sData1+"'"+
	    				" AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)<='"+sData2+"'"+            	            	
	                " GROUP BY "+
	    				Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+","+
	    				Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_COTA_TVA+","+
	        			Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_CU ;
	        			
	        break;

        // creeaza cursor pentru generarea avizului de incarcare pe drum
		case Biz.TipListaDenumiri.TLD_GEN_AVIZ_INC:
//			Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+" as produs ,"+

			sqlSir= "SELECT " +
			Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+" ,"+
    		Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_COTA_TVA+","+
			
            " round(sum(case "+Table_Antet.COL_ID_TIPDOC+ 
            	" when " + Biz.TipDoc.ID_TIPDOC_AVIZINC + " then "+Table_Pozitii.COL_CANTITATE +
            	" when " + Biz.TipDoc.ID_TIPDOC_COMANDA + " then -"+Table_Pozitii.COL_CANTITATE +
            	" when " + Biz.TipDoc.ID_TIPDOC_AVIZDESC + " then -"+Table_Pozitii.COL_CANTITATE+
            " end " +
            "),2) as cantitate  "+
            " FROM " +
            Table_Antet.TABLE_NAME+
            	" JOIN "+Table_Pozitii.TABLE_NAME +" ON "+
            	Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" = "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_ANTET+
            	" JOIN "+Table_Produse.TABLE_NAME+" ON "+
            	Table_Produse.TABLE_NAME+"."+Table_Produse._ID+" = "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+
            " WHERE "
                    +Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" > 0";
             if (iIdMaster>0) {
                 // varianta fara descarcare stoc masina la intoarcerea din cursa ( semrompack)
                 // se genereaza avizul de drup plecand de la stocul primit si cu toate deocumentele zilei chiar daca au mai forst transmise
                 sqlSir=sqlSir+"";

             } else {
                 sqlSir=sqlSir+" AND "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_BLOCAT+"=0 ";
            }
            sqlSir=sqlSir+
    				" AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)>='"+sData1+"'"+
    				" AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)<='"+sData2+"'"+            	            	            
            " GROUP BY "+Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS + 
			" ORDER BY "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE ;
			break;
		// simuleaza un cursor de denumiri pentru avize de incarcare si descarcare
		// se fol o inreg din clienti cu cod -1
		case Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC:
			sqlSir="SELECT "+
					Table_Clienti.COL_DENUMIRE+","+
					Table_Clienti.COL_ADRESA+","+
					"0 as id_master"+","+
					Table_Clienti._ID+ 
					" FROM "+Table_Clienti.TABLE_NAME+
					" WHERE 0="+iIdMaster+" AND "+Table_Clienti._ID+" IN (-1,-2,-3) "+ 
					" ORDER BY "+Table_Clienti.COL_ORDONARE+","+Table_Clienti.COL_DENUMIRE ;
			break;
		// pentru un client
		case Biz.TipListaDenumiri.TLD_UN_CLIENT:
			sqlSir=" SELECT "+
				Table_Clienti.COL_ADRESA+","+
				Table_Clienti.COL_BANCA+","+
				Table_Clienti.COL_BLOCAT+","+
				Table_Clienti.COL_CONT+","+
				Table_Clienti.COL_CONTACT+","+
				Table_Clienti.COL_DENUMIRE+","+
				Table_Clienti.COL_ID_PART+","+
				Table_Clienti.COL_ID_RUTA+","+
				Table_Clienti.COL_ID_ZONA+","+
				Table_Clienti.COL_JUDET+","+
				Table_Clienti.COL_LOC+","+
				Table_Clienti.COL_NR_FISc+","+
				Table_Clienti.COL_NR_RC+","+
				Table_Clienti.COL_ORDONARE+","+
				Table_Clienti.COL_TEL1+","+
				Table_Clienti.COL_TEL2+
				" FROM "+Table_Clienti.TABLE_NAME +
				" WHERE "+Table_Clienti._ID+"="+iIdClient;
			break;
		// pentru istoria documentelor 
		case Biz.TipListaDenumiri.TLD_ISTORIC_DOC:
			sqlSir="SELECT "+
				Biz.getPtSelect(Table_Antet.TABLE_NAME,
					new String[]{Table_Antet.COL_ID_PART,Table_Antet.COL_DATA,Table_Antet.COL_NR_DOC,
						Table_Antet.COL_VAL_FARA,Table_Antet.COL_VAL_TVA,Table_Antet.COL_INCASAT,Table_Antet._ID},false)+
				Biz.getPtSelect(Table_Tipdoc.TABLE_NAME, new String[]{Table_Tipdoc.COL_DENUMIRE}, true)+
					" FROM "+Table_Antet.TABLE_NAME+" INNER JOIN "+Table_Tipdoc.TABLE_NAME+" ON "+
					Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_TIPDOC+"="+
					Table_Tipdoc.TABLE_NAME+"."+Table_Tipdoc._ID+
					" WHERE "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_PART+"="+iIdClient+
					" ORDER BY "+Table_Antet.TABLE_NAME+"."+Table_Antet._ID;
			break;
		// se fol la afisarea listei de alegere produse luind in calcul cantitatile din TempContinut
			// daca un produs este blocat pt idClient nu se mai afiseaza
		case Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT:
			sqlSir="SELECT "+
					Table_Produse.COL_DENUMIRE+","+
					Table_Produse.COL_PRET_CU+","+
					Table_Produse.COL_PRET_FARA+","+
					"pret,"+
					"pretred,"+
					Table_Produse.COL_COTA_TVA+","+
					Table_Produse.COL_ID_MASTER+","+
					"_id ,"+
                    Table_TempContinutDocumente.COL_ID_FA1+" ,"+
					" CASE WHEN cantitate<>0 THEN cantitate ELSE null END as cantitate ,"+
					" CASE WHEN cant_bonus<>0 THEN cant_bonus ELSE null END as cant_bonus "+
				" FROM ( ";
					
			sqlSir=sqlSir+"SELECT "+
					Table_Discount.TABLE_NAME+"."+Table_Discount.COL_BLOCAT+" as dblocat,"+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+",";
			if (lPVcuTVA)
					sqlSir=sqlSir+
							Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+" as pret ,";
			else
				sqlSir=sqlSir+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+" as pret ,";
			if (lPVcuTVA)
				sqlSir=sqlSir+
						Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_CU+" as pretred ,";
			else
				sqlSir=sqlSir+
					Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_FARA+" as pretred ,";
				
			sqlSir=sqlSir+					
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_COTA_TVA+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+ ","+
                    "SUM ("+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_ID_FA1+") as "+
                        Table_TempContinutDocumente.COL_ID_FA1+" ,"+
					"SUM (CASE WHEN "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_C_ESTE_BONUS+"=0 THEN "+
						Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+" ELSE 000000.0000 END) as cantitate ,"+
					"SUM (CASE WHEN "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_C_ESTE_BONUS+"=1 THEN "+
						Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+" ELSE 000000.0000 END) as cant_bonus "+
					" FROM "+Table_Produse.TABLE_NAME+
						" LEFT OUTER JOIN "+Table_TempContinutDocumente.TABLE_NAME+" ON "+
						Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_TempContinutDocumente.TABLE_NAME+"."+
						Table_TempContinutDocumente.COL_ID_PRODUS+
						" LEFT OUTER JOIN "+Table_Discount.TABLE_NAME+" ON "+
						Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_ID_PRODUS+
						" AND "+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_ID_CLIENT+"="+iIdClient+
						" AND ("+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_CU+"+"
							+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_CU+"+"
							+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_BLOCAT+"<>0 )"+
					" WHERE "
                        +"("+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_BLOCAT+"=0 or "
                        +Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+"=0)"
                        +" AND "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+"="+iIdMaster
					    +" AND "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+"<>''"
						+" and IFNULL(dblocat,0)=0 "
			;
			if (iIdMaster>0 )
			switch (nTipTva) {
			case  1 : // cota de 24
				sqlSir=sqlSir+" and "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_COTA_TVA+"= 24 ";
				break;
			case 2 : // cota de 9
				sqlSir=sqlSir+" and "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_COTA_TVA+"= 9 ";
				break;
			default:
				break;
			}
			
			sqlSir=sqlSir+" GROUP BY 1,2,3,4,5,6,7,8 "+
					" ORDER BY "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ORDONARE+","+
					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE +" )" ;
			
			break;
			
			// pentru continutul documentului. Rezulta cursor numai cu liniile din TempContinutDocumente sau cu liniile din
			// pozitii daca se da iIdmaster
			case Biz.TipListaDenumiri.TLD_LINIE_CONTINUT:
				if (iIdMaster==0) {
					sqlSir="SELECT "+
						Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
						Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+","+
						Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+","+
						Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU1+","+
						Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA1+","+
						"CASE WHEN ("+
							Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_C_ESTE_BONUS+"=0 "+
							" AND "+
							Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_FARA_PRET+"=0 ) "+
						" THEN " ;
					if (lPVcuTVA)
							sqlSir=sqlSir+" IFNULL("+
									Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_PRET_CU+","+
									" IFNULL("+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_CU+","+
									Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+"))";
					else
							sqlSir=sqlSir+" IFNULL("+
								Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_FARA+","+
								Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+")";
					sqlSir=sqlSir+
						" ELSE 00000000.0000 END as pret ,";
					if (lPVcuTVA)
						sqlSir=sqlSir+"1 as cu_tva ,";
					else
						sqlSir=sqlSir+"0 as cu_tva ,";
					sqlSir=sqlSir+					
							Table_Produse.TABLE_NAME+"."+Table_Produse.COL_COTA_TVA+","+
							Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+","+
							Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+","+
							Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_C_ESTE_BONUS+","+
							Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_FARA_PRET+","+
							Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID +" as _id_temp "+","+
							Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_ID_PRODUS+","+
                            Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_ID_FA1+","+
                            Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_ID_FA2+","+
							Table_Produse.TABLE_NAME+"."+Table_Produse._ID+ 
							" FROM "+Table_Produse.TABLE_NAME+
							" INNER JOIN "+Table_TempContinutDocumente.TABLE_NAME+" ON "+
								Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_TempContinutDocumente.TABLE_NAME+"."+
								Table_TempContinutDocumente.COL_ID_PRODUS+
							" LEFT OUTER JOIN "+Table_Discount.TABLE_NAME+" ON "+
							Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_ID_PRODUS+
							" AND "+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_ID_CLIENT+"="+iIdClient+
							" AND ("+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_CU+"+"
								+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_PRET_CU+"<>0 )"+
							" WHERE IFNULL("+Table_Discount.TABLE_NAME+"."+Table_Discount.COL_BLOCAT+",0)=0 "+
							" ORDER BY "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID;
// am data afara pentru a se putea initializa fara continut
//	" WHERE "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+" <> 0 "+

				
				} else {
					// imaginea doc din pozitii
					// campul pret reprezinta pretul redus si se recalculeaza pe baza discountului prezent in fiecare linie
					sqlSir="SELECT "+
						Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
						Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_CU1+","+
						Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_FARA1+","+
						Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_CU+","+
						Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_FARA+",";
						if (lPVcuTVA)
							sqlSir=sqlSir+"("+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_VAL_FARA+"+" +
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_VAL_TVA+"-"+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_VAL_RED+"-"+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_TVA_RED+")/"+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE+
									" as pret ,";
						else
							sqlSir=sqlSir+"("+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_VAL_FARA+"+" +
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_VAL_TVA+"-"+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_VAL_RED+"-"+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_TVA_RED+")/"+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE+
									" as pret ,";
						
						if (lPVcuTVA)
							sqlSir=sqlSir+"1 as cu_tva ,";
						else
							sqlSir=sqlSir+"0 as cu_tva ,";
					
						sqlSir=sqlSir+					
							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_COTA_TVA+","+
							"0 as "+Table_Produse.COL_ID_MASTER+","+
							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE+","+
                            Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_BONUS+" as "+Table_TempContinutDocumente.COL_C_ESTE_BONUS+","+
							"0 as "+Table_TempContinutDocumente.COL_FARA_PRET+","+
							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii._ID +" as _id_temp "+","+
							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+","+
                            Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_FA1+","+
                            Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_FA2+","+
							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii._ID+ 
							" FROM "+Table_Antet.TABLE_NAME+
							" INNER JOIN "+ Table_Pozitii.TABLE_NAME+" ON "+ Table_Antet.TABLE_NAME+"."+Table_Antet._ID+"="+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_ANTET+
							" INNER JOIN "+Table_Produse.TABLE_NAME+" ON "+	Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+
								Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_PRODUS+
							" WHERE "+Table_Antet.TABLE_NAME+"."+Table_Antet._ID+" = "+iIdMaster;
				}

				break;
			case Biz.TipListaDenumiri.TLD_PRODUSE:
				sqlSir="SELECT "+
						Table_Produse.COL_DENUMIRE+","+
						Table_Produse.COL_PRET_CU+","+
						Table_Produse.COL_PRET_FARA+",";
				if (lPVcuTVA)
						sqlSir=sqlSir+
							Table_Produse.COL_PRET_CU+" as pret ,";
				else
						sqlSir=sqlSir+
							Table_Produse.COL_PRET_FARA+" as pret ,";

					sqlSir=sqlSir+
						Table_Produse.COL_ID_MASTER+","+
						"0000.0000 as cantitate ,"+ 
						Table_Produse._ID+ 
						" FROM "+Table_Produse.TABLE_NAME+
						" WHERE id_master="+iIdMaster
							+" AND "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+"<>''"+
						" ORDER BY "+Table_Produse.COL_ORDONARE+","+Table_Produse.COL_DENUMIRE ;
				break;
			case Biz.TipListaDenumiri.TLD_CLIENTI:
				sqlSir="SELECT "+
						Table_Clienti.COL_DENUMIRE+","+
						Table_Clienti.COL_ADRESA+","+
						"0 as id_master"+","+
						Table_Clienti._ID+ 
						" FROM "+Table_Clienti.TABLE_NAME+
						" WHERE 0="+iIdMaster+" AND _id>0 and blocat=0 "+
						" ORDER BY "+Table_Clienti.COL_ORDONARE+","+Table_Clienti.COL_DENUMIRE ;
				
				break;
			case Biz.TipListaDenumiri.TLD_AGENTI:
				sqlSir="SELECT "+
						Table_Agent.COL_DENUMIRE+","+
						"'' as "+Table_Clienti.COL_ADRESA+","+
						"0 as id_master"+","+
						Table_Agent._ID+ 
						" FROM "+Table_Agent.TABLE_NAME+
						" WHERE 0="+iIdMaster+" AND _id>0 "+ 
						" ORDER BY "+Table_Agent.COL_DENUMIRE ;
				
				break;
			case Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE:
				sqlSir="SELECT "+
						Table_Clienti.COL_DENUMIRE+","+
						Table_Clienti.COL_ADRESA+","+
						"0 as id_master"+","+
						Table_Clienti._ID+ 
						" FROM "+Table_Clienti.TABLE_NAME+
						" WHERE 0="+iIdMaster+" AND _id>0 "+ 
						" ORDER BY "+Table_Clienti.COL_ORDONARE+","+Table_Clienti.COL_DENUMIRE ;
				
				break;
		default:
			sqlSir=Biz.getSqlListaDenumiri(iTLD, iIdMaster, lPVcuTVA);
			break;
		}
		Log.d("SQLCONTINUT",sqlSir);
		return sqlSir;
	}
	
	//determina instr sql pt cursorul de denumiri in liste
	public static String getSqlListaDenumiri(int iTLD, long iIdMaster, Boolean lPVcuTVA) {
		String sqlSir="";
		Log.d("SIPEAICIeeeee","iTLD= "+iTLD);
		switch (iTLD) {
		case Biz.TipListaDenumiri.TLD_PRODUSE:
			sqlSir=Biz.getSqlListaDenumiri(iTLD, iIdMaster, 0, lPVcuTVA, 0);
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI:
			sqlSir=Biz.getSqlListaDenumiri(iTLD, iIdMaster, 0, lPVcuTVA, 0);
			break;
			// pentru lista de alegere la continut documente
		case Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT:
			sqlSir=Biz.getSqlListaDenumiri(iTLD, iIdMaster, 0, lPVcuTVA, 0);
//			sqlSir="SELECT "+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+",";
//			if (lPVcuTVA)
//					sqlSir=sqlSir+
//							Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+" as pret ,";
//			else
//				sqlSir=sqlSir+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+" as pret ,";
//
//			sqlSir=sqlSir+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_COTA_TVA+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+","+
//					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+","+
//					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID +" as _id_temp "+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+ 
//					" FROM "+Table_Produse.TABLE_NAME+" LEFT OUTER JOIN "+Table_TempContinutDocumente.TABLE_NAME+" ON "+
//					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_TempContinutDocumente.TABLE_NAME+"."+
//						Table_TempContinutDocumente.COL_ID_PRODUS+
//					" WHERE "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+"="+iIdMaster +
//					" ORDER BY "+Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ORDONARE+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE ;
//			
//			Log.d("SQLCONTINUT",sqlSir);
			break;
		// pentru determinarea imaginii documentului pe baza idAntet
		case Biz.TipListaDenumiri.TLD_IMAGINE_DOC:
			sqlSir="";
			break;
		// pentru salvarea continutulu documentului combinat cu tablea temporara 
		case Biz.TipListaDenumiri.TLD_LINIE_CONTINUT:
			// in idMaster se poate da idAntet .Daca se da , se extrage imagine din antet si pozitii nu din temporar
			sqlSir=Biz.getSqlListaDenumiri(iTLD, iIdMaster, 0, lPVcuTVA, 0);
//			if (iIdMaster==0) {
//			sqlSir="SELECT "+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+",";
//			if (lPVcuTVA)
//					sqlSir=sqlSir+
//								Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_CU+" as pret ,";
//			else
//					sqlSir=sqlSir+
//						Table_Produse.TABLE_NAME+"."+Table_Produse.COL_PRET_FARA+" as pret ,";
//			
//			sqlSir=sqlSir+					
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_COTA_TVA+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ID_MASTER+","+
//					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+","+
//					Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID +" as _id_temp "+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+ 
//					" FROM "+Table_Produse.TABLE_NAME+" INNER JOIN "+Table_TempContinutDocumente.TABLE_NAME+" ON "+
//					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_TempContinutDocumente.TABLE_NAME+"."+
//						Table_TempContinutDocumente.COL_ID_PRODUS+
//					" WHERE "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+" <> 0 "+
//					" ORDER BY "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID;
//			} else {
//				sqlSir="SELECT "+
//					Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+","+
//					Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_CU+","+
//					Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_FARA+",";
//				if (lPVcuTVA)
//						sqlSir=sqlSir+
//							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_CU+" as pret ,";
//				else
//						sqlSir=sqlSir+
//							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_PRET_FARA+" as pret ,";
//				
//				sqlSir=sqlSir+					
//					Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_COTA_TVA+","+
//					"0 as "+Table_Produse.COL_ID_MASTER+","+
//					Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_CANTITATE+","+
//					Table_Pozitii.TABLE_NAME+"."+Table_Pozitii._ID +" as _id_temp "+","+
//					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+ 
//					" FROM "+Table_Antet.TABLE_NAME +
//						" INNER JOIN "+Table_Pozitii.TABLE_NAME+" ON "+Table_Antet.TABLE_NAME+"."+Table_Antet._ID+"="+
//							Table_Pozitii.TABLE_NAME+"."+Table_Pozitii.COL_ID_ANTET+
//					Table_Produse.TABLE_NAME+" INNER JOIN "+Table_Pozitii.TABLE_NAME+" ON "+
//					Table_Produse.TABLE_NAME+"."+Table_Produse._ID+"="+Table_Pozitii.TABLE_NAME+"."+
//							Table_Pozitii.COL_ID_PRODUS+
//					" WHERE "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente.COL_CANTITATE+" <> 0 "+
//						" ORDER BY "+Table_TempContinutDocumente.TABLE_NAME+"."+Table_TempContinutDocumente._ID;
//				
//			}

			break;
		default:
			break;
		}
		Log.d("SQLCONTINUT 2","Sir="+sqlSir);
		return sqlSir;
	}

	//determina array cu coloanele pt cursor adapter la listele cu denumiri
	public static String[] getArrayColoaneListaDenumiri (int iTLD, long iIdMaster) {
		String[] col=null;
		switch (iTLD) {
		case Biz.TipListaDenumiri.TLD_PRODUSE:
			col = new String[]{"denumire","pret"};
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI:
			col=new String[]{"denumire","adresa"};
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE:
			col=new String[]{"denumire","adresa"};
			break;
		case Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC:
			col=new String[]{"denumire","adresa"};
			break;
		case Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT: // pentru afisare in lista de denumiri pt alegere
			col=new String[]{"denumire","pret","pretred","cantitate","cant_bonus"};
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_CONTINUT:
			col=new String[]{"denumire","cantitate","pret"}; // pentru afisare in liniile de continut din document 
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_SABLON:
			col=new String[]{"denumire","cantitate","diferente"}; // pentru afisare in liniile de sablon 
			break;
		case Biz.TipListaDenumiri.TLD_AGENTI:
			col=new String[]{"denumire"}; // pentru afisare in liniile de sablon 
			break;
			
		default:
			break;
		}
		
		return col;
	}
	
	//determina array cu idurile viewurilor din layoutul pentru reprezentarea randului 
	public static int[] getArrayIdViewListaDenumiri (int iTLD, long iIdMaster) {
		int[] to =null;
		switch (iTLD) {
		case Biz.TipListaDenumiri.TLD_PRODUSE:
			to = new int[]{R.id.listDen_txt_denumire_produs,R.id.listDen_txt_pret};
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI:
			to=new int[]{R.id.listDen_txt_denumire_client,R.id.listDen_txt_adresa_client};
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE:
			to=new int[]{R.id.listDen_txt_denumire_client,R.id.listDen_txt_adresa_client};
			break;
		case Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC:
			to=new int[]{R.id.listDen_txt_denumire_client,R.id.listDen_txt_adresa_client};
			break;
		case Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT:
			to=new int[]{R.id.lblRowPrCantDenumire,R.id.lblRowPrCantPret,R.id.lblRowPrCantPretRedus,R.id.lblRowPrCantCantitate,R.id.lblRowPrCantCantitateBonus};
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_CONTINUT:
			to=new int[]{R.id.lblRowContinutDenumire,R.id.lblRowContinutCantitate,R.id.lblRowContinutPret};
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_SABLON:
			to=new int[]{R.id.lblRowSablonDenumire,R.id.lblRowSablonCantitate,R.id.lblRowSablonDiferente};
			break;
		case Biz.TipListaDenumiri.TLD_AGENTI:
			to=new int[]{R.id.listDen_txt_denumire_agent};
			break;
			
		default:
			break;
		}
		return to;
	}
	
	//determina idul layoutului pentru radul de la lista denumiri
	public static int getIdRowListaDenumiri (int iTLD, long iIdMaster) {
		int iId=0;
		switch (iTLD) {
		case Biz.TipListaDenumiri.TLD_PRODUSE:
			iId=R.layout.row_listview_denumiri_produse;
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI:
			iId=R.layout.row_listview_denumiri_clienti;
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE:
			iId=R.layout.row_listview_denumiri_clienti;
			break;
		case Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC:
			iId=R.layout.row_listview_denumiri_clienti;
			break;
		case Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT:
			iId=R.layout.row_listview_produse_alege_cantitate;
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_CONTINUT:
			iId=R.layout.row_listview_continut_document;
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_SABLON:
			iId=R.layout.row_listview_continut_sablon;
			break;
		case Biz.TipListaDenumiri.TLD_AGENTI:
			iId=R.layout.row_listview_denumiri_agenti;
			break;
			
		default:
			break;
		}
		return iId;
	}
	
	public static String[] getActiuniListaDenumiri(int iTLD,long iIdMaster,Context context) {
		String[] vOpt=new String[] {" "} ;
		switch (iTLD) {

		case Biz.TipListaDenumiri.TLD_CLIENTI:
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_ecran5_actual_clienti",false) ||
                    PreferenceManager.getDefaultSharedPreferences(context).getString("key_ecran5_varianta","").toUpperCase().equals("SOROLI"))
                vOpt=new String[]  {"Adauga documente","Istoric documente","Adauga incasare","Sablon cerere marfa","Adauga client",
                        "Modifica client","Sterge client","Renunta"};
            else
			    vOpt=new String[]  {"Adauga documente","Istoric documente","Sablon cerere marfa","Sold ambalaje","Renunta"};
			break;
		case Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE:
			vOpt=new String[]  {"Sablon cerere marfa","Renunta"};
			break;
		case Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC:
			if (iIdMaster==-1)
				vOpt=new String[]  {"Adauga documente","Istoric documente","Lista produse","Generare aviz descarcare stoc","Renunta"};
			else if (iIdMaster==-3)
				vOpt=new String[]  {"Generare nota transfer","Renunta"};
			break;
		case Biz.TipListaDenumiri.TLD_PRODUSE:
			vOpt=new String[]  {"Articol nou","Modifica","Renunta"};
			break;
		case Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT:
			vOpt=new String[]  {""};
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_CONTINUT:
			vOpt=new String[]  {""};
			break;
		case Biz.TipListaDenumiri.TLD_LINIE_SABLON:
			vOpt=new String[]  {""};
			break;
			
		}
		return vOpt;
	}
	// determina array cu idurile asociate unui meniu
	public static long[] getIdsActiuni (int iTLD, long iIdMaster,Context context) {
		switch (iTLD) {
		case Biz.TipListaDenumiri.TLD_CLIENTI:
            Log.d("PRO&","Opt clienti"+PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("key_ecran5_varianta","").toUpperCase());

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_ecran5_actual_clienti",false) ||
                    PreferenceManager.getDefaultSharedPreferences(context).getString("key_ecran5_varianta", "").toUpperCase().equals("SOROLI")) {
                Log.d("PRO&","1");
                return ConstanteGlobale.Optiuni_ListaClienti.ID_OPTIUNI_CLIENT_SOROLI;
            }
            else {
                Log.d("PRO&","2");
                return ConstanteGlobale.Optiuni_ListaClienti.ID_OPTIUNI_CLIENT;
            }
		case Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE:
			return ConstanteGlobale.Optiuni_ListaClienti.ID_OPTIUNI_CLIENT_COMENZI_ONLINE ;
		case Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC:
			if (iIdMaster==-1)
				return ConstanteGlobale.Optiuni_ListaClienti.ID_OPTIUNI_INC_DESC ;
			else if (iIdMaster==-3)
				return ConstanteGlobale.Optiuni_ListaClienti.ID_OPTIUNI_TRANSFER_AMANUNT ;
		case Biz.TipListaDenumiri.TLD_PRODUSE:
			return ConstanteGlobale.Optiuni_ListaClienti.ID_OPTIUNI_ARTICOLE ;
		default:
			return new long[] {1};
		}
	}
	
	
	public static abstract class TipListaDenumiri {
		public static final int TLD_PRODUSE=1;
		public static final int TLD_CLIENTI=2;
		public static final int TLD_PRODUSE_PT_CONTINUT=3;
		public static final int TLD_LINIE_CONTINUT=4; // pentru lista de la liniile de continut ale unui document
		public static final int TLD_ISTORIC_DOC=5;
		// pentru determinarea imaginii documentului din idantet
		public static final int TLD_IMAGINE_DOC=6;
		// pentru extragerea detelor pt un client
		public static final int TLD_UN_CLIENT=7;
		// sinuleaza un client pentru avize de incarcare descarcare 
		public static final int TLD_AVIZ_INC_DESC=8;
		// pentru generare aviz de incarcare pe drum
		public static final int TLD_GEN_AVIZ_INC=9;
		// pentru generare aviz desc stoc
		public static final int TLD_GEN_AVIZ_STOC_0=10;
		// pentru comenzi predefinite (sabloane) 
		public static final int TLD_SABLON_CERERE=11;
		// pentru liniile de continut din sablon
		public static final int TLD_LINIE_SABLON=12;
		// pentru alegere agenti
		public static final int TLD_AGENTI=13;
		// alege clienti pentru comenzi online
		public static final int TLD_CLIENTI_COMENZI_ONLINE=14;
		// transfer amanunt 
		public static final int TLD_TRANSFER_AMANUNT=15;
		// actualizare ambalaje 
		public static final int TLD_ACTUAL_AMBALAJE=16;
        // pentru varianta soroli
        public static final int TLD_RUTE=17;
        // pentru client_agent
        public static final int TLD_CLIENT_AGENT=18;


	}
	
	// constante pentru calcul
	public static abstract class ConstCalcul {
		public static final int ZEC_PRET_FARA=4;
		public static final int ZEC_PRET_CU=2;
		public static final int ZEC_VAL_FARA=2;
		public static final int ZEC_VAL_CU=2;
		public static final int ZEC_VAL_TVA=2;
		public static final int ZEC_PROC_RED=4;				
	}
	
	public static abstract class TipDoc {
		public static final int ID_TIPDOC_COMANDA=1;
		public static final int ID_TIPDOC_FACTURA=2;
		public static final int ID_TIPDOC_AVIZCLIENT=3;
		public static final int ID_TIPDOC_AVIZDESC=4;
		public static final int ID_TIPDOC_AVIZINC=5;
		public static final int ID_TIPDOC_BONFISC=6;
		public static final int ID_TIPDOC_CEREMF=7;
		public static final int ID_TIPDOC_TRANSAM=8;
		public static final int ID_TIPDOC_CHITANTA=9;
		
		public static final String [] DEN_TIPDOC= new String [] {"COMANDA","FACTURA","AVIZ CLIENT","AVIZ DESC","AVIZ INC",
			"BON FISCAL","CERERE MARFA","TRANSFER","CHITANTA"};

		public static final int [] ID_TIPDOC=new int[] {ID_TIPDOC_COMANDA,ID_TIPDOC_FACTURA,ID_TIPDOC_AVIZCLIENT,ID_TIPDOC_AVIZDESC,
			ID_TIPDOC_AVIZINC,ID_TIPDOC_BONFISC,ID_TIPDOC_CEREMF,ID_TIPDOC_TRANSAM,ID_TIPDOC_CHITANTA};
		public static final int [] ID_PREF_DOCCRT=new int[] 
				{R.string.key_ecran2_comanda_curent,R.string.key_ecran2_facturi_curent,R.string.key_ecran2_aviz_curent,
				R.string.key_ecran2_avmf_curent,R.string.key_ecran2_avmf_curent,0,0,0,R.string.key_ecran2_chitanta_curent};
		public static int getTipDocPoz(String denumire) {
			for (int i = 0; i < Biz.TipDoc.DEN_TIPDOC.length; i++) {
				Log.d("POZ",denumire+"="+Biz.TipDoc.DEN_TIPDOC[i]);
				if (denumire.equals(Biz.TipDoc.DEN_TIPDOC[i])) {
					Log.d("POZ","Poz="+i);
					return i;
				}
			}
			return -1;	
		}
		public static int getIdPrefDoc(String denumire) {
			int nPoz=Biz.TipDoc.getTipDocPoz(denumire);
			if (nPoz>=0 && nPoz<Biz.TipDoc.ID_PREF_DOCCRT.length)
				return Biz.TipDoc.ID_PREF_DOCCRT[nPoz];
			return 0;	
		}

	}
    // se creeaza filtru de tip in( ...) . Se intoarce doar sirul nu si parantezele
    public static String getFiltruIn ( Cursor crs, String sCamp) {
        String sRez="0";
        String sNumeC=(sCamp.equals("") ? "_id" : sCamp );

        try {
            crs.moveToFirst();
            while (!crs.isAfterLast()) {
                sRez = sRez +  "," + crs.getInt(crs.getColumnIndexOrThrow(sNumeC));
                crs.moveToNext();
            }
        } catch ( Exception e) {
            Log.d("PRO","Eror filtruin:"+e.getMessage());
        }

        return sRez;
    }

    public static String getFiltruIn ( ResultSet crs, String sCamp) {
        String sRez="0";
        String sNumeC=(sCamp.equals("") ? "cod_int" : sCamp );
        try {
            while (crs.next()) {
                sRez = sRez +  "," + crs.getInt(sNumeC);
            }
        } catch ( Exception e) {

        }
        return sRez;
    }
}
