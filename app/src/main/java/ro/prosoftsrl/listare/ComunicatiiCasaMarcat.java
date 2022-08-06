package ro.prosoftsrl.listare;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.datecs.fiscalprinter.FiscalPrinterException;
import com.datecs.fiscalprinter.rou.DP25ROU;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.bluet.Bluet;
import ro.prosoftsrl.diverse.Siruri;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class ComunicatiiCasaMarcat {

	public static String[] getImageBon (Cursor crs, int versiune ,Context context, int tipCasa) {
		// cursorul contine pozitiile 
		return null;
	}

// se incearca deblocarea casei de marcat prin emiterea unei comenzi de inchidere bon fiscal
	public static void deblocCasa (Context context,int tipCasa) {
	Bluet printer=null;
	String sBluetAdapter =null;
	DP25ROU mFMP =null;
	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	sBluetAdapter=settings.getString(context.getString(R.string.key_ecran3_bluetcasamarcat),"");
	
	printer=new Bluet(context,sBluetAdapter);
	Log.d("PRO","cauta bluet");
	printer.findBT();
	try {
		Log.d("PRO","deschide bluet");
		printer.openBT();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		Log.d("COMCASA","Err_open:"+e1.getMessage());
		e1.printStackTrace();
	}
	// se verifica starea si nu se listeaza daca nu este deschis
	if (printer.stare!=1) {
		Toast.makeText(context, "Adaptorul Bluettoth nu poate fi deschis", Toast.LENGTH_SHORT).show();
	} else {
		try {
			Log.d("PRO","crea mFMP");
			final InputStream in = printer.getMmInputStream();
			final OutputStream out = printer.getMmOutputStream();
			printer.mmSocket.getInputStream();
			mFMP = new DP25ROU(in, out );
			mFMP.command56Variant0Version0();
			mFMP.close();
			printer.closeBT();			
		} catch (FiscalPrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("COMCASA","Err1:"+e.getMessage());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("COMCASA","Err2:"+e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("COMCASA","Err3:"+e.getMessage());
		}
	}
	
}

	
	public static void rapZcasa (Context context,int tipCasa) {
	Bluet printer=null;
	String sBluetAdapter =null;
	DP25ROU mFMP =null;
	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	sBluetAdapter=settings.getString(context.getString(R.string.key_ecran3_bluetcasamarcat),"");
	
	printer=new Bluet(context,sBluetAdapter);
	Log.d("PRO","cauta bluet");
	printer.findBT();
	try {
		Log.d("PRO","deschide bluet");
		printer.openBT();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		Log.d("COMCASA","Err_open:"+e1.getMessage());
		e1.printStackTrace();
	}
	// se verifica starea si nu se listeaza daca nu este deschis
	if (printer.stare!=1) {
		Toast.makeText(context, "Adaptorul Bluettoth nu poate fi deschis", Toast.LENGTH_SHORT).show();
	} else {
		try {
			Log.d("PRO","crea mFMP");
			final InputStream in = printer.getMmInputStream();
			final OutputStream out = printer.getMmOutputStream();
			printer.mmSocket.getInputStream();
			mFMP = new DP25ROU(in, out );
			mFMP.command69Variant0Version0("0");
			mFMP.close();
			printer.closeBT();			
		} catch (FiscalPrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("COMCASA","Err1:"+e.getMessage());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("COMCASA","Err2:"+e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("COMCASA","Err3:"+e.getMessage());
		}
	}
	
}

	public static int createFisImagine (Long nIdAntet, SQLiteDatabase db,int versiune, Context context, Boolean lPrint, int tipCasa) {
		int nRez=0;
		switch (tipCasa) {
		case ConstanteGlobale.Tipuri_case_marcat.DATECS_DP_05:
			nRez=createFisImagineDatecs (nIdAntet,db,versiune,context, lPrint, tipCasa); 
			break;
		case ConstanteGlobale.Tipuri_case_marcat.ACTIVA_MOBILE_EJ:
			nRez=createFisImagineActiva(nIdAntet,db,versiune,context, lPrint, tipCasa); 
			break;
		default:
			break;
		}
		return nRez;
	}

public static int createFisImagineActiva (Long nIdAntet, SQLiteDatabase db,int versiune, Context context, Boolean lPrint, int tipCasa) {
	Bluet printer=null;
	String sBluetAdapter =null;
	int nRez=0;
	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	sBluetAdapter=settings.getString(context.getString(R.string.key_ecran3_bluetcasamarcat),"");
	if (lPrint) {
		Log.d("PRO","crea bluet");
		printer=new Bluet(context,sBluetAdapter);
		Log.d("PRO","cauta bluet");
		printer.findBT();
		try {
			Log.d("PRO","deschide bluet");
			printer.openBT();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Log.d("COMCASA","Err_open:"+e1.getMessage());
			e1.printStackTrace();
		}
		// se verifica starea si nu se listeaza daca nu este deschis
		if (printer.stare!=1) {
			Toast.makeText(context, "Adaptorul Bluettoth nu poate fi deschis", Toast.LENGTH_SHORT).show();
			lPrint=false;
		}
		
		
	}
	
	if (lPrint ) {
		try {
			Log.d("PRO","Inainte deschide bon");
			printer.sendData("KARAT"+"\n");
			printer.sendData("*Baterie telefon.........000008002000001000100"+"\n");
			printer.sendData("T0000010000  CASH"+"\n");
			printer.sendData("END KARAT"+"\n");

			Log.d("PRO","LISTBON 9");
			 printer.closeBT();
			Log.d("PRO","LISTBON 10");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("PRO","LISTBON 11");
			e.printStackTrace();
		}
	}	
	return nRez;
}
		

	
public static int createFisImagineDatecs (Long nIdAntet, SQLiteDatabase db,int versiune, Context context, Boolean lPrint, int tipCasa) {
	Cursor crs=null;
	crs=db.rawQuery(Biz.getSqlImagineDoc(nIdAntet), null);
	Bluet printer=null;
	String sBluetAdapter =null;
	DP25ROU mFMP =null;
	boolean lModTestCasa=false;
	int nRez=1 ; // daca imprimarea se termina bine se returneaza 0 
	crs=db.rawQuery(Biz.getSqlImagineDoc(nIdAntet), null);
	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	sBluetAdapter=settings.getString(context.getString(R.string.key_ecran3_bluetcasamarcat),"");
	lModTestCasa=settings.getBoolean(context.getString(R.string.key_ecran5_casa_marcat_test), false);
	if (lPrint) {
		printer=new Bluet(context,sBluetAdapter);
		printer.findBT();
		try {
			printer.openBT();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Log.d("COMCASA","Err_open:"+e1.getMessage());
			e1.printStackTrace();
		}
		// se verifica starea si nu se listeaza daca nu este deschis
		if (printer.stare!=1) {
			Toast.makeText(context, "Adaptorul Bluettoth nu poate fi deschis", Toast.LENGTH_SHORT).show();
			lPrint=false;
		} else {
		
			try {
				Log.d("PRO","crea mFMP");
				final InputStream in = printer.getMmInputStream();
				final OutputStream out = printer.getMmOutputStream();
				printer.mmSocket.getInputStream();
				mFMP = new DP25ROU(in, out );
			} catch (FiscalPrinterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("COMCASA","Err1:"+e.getMessage());
				lPrint=false;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("COMCASA","Err2:"+e.getMessage());
				lPrint=false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("COMCASA","Err3:"+e.getMessage());
				lPrint=false;
			}
		}
	}
	
	if (lPrint && crs.getCount()>0) {
		try {
			mFMP.command48Variant0Version0("1", "1", "1"); // deschide bon fiscal
            Thread.sleep(1000);
			crs.moveToFirst();
			double reducere=0;
			while (!crs.isAfterLast()) {
				double nCant=crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE));
				double nPret=crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PRET_CU));
				double nPretCasa=crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PRET_CU1));
				String den =crs.getString(crs.getColumnIndexOrThrow(Table_Produse.TABLE_NAME+"_"+Table_Produse.COL_DENUMIRE));
				if (den.length()>30) den=den.substring(0, 30);
				String ctva=crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA))==24 ? "A" : "B";
//                Log.d("PRO",den+" , "+ctva+","+Siruri.str(nPret,8,Biz.ConstCalcul.ZEC_PRET_CU)+","+Siruri.str(nCant,8,3));
                if (nPret>0) { 
	                mFMP.command49Variant0Version14(
	                		den ,
	                		"" ,
	                		ctva, 
	                		Siruri.str(nPret,8,Biz.ConstCalcul.ZEC_PRET_CU).replace(" ",""),
	                		Siruri.str(nCant,8,3).replace(" ","")
	                		);
                }
                reducere=reducere-nCant*(nPret-nPretCasa);
                		//Math.round(nCant*(nPret-nPretCasa)*100)/100;
                crs.moveToNext();
			}
			if (reducere!=0)
				mFMP.command51Variant0Version2(Siruri.str(reducere, 8, 2).replace(" ", ""));
			if (lModTestCasa) 	{
				Thread.sleep(1000);
				mFMP.checkAndResolve();
				nRez=0;
			} else {
	            mFMP.totalInCash();
	            Thread.sleep(1000);
	            mFMP.closeFiscalCheck();
	            nRez=0;
			}
		} catch (FiscalPrinterException e2) {
			// TODO Auto-generated catch block
			Log.d("PRO","eroare 1");
			e2.printStackTrace();
			try {
				Log.d("PRO","eroare 2");

				mFMP.checkAndResolve();
				nRez=0;
				Log.d("PRO","eroare 3");

			} catch (FiscalPrinterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IllegalArgumentException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} // deschide bonul fiscal
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		try {
			if (mFMP!=null) {
				Thread.sleep(1000);
//				printer.mmSocket.getInputStream().close();
//				printer.mmSocket.getOutputStream().close();
				Log.d("PRO","LISTBON 31");
				mFMP.close();
			}
		}  catch (Exception e1) {
			Log.d("PRO","LISTBON 35");
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Log.d("PRO","LISTBON 9");
			 printer.closeBT();
			Log.d("PRO","LISTBON 10");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("PRO","LISTBON 11");
			e.printStackTrace();
		}
	}
	
	crs.close();
	return nRez;
}


}
	