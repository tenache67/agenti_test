package ro.prosoftsrl.listare;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.bluet.Bluet;
import ro.prosoftsrl.diverse.ConvertNumar;
import ro.prosoftsrl.diverse.Siruri;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.datecs.api.printer.Printer;

public class ListaFormat {
//    private static Printer mPrinter ;
//    private static PrinterInformation mPrinterInfo ;
//
//    public static void setmPrinterInfo(PrinterInformation mPrinterInfo) {
//        ListaFormat.mPrinterInfo = mPrinterInfo;
//    }
//    public static void setmPrinter(Printer mPrinter) {
//        ListaFormat.mPrinter = mPrinter;
//    }


	public static String[] getImageTransAm(Cursor crs, int versiune ,Context context) {
		ListaImage img=null;
		int nLinHeader=2;
		int nTopSubsol=10;
		String sCarLinii="=";
		int nLinPePag=0;
		int nVertClient=70;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//		return settings.getBoolean(context.getString(R.string.key_ecran4_pret_vanzare), true);
		switch (versiune) {
		case ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY: 
			nLinPePag=96;
			img=new ListaImage(nLinPePag, 130, 5,0);
            img.adText(1, 1, 120,  Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 0, 1, 13, "Furnizor: ");
            img.adText(nLinHeader + 0, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 0, 45, 0, "AVIZ DE INSOTIRE");
            img.adText(nLinHeader + 1, 45, 0, "   AL MARFII");
            img.adText(nLinHeader + 1, 1, 13, "Nr Inr. RC: ");
            img.adText(nLinHeader + 1, 14, 25,settings.getString(context.getString(R.string.key_ecran4_nrrc),"") );
            img.adText(nLinHeader + 2, 45,0, "Seria: BETTY2 "+" Nr:" 
            		+ crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_NR_DOC)));
            img.adText(nLinHeader + 2, 1, 13, "CIF: ");
            img.adText(nLinHeader + 2, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 3, 45, 35, "Data: "+
            		Siruri.dtoc(Siruri.cTod(
            		crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_DATA)))
            		));
            img.adText(nLinHeader + 3, 1, 13, "Sediu: ");
            img.adText(nLinHeader + 3, 14, 28, settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 4, 1, 13,  "Banca: ");
            img.adText(nLinHeader + 4, 14, 25,settings.getString(context.getString(R.string.key_ecran4_banca1),"") );
            img.adText(nLinHeader + 5, 1, 13, "IBAN: ");
            img.adText(nLinHeader + 5, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            img.adText(nLinHeader + 6, 1, 13, "Cap. social: ");
            img.adText(nLinHeader + 6, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cap_social),"") );
            img.adText(nLinHeader + 6, 40, 40, "Tel/Fax:"+ 
            		settings.getString(context.getString(R.string.key_ecran4_telfax),""));
      //////incepe pentru partea de client unde se trece tot furnizorul
            img.adText(nLinHeader + 0, nVertClient, 13, "Cumparator: ");
            img.adText(nLinHeader + 0, nVertClient + 14, 40, 
            		settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 1, nVertClient + 14, 40,"Gestiune Amanunt");
            
            img.adText(nLinHeader + 2, nVertClient, 13, "Nr inr. RC: ");
            img.adText(nLinHeader + 2, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_nrrc),""));
            img.adText(nLinHeader + 3, nVertClient, 13, "CIF: ");
            img.adText(nLinHeader + 3, nVertClient + 14, 30,
            		settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 4, nVertClient, 13, "Sediu: ");
            img.adText(nLinHeader + 4, nVertClient + 14, 50 - 15, 
            		settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 5, nVertClient, 13, "Cont: ");
            img.adText(nLinHeader + 5, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_banca1),""));
            img.adText(nLinHeader + 6, nVertClient, 12, "Banca: ");
            img.adText(nLinHeader + 6, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            
            img.adText(nLinHeader + 7, 1, 120,Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 8, 1, 120, "Crt   Denumire produse                            UM       Cantitate         Pret ");
            img.adText(nLinHeader + 9, 1, 120, "                                                                                  ");
            img.adText(nLinHeader + 10, 1, 120, Siruri.replicate(sCarLinii, 120));
            int nLinPozitii=0, nColPozitii=0; 
            double nSumaTot = 0;
            nLinPozitii = nLinHeader + 11;
            nColPozitii = 1;
            int k=0;
            while (!crs.isAfterLast()) {
                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k+1));
                img.adText(nLinPozitii + k, nColPozitii + 7, 35,
                		crs.getString(crs.getColumnIndexOrThrow(Table_Produse.TABLE_NAME+"_"+Table_Produse.COL_DENUMIRE)));
                img.adText(nLinPozitii + k, nColPozitii + 51, 3,"bc");             	
                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8,  
                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME + "_" + Table_Pozitii.COL_CANTITATE)), 7, 1));
                img.adText(nLinPozitii + k, nColPozitii + 81 + 7, 7,
                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PRET_CU)),7,Biz.ConstCalcul.ZEC_PRET_CU));
                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7,
                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_FARA))+
                				crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_TVA))
                				,7,	Biz.ConstCalcul.ZEC_VAL_FARA));
                nSumaTot=nSumaTot+
                		crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE))*
                		crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PRET_CU));
                crs.moveToNext();
                k=k+1;
			}
            crs.moveToFirst();
            nTopSubsol=15;
            img.adText(nTopSubsol, 1, 120,  Siruri.replicate(sCarLinii, 120),true);
            img.adText(nTopSubsol - 1, nColPozitii + 93, 8, Siruri.str(nSumaTot, 10, 2), true);
            img.adText(nTopSubsol - 1, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 1, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 88, "|" +Siruri.replicate("-",120 - 88 - 1) + "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 120, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 13, 30, "Delegat:" + settings.getString(context.getString(R.string.key_ecran1_numeagent),""),true);
            img.adText(nTopSubsol - 1, nColPozitii + 45, 8, "B.I.:"+ settings.getString(context.getString(R.string.key_ecran1_biagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 13, 15, "Elib.de:" + settings.getString(context.getString(R.string.key_ecran1_polagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 30, 15, "Auto :" + settings.getString(context.getString(R.string.key_ecran1_auto),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 46, "Data livr: "+Siruri.dtoc(Calendar.getInstance()) ,true);
            
            img.adText(nTopSubsol - 1, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 11, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 0, "Semnatura", true);
            img.adText(nTopSubsol - 2, nColPozitii + 0, "furnizor", true);

            img.adText(nTopSubsol - 1, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 70, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 72, "Semnatura", true);
            img.adText(nTopSubsol - 2, nColPozitii + 72, "si stampila", true);
            img.adText(nTopSubsol - 3, nColPozitii + 72, "de primire", true);
            img.adText(nTopSubsol - 6, 1, 120, Siruri.replicate(sCarLinii,120), true);
			
			break;
			
		}
		return img.format();

	}
	
	
	public static String[] getImageAvizInc(Cursor crs, int versiune ,Context context) {
		ListaImage img=null;
		int nLinHeader=2;
		int nTopSubsol=10;
		String sCarLinii="=";
		int nVertClient=70;
		int nLinPePag=0;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//		return settings.getBoolean(context.getString(R.string.key_ecran4_pret_vanzare), true);
		switch (versiune) {
		case ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY: 
			nLinPePag=96;
			img=new ListaImage(nLinPePag, 130, 5,0);
            img.adText(1, 1, 120,  Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 0, 1, 13, "Furnizor: ");
            img.adText(nLinHeader + 0, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 0, 45, 0, "AVIZ DE INSOTIRE");
            img.adText(nLinHeader + 1, 1, 13, "Nr Inr. RC: ");
            img.adText(nLinHeader + 1, 14, 25,settings.getString(context.getString(R.string.key_ecran4_nrrc),"") );
            img.adText(nLinHeader + 1, 45, 0, "   AL MARFII");
            img.adText(nLinHeader + 2, 45,0, "Seria: BETTY2  "+" Nr:" 
            		+ crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_NR_DOC)));
            img.adText(nLinHeader + 2, 1, 13, "CIF: ");
            img.adText(nLinHeader + 2, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 3, 45, 35, "Data: "+
            		Siruri.dtoc(Siruri.cTod(
            		crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_DATA)))
            		));
            img.adText(nLinHeader + 3, 1, 13, "Sediu: ");
            img.adText(nLinHeader + 3, 14, 28, settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 4, 1, 13,  "Banca: ");
            img.adText(nLinHeader + 4, 14, 25,settings.getString(context.getString(R.string.key_ecran4_banca1),"") );
            img.adText(nLinHeader + 5, 1, 13, "IBAN: ");
            img.adText(nLinHeader + 5, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            img.adText(nLinHeader + 6, 1, 13, "Cap. social: ");
            img.adText(nLinHeader + 6, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cap_social),"") );
            img.adText(nLinHeader + 6, 40, 40, "Tel/Fax:"+ 
            		settings.getString(context.getString(R.string.key_ecran4_telfax),""));
            
      //////incepe pentru partea de client unde se trece tot furnizorul
            img.adText(nLinHeader + 0, nVertClient, 13, "Cumparator: ");
            img.adText(nLinHeader + 0, nVertClient + 14, 40, 
            		settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 1, nVertClient + 14, 40,"Gestiune Distributie");            
            img.adText(nLinHeader + 2, nVertClient, 13, "Nr inr. RC: ");
            img.adText(nLinHeader + 2, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_nrrc),""));
            img.adText(nLinHeader + 3, nVertClient, 13, "CIF: ");
            img.adText(nLinHeader + 3, nVertClient + 14, 30,
            		settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 4, nVertClient, 13, "Sediu: ");
            img.adText(nLinHeader + 4, nVertClient + 14, 50 - 15, 
            		settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 5, nVertClient, 13, "Cont: ");
            img.adText(nLinHeader + 5, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_banca1),""));
            img.adText(nLinHeader + 6, nVertClient, 12, "Banca: ");
            img.adText(nLinHeader + 6, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            
            img.adText(nLinHeader + 7, 1, 120,Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 8, 1, 120, "Crt   Denumire produse                            UM       Cantitate          ");
            img.adText(nLinHeader + 9, 1, 120, "                                                                              ");
            img.adText(nLinHeader + 10, 1, 120, Siruri.replicate(sCarLinii, 120));
            int nLinPozitii=0, nColPozitii=0; 
            double nSumaCant = 0;
            nLinPozitii = nLinHeader + 11;
            nColPozitii = 1;
            int k=0;
            while (!crs.isAfterLast()) {
                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k+1));
                img.adText(nLinPozitii + k, nColPozitii + 7, 35,
                		crs.getString(crs.getColumnIndexOrThrow(Table_Produse.TABLE_NAME + "_" + Table_Produse.COL_DENUMIRE)));
                img.adText(nLinPozitii + k, nColPozitii + 51, 3,"bc");             	
                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8,  
                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE)),7,1));
                nSumaCant=nSumaCant+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE));
                crs.moveToNext();
                k=k+1;
			}
            crs.moveToFirst();
            nTopSubsol=15;
            img.adText(nTopSubsol, 1, 120,  Siruri.replicate(sCarLinii, 120),true);
            img.adText(nTopSubsol - 1, nColPozitii + 93, 8, Siruri.str(nSumaCant, 10, 2), true);
            img.adText(nTopSubsol - 1, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 1, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 88, "|" +Siruri.replicate("-",120 - 88 - 1) + "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 120, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 13, 30, "Delegat:" + settings.getString(context.getString(R.string.key_ecran1_numeagent),""),true);
            img.adText(nTopSubsol - 1, nColPozitii + 45, 8, "B.I.:"+ settings.getString(context.getString(R.string.key_ecran1_biagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 13, 15, "Elib.de:" + settings.getString(context.getString(R.string.key_ecran1_polagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 30, 15, "Auto :" + settings.getString(context.getString(R.string.key_ecran1_auto),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 46, "Data livr: "+Siruri.dtoc(Calendar.getInstance()) ,true);
            
            img.adText(nTopSubsol - 1, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 11, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 0, "Semnatura", true);
            img.adText(nTopSubsol - 2, nColPozitii + 0, "furnizor", true);

            img.adText(nTopSubsol - 1, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 70, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 72, "Semnatura", true);
            img.adText(nTopSubsol - 2, nColPozitii + 72, "si stampila", true);
            img.adText(nTopSubsol - 3, nColPozitii + 72, "de primire", true);
            img.adText(nTopSubsol - 6, 1, 120, Siruri.replicate(sCarLinii,120), true);
			
			break;
			
		}
		return img.format();

	}

	public static String[] getImageAvizDesc(Cursor crs, int versiune ,Context context) {
		ListaImage img=null;
		int nLinHeader=2;
		int nTopSubsol=10;
		String sCarLinii="=";
		int nVertClient=70;
		int nLinPePag=0;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//		return settings.getBoolean(context.getString(R.string.key_ecran4_pret_vanzare), true);
		switch (versiune) {
		case ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY: 
			nLinPePag=96;
			img=new ListaImage(nLinPePag, 130, 5,0);
            img.adText(1, 1, 120,  Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 0, 1, 13, "Furnizor: ");
            img.adText(nLinHeader + 0, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 0, 45, 0, "AVIZ DE INSOTIRE");
            img.adText(nLinHeader + 1, 45, 0, "   DESCARCARE");
            img.adText(nLinHeader + 1, 1, 13, "Nr Inr. RC: ");
            img.adText(nLinHeader + 1, 14, 25,settings.getString(context.getString(R.string.key_ecran4_nrrc),"") );
            img.adText(nLinHeader + 2, 45,0, "Seria:BETTY2  "+" Nr:" 
            		+ crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_NR_DOC)));
            img.adText(nLinHeader + 2, 1, 13, "CIF: ");
            img.adText(nLinHeader + 2, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 3, 45, 35, "Data: "+
            		Siruri.dtoc(Siruri.cTod(
            		crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_DATA)))
            		));
            img.adText(nLinHeader + 3, 1, 13, "Sediu: ");
            img.adText(nLinHeader + 3, 14, 28, settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 4, 1, 13,  "Banca: ");
            img.adText(nLinHeader + 4, 14, 25,settings.getString(context.getString(R.string.key_ecran4_banca1),"") );
            img.adText(nLinHeader + 5, 1, 13, "IBAN: ");
            img.adText(nLinHeader + 5, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            img.adText(nLinHeader + 6, 1, 13, "Cap. social: ");
            img.adText(nLinHeader + 6, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cap_social),"") );
            img.adText(nLinHeader + 6, 40, 40, "Tel/Fax:"+ 
            		settings.getString(context.getString(R.string.key_ecran4_telfax),""));
//////incepe pentru partea de client unde se trece tot furnizorul
            img.adText(nLinHeader + 0, nVertClient, 13, "Cumparator: ");
            img.adText(nLinHeader + 0, nVertClient + 14, 40, 
            		settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 1, nVertClient + 14, 40,"Gestiune Distributie");
            img.adText(nLinHeader + 2, nVertClient, 13, "Nr inr. RC: ");
            img.adText(nLinHeader + 2, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_nrrc),""));
            img.adText(nLinHeader + 3, nVertClient, 13, "CIF: ");
            img.adText(nLinHeader + 3, nVertClient + 14, 30,
            		settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 4, nVertClient, 13, "Sediu: ");
            img.adText(nLinHeader + 4, nVertClient + 14, 50 - 15, 
            		settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 5, nVertClient, 13, "Cont: ");
            img.adText(nLinHeader + 5, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_banca1),""));
            img.adText(nLinHeader + 6, nVertClient, 12, "Banca: ");
            img.adText(nLinHeader + 6, nVertClient + 14, 30, 
            		settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            
            
            
            img.adText(nLinHeader + 7, 1, 120,Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 8, 1, 120, "Crt   Denumire produse                            UM       Cantitate          ");
            img.adText(nLinHeader + 9, 1, 120, "                                                                              ");
            img.adText(nLinHeader + 10, 1, 120, Siruri.replicate(sCarLinii, 120));
            int nLinPozitii=0, nColPozitii=0; 
            double nSumaCant = 0;
            nLinPozitii = nLinHeader + 11;
            nColPozitii = 1;
            int k=0;
            while (!crs.isAfterLast()) {
                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k+1));
                img.adText(nLinPozitii + k, nColPozitii + 7, 35,
                		crs.getString(crs.getColumnIndexOrThrow(Table_Produse.TABLE_NAME+"_"+Table_Produse.COL_DENUMIRE)));
                img.adText(nLinPozitii + k, nColPozitii + 51, 3,"bc");             	
                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8,  
                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE)),7,1));
                nSumaCant=nSumaCant+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE));
                crs.moveToNext();
                k=k+1;
			}
            crs.moveToFirst();
            nTopSubsol=15;
            img.adText(nTopSubsol, 1, 120,  Siruri.replicate(sCarLinii, 120),true);
            img.adText(nTopSubsol - 1, nColPozitii + 93, 8, Siruri.str(nSumaCant, 10, 2), true);
            img.adText(nTopSubsol - 1, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 1, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 88, "|" +Siruri.replicate("-",120 - 88 - 1) + "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 120, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 88, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 120, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 13, 30, "Delegat:" + settings.getString(context.getString(R.string.key_ecran1_numeagent),""),true);
            img.adText(nTopSubsol - 1, nColPozitii + 45, 8, "B.I.:"+ settings.getString(context.getString(R.string.key_ecran1_biagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 13, 15, "Elib.de:" + settings.getString(context.getString(R.string.key_ecran1_polagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 30, 15, "Auto :" + settings.getString(context.getString(R.string.key_ecran1_auto),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 46, "Data livr: "+Siruri.dtoc(Calendar.getInstance()) ,true);
            
            img.adText(nTopSubsol - 1, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 11, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 11, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 0, "Semnatura", true);
            img.adText(nTopSubsol - 2, nColPozitii + 0, "furnizor", true);

            img.adText(nTopSubsol - 1, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 2, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 3, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 4, nColPozitii + 70, "|", true);
            img.adText(nTopSubsol - 5, nColPozitii + 70, "|", true);

            img.adText(nTopSubsol - 1, nColPozitii + 72, "Semnatura", true);
            img.adText(nTopSubsol - 2, nColPozitii + 72, "si stampila", true);
            img.adText(nTopSubsol - 3, nColPozitii + 72, "de primire", true);
            img.adText(nTopSubsol - 6, 1, 120, Siruri.replicate(sCarLinii,120), true);
			
			break;
			
		}
		return img.format();

	}
	
	
	public static String[] getImageAvizClient (Cursor crs, int versiune ,Context context) {
		ListaImage img=null;
		int nLinHeader=2;
		int nTopSubsol=10;
		String sCarLinii="=";
		int nVertClient=70;
		int nLinPePag=0;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//		return settings.getBoolean(context.getString(R.string.key_ecran4_pret_vanzare), true);
		switch (versiune) {
		case ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY:
            String sConformitate1 = "Declaram pe proprie raspundere ca produsele de panifica-";
            String sConformitate2  = "tie livrate conform facturii sunt in conformitate cu le-";
            String sConformitate3 = "gislatia sanitar veterinara si pentru siguranta alimentului in vigoare";

			nLinPePag=96;
			img=new ListaImage(nLinPePag, 130, 5,0);
            img.adText(1, 1, 120,  Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 0, 1, 13, "Furnizor: ");
            img.adText(nLinHeader + 0, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 0, 45, 0, "AVIZ DE INSOTIRE");
            img.adText(nLinHeader + 1, 45, 0, "   AL MARFII");
            img.adText(nLinHeader + 1, 1, 13, "Nr Inr. RC: ");
            img.adText(nLinHeader + 1, 14, 25,settings.getString(context.getString(R.string.key_ecran4_nrrc),"") );
            img.adText(nLinHeader + 2, 45,0, "Seria:"+settings.getString(context.getString(R.string.key_ecran4_serie_facturi),"")+" Nr:" 
            		+ crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_NR_DOC)));
            img.adText(nLinHeader + 2, 1, 13, "CIF: ");
            img.adText(nLinHeader + 2, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 3, 45, 35, "Data: "+
            		Siruri.dtoc(Siruri.cTod(
            		crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_DATA)))
            		));
            img.adText(nLinHeader + 3, 1, 13, "Sediu: ");
            img.adText(nLinHeader + 3, 14, 28, settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 4, 1, 13,  "Banca: ");
            img.adText(nLinHeader + 4, 14, 25,settings.getString(context.getString(R.string.key_ecran4_banca1),"") );
            img.adText(nLinHeader + 5, 1, 13, "IBAN: ");
            img.adText(nLinHeader + 5, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            img.adText(nLinHeader + 6, 1, 13, "Cap. social: ");
            img.adText(nLinHeader + 6, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cap_social),"") );
            img.adText(nLinHeader + 6, 40, 40, "Tel/Fax:"+ 
            		settings.getString(context.getString(R.string.key_ecran4_telfax),""));
            img.adText(nLinHeader + 0, nVertClient, 13, "Client: ");
            img.adText(nLinHeader + 0, nVertClient + 14, 40, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_DENUMIRE)));
            img.adText(nLinHeader + 1, nVertClient, 13, "Nr inr. RC: ");
            img.adText(nLinHeader + 1, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_RC)));
            img.adText(nLinHeader + 2, nVertClient, 13, "CIF: ");
            img.adText(nLinHeader + 2, nVertClient + 14, 30,
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_FISc)));
            img.adText(nLinHeader + 3, nVertClient, 12, "Localitate: ");
            img.adText(nLinHeader + 3, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_LOC)));
            img.adText(nLinHeader + 4, nVertClient, 13, "Adresa: ");
            img.adText(nLinHeader + 4, nVertClient + 14, 50 - 15, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_ADRESA)));
            img.adText(nLinHeader + 5, nVertClient, 13, "Cont: ");
            img.adText(nLinHeader + 5, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_CONT)));
            img.adText(nLinHeader + 6, nVertClient, 12, "Banca: ");
            img.adText(nLinHeader + 6, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_BANCA)));
            img.adText(nLinHeader + 7, 1, 120, Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 8, 1, 120, "Crt   Denumire produse                            UM   %RED       Cantitate   TVA%   Pret       Valoare     ");
            img.adText(nLinHeader + 9, 1, 120, "                                                                                     fara TVA   fara TVA    ");
            img.adText(nLinHeader + 10, 1, 120,Siruri.replicate(sCarLinii, 120));
            int nLinPozitii=nLinHeader+11;
            int nColPozitii=1;
            int k=0;
            int nGrupTva=0;
            double nCotaTva=0;
//            double nLastCota=0;
            double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
            //double nTotValFara=0, nTotValTva=0, nTotValRed=0, nTotTvaRed=0;
            
        	while (!crs.isAfterLast()){
                nCotaTva=Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0);
            	while (!crs.isAfterLast() && (nCotaTva==Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0))) {
	                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k+1));
	                img.adText(nLinPozitii + k, nColPozitii + 7, 35,
	                		crs.getString(crs.getColumnIndexOrThrow(Table_Produse.TABLE_NAME+"_"+Table_Produse.COL_DENUMIRE)));
	                img.adText(nLinPozitii + k, nColPozitii + 51, 3,"bc");             	
	                if (crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PROC_RED))!=0)
	                	img.adText(nLinPozitii + k, nColPozitii + 54, 7,
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PROC_RED)),7,Biz.ConstCalcul.ZEC_PROC_RED));
	                else
	                	img.adText(nLinPozitii + k, nColPozitii + 54, 7," ");
	                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8,  
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE)),7,1));
	                img.adText(nLinPozitii + k, nColPozitii + 78, 4,
	                		Siruri.str(Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0),4,0)+
	                		"%");
	                img.adText(nLinPozitii + k, nColPozitii + 81 + 7, 7,
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PRET_FARA)),7,Biz.ConstCalcul.ZEC_PRET_FARA));
	                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7,
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_FARA)),7,Biz.ConstCalcul.ZEC_VAL_FARA));
//	                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7, 
//	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_TVA)),7,Biz.ConstCalcul.ZEC_VAL_TVA));
	                nValFara=nValFara+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_FARA));
	                nValTva=nValTva+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_TVA));
	                nValRed=nValRed+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_RED));
	                nTvaRed=nTvaRed+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_TVA_RED));
	                crs.moveToNext();
	                k=k+1;
            	}
            	nGrupTva=nGrupTva+1;
            	//nLastCotaTva=Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0);
            }
//            if (nGrupTva>true); {
//    			img.adText(nLinPozitii + k, nColPozitii + 0,0 ,"      Subtotal cota: "+nLastCota+" %");
//                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7,Siruri.str(nValFara,7,Biz.ConstCalcul.ZEC_VAL_FARA));
//                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7,Siruri.str(nValTva,7,Biz.ConstCalcul.ZEC_VAL_TVA));
//            	k=k+1;
//            }
            if (nValRed!=0) {
                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k + 1));
                img.adText(nLinPozitii + k, nColPozitii + 7, 35, "Total marfa");
                img.adText(nLinPozitii + k, nColPozitii + 51, 3, "---");
                img.adText(nLinPozitii + k, nColPozitii + 54, 7, "-------");
                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8, "---");
                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7, Siruri.str(nValFara,7,Biz.ConstCalcul.ZEC_VAL_FARA));
                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7, Siruri.str(nValTva,7,Biz.ConstCalcul.ZEC_VAL_TVA));
                k = k + 1;
                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k + 1));
                img.adText(nLinPozitii + k, nColPozitii + 7, 35, "Discount acordat");
                img.adText(nLinPozitii + k, nColPozitii + 51, 3, "---");
                img.adText(nLinPozitii + k, nColPozitii + 54, 7, "-------");
                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8, "---");
                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7, Siruri.str(nValRed,7,Biz.ConstCalcul.ZEC_VAL_FARA));
                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7,Siruri.str(nTvaRed,7,Biz.ConstCalcul.ZEC_VAL_TVA));            	
            }
            // incepe subsolul paginii
            // se pozitioneaza fata de sfirsitul paginii
            crs.moveToFirst();
            nTopSubsol = 15;

            img.adText(nTopSubsol - 0, 1, 120,Siruri.replicate(sCarLinii, 120),true);
            img.adText(nTopSubsol - 1, nColPozitii + 93 + 7, 7, 
            		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))
            				,7,Biz.ConstCalcul.ZEC_VAL_FARA),true);
//            img.adText(nTopSubsol - 1, nColPozitii + 105 + 7, 7, 
//            		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_TVA))
//            		,7,Biz.ConstCalcul.ZEC_VAL_TVA),true);
            img.adText(nTopSubsol - 3, nColPozitii + 91, 10, "Total cu",true);
            img.adText(nTopSubsol - 4, nColPozitii + 91, 10, "  TVA",true);
            img.adText(nTopSubsol - 3, nColPozitii + 102, 8, Siruri.str(
            		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))+
            		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_TVA))
            		,7,Biz.ConstCalcul.ZEC_VAL_CU),true);
            img.adText(nTopSubsol - 1, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 1, nColPozitii + 120, "|",true);
            img.adText(nTopSubsol - 2, nColPozitii + 88, "|" + Siruri.replicate("-",120 - 88 -1)+ "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 120, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 120, "|",true);
            img.adText(nTopSubsol - 5, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 5, nColPozitii + 120, "|",true);

            img.adText(nTopSubsol - 1, nColPozitii + 13, 30, "Delegat:" + settings.getString(context.getString(R.string.key_ecran1_numeagent),""),true);
            img.adText(nTopSubsol - 1, nColPozitii + 45, 18, "B.I.:"+ settings.getString(context.getString(R.string.key_ecran1_biagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 13, 15, "Elib. de:" + settings.getString(context.getString(R.string.key_ecran1_polagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 30, 15, "Auto nr.:" + settings.getString(context.getString(R.string.key_ecran1_auto),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 46, "Data livr: "+Siruri.dtoc(Calendar.getInstance()) ,true);
            img.adText(nTopSubsol - 3, nColPozitii + 13, sConformitate1,true);
            img.adText(nTopSubsol - 4, nColPozitii + 13, sConformitate2,true);
            img.adText(nTopSubsol - 5, nColPozitii + 13, sConformitate3,true);

            img.adText(nTopSubsol - 1, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 2, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 5, nColPozitii + 11, "|",true);

            img.adText(nTopSubsol - 1, nColPozitii + 0, "Semnatura",true);
            img.adText(nTopSubsol - 2, nColPozitii + 0, "furnizor",true);

            img.adText(nTopSubsol - 1, nColPozitii + 70, "|",true);
            img.adText(nTopSubsol - 2, nColPozitii + 70, "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 70, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 70, "|",true);

            img.adText(nTopSubsol - 1, nColPozitii + 72, "Semnatura",true);
            img.adText(nTopSubsol - 2, nColPozitii + 72, "si stampila",true);
            img.adText(nTopSubsol - 3, nColPozitii + 72, "de primire",true);
            img.adText(nTopSubsol - 6, 1, 120, Siruri.replicate(sCarLinii,120),true);
            
			break; // de la sqithc
		}
		return img.format();
	}

	public static String[] getImageComanda (Cursor crs, int versiune ,Context context) {
		ListaImage img=null;
        int nLinHeader = 2 ;// linia de inceput header
        int nTopSubsol = 10;
        int nLinPePag=42;
        String sCarLinii = "=";
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		img=new ListaImage(nLinPePag, 130, 10,0);
		
        img.adText(1, 1, 120, Siruri.replicate(sCarLinii,120));
        img.adText(nLinHeader + 0, 1, 13, "Furnizor: ");
        img.adText(nLinHeader + 0, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
        img.adText(nLinHeader + 0, 45,0, "COMANDA");
        img.adText(nLinHeader + 1, 45, 0,"DE LIVRARE");

        img.adText(nLinHeader + 1, 1, 13, "Nr Inr. RC: ");
        img.adText(nLinHeader + 1, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nrrc),""));

        img.adText(nLinHeader + 2, 1, 13, "CIF: ");
        img.adText(nLinHeader + 2, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cf),""));
        img.adText(nLinHeader + 2, 45, 35, "Data: " +
        		Siruri.dtoc(Siruri.cTod(
                crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_DATA)))
                ));
        // img.adText(nLinHeader + 3, 45, 17, "Cota TVA :" & main.nCota1Tva & " % ")

        img.adText(nLinHeader + 3, 1, 13, "Sediu: ");
        img.adText(nLinHeader + 3, 14, 28, settings.getString(context.getString(R.string.key_ecran4_adresa),""));
        img.adText(nLinHeader + 4, 1, 13, "IBAN: ");
        img.adText(nLinHeader + 4, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cont1),""));
        img.adText(nLinHeader + 5, 1, 13, "Banca: ");
        img.adText(nLinHeader + 5, 14, 25, settings.getString(context.getString(R.string.key_ecran4_banca1),""));
        img.adText(nLinHeader + 6, 1, 13, "Cap. social: ");
        img.adText(nLinHeader + 6, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cap_social),""));
        img.adText(nLinHeader + 6, 40, 40, "Tel/Fax: "+settings.getString(context.getString(R.string.key_ecran4_telfax),""));
        int nVertClient  = 70;
        img.adText(nLinHeader + 0, nVertClient, 13, "Client: ");
        img.adText(nLinHeader + 0, nVertClient + 14, 40, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_DENUMIRE)));
        img.adText(nLinHeader + 1, nVertClient, 13, "Nr inr. RC: ");
        img.adText(nLinHeader + 1, nVertClient + 14, 30, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_RC)));
        img.adText(nLinHeader + 2, nVertClient, 13, "CIF: ");
        img.adText(nLinHeader + 2, nVertClient + 14, 30,crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_FISc)));
        img.adText(nLinHeader + 3, nVertClient, 12, "Localitate: ");
        img.adText(nLinHeader + 3, nVertClient + 14, 30, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_LOC)));
        img.adText(nLinHeader + 4, nVertClient, 13, "Adresa: ");
        img.adText(nLinHeader + 4, nVertClient + 14, 50 - 15, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_ADRESA)));
        img.adText(nLinHeader + 5, nVertClient, 13, "Cont: ");
        img.adText(nLinHeader + 5, nVertClient + 14, 30, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_CONT)));
        img.adText(nLinHeader + 6, nVertClient, 12, "Banca: ");
        img.adText(nLinHeader + 6, nVertClient + 14, 30, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_BANCA)));
        img.adText(nLinHeader + 7, 1, 120, Siruri.replicate(sCarLinii,120));

        //'''''''''''''''''''''''''''''''''''''''''''12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
        img.adText(nLinHeader + 8, 1, 120, "Crt   Denumire produse                            UM   %RED       Cantitate   TVA%   Pret       Valoare  ");
        img.adText(nLinHeader + 9, 1, 120, "                                                                                     fara TVA   fara TVA ");
        img.adText(nLinHeader + 10, 1, 120, Siruri.replicate(sCarLinii,120));
        int nLinPozitii=nLinHeader + 11, nColPozitii =1;
        int k =0;
        double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
        while (!crs.isAfterLast()) {
            img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k+1));
            img.adText(nLinPozitii + k, nColPozitii + 7, 35,
            		crs.getString(crs.getColumnIndexOrThrow(Table_Produse.TABLE_NAME+"_"+Table_Produse.COL_DENUMIRE)));
            img.adText(nLinPozitii + k, nColPozitii + 51, 3,"bc");  
            if (crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PROC_RED))!=0)
            	img.adText(nLinPozitii + k, nColPozitii + 54, 7,
            		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PROC_RED)),7,Biz.ConstCalcul.ZEC_PROC_RED));
            else
            	img.adText(nLinPozitii + k, nColPozitii + 54, 7," ");
            img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8,  
            	Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE)),7,1));
            img.adText(nLinPozitii + k, nColPozitii + 78, 4,
            		Siruri.str(Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0),4,0)+
            		"%");
            img.adText(nLinPozitii + k, nColPozitii + 81 + 7, 7,
            		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PRET_FARA)),7,Biz.ConstCalcul.ZEC_PRET_FARA));
            img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7,
            		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_FARA)),7,Biz.ConstCalcul.ZEC_VAL_FARA));
            nValFara=nValFara+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_FARA));
            nValTva=nValTva+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_TVA));
            nValRed=nValRed+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_RED));
            nTvaRed=nTvaRed+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_TVA_RED));
            crs.moveToNext();
            k=k+1;
        }
        crs.moveToFirst();
        if (nValRed!=0) {
            img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k + 1));
            img.adText(nLinPozitii + k, nColPozitii + 7, 35, "Total marfa");
            img.adText(nLinPozitii + k, nColPozitii + 51, 3, "---");
            img.adText(nLinPozitii + k, nColPozitii + 54, 7, "-------");
            img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8, "---");
            img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7, Siruri.str(nValFara,7,Biz.ConstCalcul.ZEC_VAL_FARA));
            img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7, Siruri.str(nValTva,7,Biz.ConstCalcul.ZEC_VAL_TVA));
            k = k + 1;
            img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k + 1));
            img.adText(nLinPozitii + k, nColPozitii + 7, 35, "Discount acordat");
            img.adText(nLinPozitii + k, nColPozitii + 51, 3, "---");
            img.adText(nLinPozitii + k, nColPozitii + 54, 7, "-------");
            img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8, "---");
            img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7, Siruri.str(nValRed,7,Biz.ConstCalcul.ZEC_VAL_FARA));
            img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7,Siruri.str(nTvaRed,7,Biz.ConstCalcul.ZEC_VAL_TVA));            	
        }
        // incepe subsolul paginii
        // se pozitioneaza fata de sfirsitul paginii
        nTopSubsol = 15;
        img.adText(nTopSubsol - 0, 1, 120, Siruri.replicate( sCarLinii,120), true);
        img.adText(nTopSubsol - 1, nColPozitii + 93 + 7, 7, 
        		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))
        				,7,Biz.ConstCalcul.ZEC_VAL_FARA),true);
        img.adText(nTopSubsol - 3, nColPozitii + 91, "Total cu TVA:", true);
        img.adText(nTopSubsol - 3, nColPozitii + 114, 8, Siruri.str(
        		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))+
        		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_TVA))
        		,7,Biz.ConstCalcul.ZEC_VAL_CU),true);
        img.adText(nTopSubsol - 1, nColPozitii + 88, "|", true);
        img.adText(nTopSubsol - 1, nColPozitii + 120, "|", true);
        img.adText(nTopSubsol - 2, nColPozitii + 88, "|" + Siruri.replicate("-",120 - 88 - 1) + "|", true);
        img.adText(nTopSubsol - 3, nColPozitii + 88, "|", true);
        img.adText(nTopSubsol - 3, nColPozitii + 120, "|", true);
        img.adText(nTopSubsol - 4, nColPozitii + 88, "|", true);
        img.adText(nTopSubsol - 4, nColPozitii + 120, "|", true);
        img.adText(nTopSubsol - 5, nColPozitii + 88, "|", true);
        img.adText(nTopSubsol - 5, nColPozitii + 120, "|", true);

        img.adText(nTopSubsol - 1, nColPozitii + 13, 30,"Delegat:" + settings.getString(context.getString(R.string.key_ecran1_numeagent),""),true);
        img.adText(nTopSubsol - 1, nColPozitii + 45, 8, "B.I.:"+ settings.getString(context.getString(R.string.key_ecran1_biagent),""),true);
        img.adText(nTopSubsol - 2, nColPozitii + 13, 15, "Elib. de:" + settings.getString(context.getString(R.string.key_ecran1_polagent),""),true);
        img.adText(nTopSubsol - 2, nColPozitii + 30, 15, "Auto nr.:" + settings.getString(context.getString(R.string.key_ecran1_auto),""),true);
        img.adText(nTopSubsol - 3, nColPozitii + 13, "Data livrarii: "+Siruri.dtoc(Calendar.getInstance()) ,true);

        img.adText(nTopSubsol - 1, nColPozitii + 11, "|", true);
        img.adText(nTopSubsol - 2, nColPozitii + 11, "|", true);
        img.adText(nTopSubsol - 3, nColPozitii + 11, "|", true);
        img.adText(nTopSubsol - 4, nColPozitii + 11, "|", true);
        img.adText(nTopSubsol - 5, nColPozitii + 11, "|", true);

        img.adText(nTopSubsol - 1, nColPozitii + 0, "Semnatura", true);
        img.adText(nTopSubsol - 2, nColPozitii + 0, "furnizor", true);

        img.adText(nTopSubsol - 1, nColPozitii + 70, "|", true);
        img.adText(nTopSubsol - 2, nColPozitii + 70, "|", true);
        img.adText(nTopSubsol - 3, nColPozitii + 70, "|", true);
        img.adText(nTopSubsol - 4, nColPozitii + 70, "|", true);
        img.adText(nTopSubsol - 5, nColPozitii + 70, "|", true);

        img.adText(nTopSubsol - 1, nColPozitii + 72, "Semnatura", true);
        img.adText(nTopSubsol - 2, nColPozitii + 72, "si stampila", true);
        img.adText(nTopSubsol - 3, nColPozitii + 72, "de primire", true);
        img.adText(nTopSubsol - 6, 1, 120, Siruri.replicate( sCarLinii,120), true);
        
		return img.format();
	}
	

	public static String[] getImageFactura (Cursor crs, int versiune ,Context context) {
		ListaImage img=null;
		int nLinHeader=2;
		int nTopSubsol=10;
		String sCarLinii="=";
		int nVertClient=70;
		int nLinPePag=0;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//		return settings.getBoolean(context.getString(R.string.key_ecran4_pret_vanzare), true);
		switch (versiune) {
		case ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY:
            String sConformitate1 = "Declaram pe proprie raspundere ca produsele de panifica-";
            String sConformitate2  = "tie livrate conform facturii sunt in conformitate cu le-";
            String sConformitate3 = "gislatia sanitar veterinara si pentru siguranta alimentului in vigoare";

			Boolean lCuChit=(crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_ID_MODPL))==5);
			if (lCuChit)
				nLinPePag=96;
			else
				nLinPePag=48;
			img=new ListaImage(nLinPePag, 130, 5,0);
            img.adText(1, 1, 120,  Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 0, 1, 13, "Furnizor: ");
            img.adText(nLinHeader + 0, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
            img.adText(nLinHeader + 0, 45, 10, "FACTURA");
            img.adText(nLinHeader + 1, 1, 13, "Nr Inr. RC: ");
            img.adText(nLinHeader + 1, 14, 25,settings.getString(context.getString(R.string.key_ecran4_nrrc),"") );
            img.adText(nLinHeader + 1, 45,0, "Seria:"+settings.getString(context.getString(R.string.key_ecran4_serie_facturi),"")+" Nr:" 
            		+ crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_NR_DOC)));
            img.adText(nLinHeader + 2, 1, 13, "CIF: ");
            img.adText(nLinHeader + 2, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cf),""));
            img.adText(nLinHeader + 2, 45, 35, "Data: "+
            		Siruri.dtoc(Siruri.cTod(
            		crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_DATA)))
            		));
            img.adText(nLinHeader + 3, 1, 13, "Sediu: ");
            img.adText(nLinHeader + 3, 14, 28, settings.getString(context.getString(R.string.key_ecran4_adresa),""));
            img.adText(nLinHeader + 4, 1, 13,  "Banca: ");
            img.adText(nLinHeader + 4, 14, 25,settings.getString(context.getString(R.string.key_ecran4_banca1),"") );
            img.adText(nLinHeader + 5, 1, 13, "IBAN: ");
            img.adText(nLinHeader + 5, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cont1),""));
            img.adText(nLinHeader + 6, 1, 13, "Cap. social: ");
            img.adText(nLinHeader + 6, 14, 25,settings.getString(context.getString(R.string.key_ecran4_cap_social),"") );
            img.adText(nLinHeader + 6, 40, 40, "Tel/Fax:"+ 
            		settings.getString(context.getString(R.string.key_ecran4_telfax),""));
            img.adText(nLinHeader + 0, nVertClient, 13, "Client: ");
            img.adText(nLinHeader + 0, nVertClient + 14, 40, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_DENUMIRE)));
            img.adText(nLinHeader + 1, nVertClient, 13, "Nr inr. RC: ");
            img.adText(nLinHeader + 1, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_RC)));
            img.adText(nLinHeader + 2, nVertClient, 13, "CIF: ");
            img.adText(nLinHeader + 2, nVertClient + 14, 30,
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_FISc)));
            img.adText(nLinHeader + 3, nVertClient, 12, "Localitate: ");
            img.adText(nLinHeader + 3, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_LOC)));
            img.adText(nLinHeader + 4, nVertClient, 13, "Adresa: ");
            img.adText(nLinHeader + 4, nVertClient + 14, 50 - 15, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_ADRESA)));
            img.adText(nLinHeader + 5, nVertClient, 13, "Cont: ");
            img.adText(nLinHeader + 5, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_CONT)));
            img.adText(nLinHeader + 6, nVertClient, 12, "Banca: ");
            img.adText(nLinHeader + 6, nVertClient + 14, 30, 
            		crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_BANCA)));
            img.adText(nLinHeader + 7, 1, 120, Siruri.replicate(sCarLinii, 120));
            img.adText(nLinHeader + 8, 1, 120, "Crt   Denumire produse                            UM   %RED       Cantitate   TVA%   Pret       Valoare     Valoare");
            img.adText(nLinHeader + 9, 1, 120, "                                                                                     fara TVA   fara TVA       TVA");
            img.adText(nLinHeader + 10, 1, 120,Siruri.replicate(sCarLinii, 120));
            int nLinPozitii=nLinHeader+11;
            int nColPozitii=1;
            int k=0;
            int nGrupTva=0;
            double nCotaTva=0;
//            double nLastCota=0;
            double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
            //double nTotValFara=0, nTotValTva=0, nTotValRed=0, nTotTvaRed=0;
            
        	while (!crs.isAfterLast()){
                nCotaTva=Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0);
            	while (!crs.isAfterLast() && (nCotaTva==Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0))) {
//            		if (subtotal) {
//  //deocamdata am dezactivat subtotalurile pe cote de tva
//            			img.adText(nLinPozitii + k, nColPozitii + 0,0 ,"      Subtotal cota: "+nLastCota+" %");
//    	                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7,Siruri.str(nValFara,7,Biz.ConstCalcul.ZEC_VAL_FARA));
//    	                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7,Siruri.str(nValTva,7,Biz.ConstCalcul.ZEC_VAL_TVA));
//            			k=k+1;
//    	                nValFara=0;
//    	                nValTva=0;
//            			subtotal=false;
//            		}
	                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k+1));
	                img.adText(nLinPozitii + k, nColPozitii + 7, 35,
	                		crs.getString(crs.getColumnIndexOrThrow(Table_Produse.TABLE_NAME+"_"+Table_Produse.COL_DENUMIRE)));
	                img.adText(nLinPozitii + k, nColPozitii + 51, 3,"bc");             	
	                if (crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PROC_RED))!=0)
	                	img.adText(nLinPozitii + k, nColPozitii + 54, 7,
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PROC_RED)),7,Biz.ConstCalcul.ZEC_PROC_RED));
	                else
	                	img.adText(nLinPozitii + k, nColPozitii + 54, 7," ");
	                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8,  
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_CANTITATE)),7,1));
	                img.adText(nLinPozitii + k, nColPozitii + 78, 4,
	                		Siruri.str(Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0),4,0)+
	                		"%");
	                img.adText(nLinPozitii + k, nColPozitii + 81 + 7, 7,
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_PRET_FARA)),7,Biz.ConstCalcul.ZEC_PRET_FARA));
	                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7,
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_FARA)),7,Biz.ConstCalcul.ZEC_VAL_FARA));
	                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7, 
	                		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_TVA)),7,Biz.ConstCalcul.ZEC_VAL_TVA));
//	                nLastCota=Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0);
	                nValFara=nValFara+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_FARA));
	                nValTva=nValTva+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_TVA));
	                nValRed=nValRed+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_VAL_RED));
	                nTvaRed=nTvaRed+crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_TVA_RED));
	                crs.moveToNext();
	                k=k+1;
            	}
            	nGrupTva=nGrupTva+1;
            	//nLastCotaTva=Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(Table_Pozitii.TABLE_NAME+"_"+Table_Pozitii.COL_COTA_TVA)),0);
            }
//            if (nGrupTva>true); {
//    			img.adText(nLinPozitii + k, nColPozitii + 0,0 ,"      Subtotal cota: "+nLastCota+" %");
//                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7,Siruri.str(nValFara,7,Biz.ConstCalcul.ZEC_VAL_FARA));
//                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7,Siruri.str(nValTva,7,Biz.ConstCalcul.ZEC_VAL_TVA));
//            	k=k+1;
//            }
            if (nValRed!=0) {
                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k + 1));
                img.adText(nLinPozitii + k, nColPozitii + 7, 35, "Total marfa");
                img.adText(nLinPozitii + k, nColPozitii + 51, 3, "---");
                img.adText(nLinPozitii + k, nColPozitii + 54, 7, "-------");
                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8, "---");
                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7, Siruri.str(nValFara,7,Biz.ConstCalcul.ZEC_VAL_FARA));
                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7, Siruri.str(nValTva,7,Biz.ConstCalcul.ZEC_VAL_TVA));
                k = k + 1;
                img.adText(nLinPozitii + k, nColPozitii + 0, 3, String.valueOf(k + 1));
                img.adText(nLinPozitii + k, nColPozitii + 7, 35, "Discount acordat");
                img.adText(nLinPozitii + k, nColPozitii + 51, 3, "---");
                img.adText(nLinPozitii + k, nColPozitii + 54, 7, "-------");
                img.adText(nLinPozitii + k, nColPozitii + 62 + 7, 8, "---");
                img.adText(nLinPozitii + k, nColPozitii + 93 + 7, 7, Siruri.str(nValRed,7,Biz.ConstCalcul.ZEC_VAL_FARA));
                img.adText(nLinPozitii + k, nColPozitii + 105 + 7, 7,Siruri.str(nTvaRed,7,Biz.ConstCalcul.ZEC_VAL_TVA));            	
            }
            // incepe subsolul paginii
            // se pozitioneaza fata de sfirsitul paginii
            crs.moveToFirst();
            if (lCuChit) nTopSubsol = 15 + nLinPePag / 2;
            else  nTopSubsol = 15;

            img.adText(nTopSubsol - 0, 1, 120,Siruri.replicate(sCarLinii, 120),true);
            img.adText(nTopSubsol - 1, nColPozitii + 93 + 7, 7, 
            		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))
            				,7,Biz.ConstCalcul.ZEC_VAL_FARA),true);
            img.adText(nTopSubsol - 1, nColPozitii + 105 + 7, 7, 
            		Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_TVA))
            		,7,Biz.ConstCalcul.ZEC_VAL_TVA),true);
            img.adText(nTopSubsol - 3, nColPozitii + 91, 10, "De plata:",true);
            img.adText(nTopSubsol - 3, nColPozitii + 102, 8, Siruri.str(
            		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))+
            		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_TVA))
            		,7,Biz.ConstCalcul.ZEC_VAL_CU),true);
            img.adText(nTopSubsol - 1, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 1, nColPozitii + 120, "|",true);
            img.adText(nTopSubsol - 2, nColPozitii + 88, "|" + Siruri.replicate("-",120 - 88 -1)+ "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 120, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 120, "|",true);
            img.adText(nTopSubsol - 5, nColPozitii + 88, "|",true);
            img.adText(nTopSubsol - 5, nColPozitii + 120, "|",true);

            img.adText(nTopSubsol - 1, nColPozitii + 13, 30, "Delegat:" + settings.getString(context.getString(R.string.key_ecran1_numeagent),""),true);
            img.adText(nTopSubsol - 1, nColPozitii + 45, 18, "B.I.:"+ settings.getString(context.getString(R.string.key_ecran1_biagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 13, 15, "Elib. de:" + settings.getString(context.getString(R.string.key_ecran1_polagent),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 30, 15, "Auto nr.:" + settings.getString(context.getString(R.string.key_ecran1_auto),""),true);
            img.adText(nTopSubsol - 2, nColPozitii + 46, "Data livr: "+Siruri.dtoc(Calendar.getInstance()) ,true);
            img.adText(nTopSubsol - 3, nColPozitii + 13, sConformitate1,true);
            img.adText(nTopSubsol - 4, nColPozitii + 13, sConformitate2,true);
            img.adText(nTopSubsol - 5, nColPozitii + 13, sConformitate3,true);

            img.adText(nTopSubsol - 1, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 2, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 11, "|",true);
            img.adText(nTopSubsol - 5, nColPozitii + 11, "|",true);

            img.adText(nTopSubsol - 1, nColPozitii + 0, "Semnatura",true);
            img.adText(nTopSubsol - 2, nColPozitii + 0, "furnizor",true);

            img.adText(nTopSubsol - 1, nColPozitii + 70, "|",true);
            img.adText(nTopSubsol - 2, nColPozitii + 70, "|",true);
            img.adText(nTopSubsol - 3, nColPozitii + 70, "|",true);
            img.adText(nTopSubsol - 4, nColPozitii + 70, "|",true);

            img.adText(nTopSubsol - 1, nColPozitii + 72, "Semnatura",true);
            img.adText(nTopSubsol - 2, nColPozitii + 72, "si stampila",true);
            img.adText(nTopSubsol - 3, nColPozitii + 72, "de primire",true);
            img.adText(nTopSubsol - 6, 1, 120, Siruri.replicate(sCarLinii,120),true);
            // imagine pentru chitanta
            if (lCuChit) {
                nTopSubsol = 34;
                img.adText(nTopSubsol, 1, 120, Siruri.replicate(sCarLinii,120), true);
                nLinHeader = nLinPePag - nTopSubsol + 2;
                img.adText(nLinHeader + 0, 1, 13, "Furnizor: ");
                img.adText(nLinHeader + 0, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nume_firma),""));
                img.adText(nLinHeader + 0, 45, 10, "CHITANTA");

                img.adText(nLinHeader + 1, 1, 13, "Nr Inr. RC: ");
                img.adText(nLinHeader + 1, 14, 25, settings.getString(context.getString(R.string.key_ecran4_nrrc),""));
                img.adText(nLinHeader + 1, 45,0,"Seria:"+settings.getString(context.getString(R.string.key_ecran4_serie_facturi),"")+" Nr:" 
                		+ crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_NR_DOC)));

                img.adText(nLinHeader + 2, 1, 13, "CIF: ");
                img.adText(nLinHeader + 2, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cf),""));
                img.adText(nLinHeader + 2, 45, 35, "Data: "+
                		Siruri.dtoc(Siruri.cTod(
                        		crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_DATA)))
                        		));
                
                img.adText(nLinHeader + 3, 1, 13, "Sediu: ");
                img.adText(nLinHeader + 3, 14, 28, settings.getString(context.getString(R.string.key_ecran4_adresa),""));
                img.adText(nLinHeader + 4, 1, 13, "IBAN: ");
                img.adText(nLinHeader + 4, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cont1),""));
                img.adText(nLinHeader + 5, 1, 13, "Banca: ");
                img.adText(nLinHeader + 5, 14, 25, settings.getString(context.getString(R.string.key_ecran4_banca1),""));
                img.adText(nLinHeader + 6, 1, 13, "Cap. social: ");
                img.adText(nLinHeader + 6, 14, 25, settings.getString(context.getString(R.string.key_ecran4_cap_social),"") );
                img.adText(nLinHeader + 6, 40, 40,  "Tel/Fax:"+ settings.getString(context.getString(R.string.key_ecran4_telfax),""));
                img.adText(nLinHeader + 0, nVertClient,0, "AM PRIMIT DE LA: ");
                img.adText(nLinHeader + 1, nVertClient + 2, 40, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_DENUMIRE)));
                img.adText(nLinHeader + 2, nVertClient, 13, "Nr inr. RC: ");
                img.adText(nLinHeader + 2, nVertClient + 14, 30,crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_RC)));
                img.adText(nLinHeader + 3, nVertClient, 13, "CIF: ");
                img.adText(nLinHeader + 3, nVertClient + 14, 30, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_NR_FISc)));
                img.adText(nLinHeader + 4, nVertClient, 12, "Localitate: ");
                img.adText(nLinHeader + 4, nVertClient + 14, 30, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_LOC)));
                img.adText(nLinHeader + 5, nVertClient, 13, "Adresa: ");
                img.adText(nLinHeader + 5, nVertClient + 14, 50 - 15, crs.getString(crs.getColumnIndexOrThrow(Table_Clienti.TABLE_NAME+"_"+Table_Clienti.COL_ADRESA)));
                nLinHeader = nLinHeader + 1;
                img.adText(nLinHeader + 6, 1, 120, Siruri.replicate("-",120));
                img.adText(nLinHeader + 7, nVertClient + 14,0, "Casier: ");
                img.adText(nLinHeader + 7, 1,0, "Suma de: ");

                img.adText(nLinHeader + 7, 10, 8, Siruri.str(
                		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))+
                		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_TVA))
                		,7,Biz.ConstCalcul.ZEC_VAL_CU));

                img.adText(nLinHeader + 8, 1, 8, "Adica: ");
                img.adText(nLinHeader + 9, 1,0, ConvertNumar.convert(
                		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_FARA))+
                		crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_VAL_TVA)) ));
                img.adText(nLinHeader + 10, 1,0, "Reprezentand: " + " C.V. Factura " +
                		settings.getString(context.getString(R.string.key_ecran4_serie_facturi),"")+" Nr:" 
                		+ crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_NR_DOC)));
                img.adText(nLinHeader + 11, 1, 120, Siruri.replicate(sCarLinii,120));
            	
            }
            
            
			break; // de la sqithc
		} // de la case
		return img.format();
	}

	// daca lPrint =true atunci se si listeaza pe bluet
	public static void createFisImagine (
	        Long nIdAntet, Cursor crs,int versiune,
            Context context, Boolean lPrint, String sCondens

    ) {
        String modelPrinter=PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_ecran5_model_printer), "EPSON") ;
        if (crs.getCount()>0) {
            crs.moveToFirst();
            // se imparte listarea in functie de felul imprimantei : listare ascii sau cu functiile imprimantei
            if ("SEIKO,EPSON".contains(modelPrinter)) {
                // imprimare pe model ascii
                createFisImagineAscii(nIdAntet, versiune, context, lPrint, sCondens, modelPrinter,crs);
            }
            if ("DPP-450".contains(modelPrinter)) {
                Log.d("PRO&","Ininte de listare");
                createFisImagineDPP450(nIdAntet, versiune, context, crs);
            }
            if ("DPP-350".contains(modelPrinter)) {
                Log.d("PRO&","Ininte de listare");
                createFisImagineDPP350(nIdAntet, versiune, context, crs);
            }
        }
        crs.close();
	}

    private static void createFisImagineAscii (Long nIdAntet,int versiune, Context context, Boolean lPrint, String sCondens, String modelPrinter, Cursor crs) {
        String FileName="imagine.txt";
        FileOutputStream fos;
        String[] arrimg=null;
        Bluet printer=null;
        if (lPrint) printer=new Bluet(context,"");

            // se parcurge crs si se creeaza imaginea
            // se verifica modul de plata 5 - inca pe loc cu chitanta si se listeaza varianta fact cu chitanta
            int nTipDoc=crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME+"_"+Table_Antet.COL_ID_TIPDOC));
            switch (nTipDoc) {
                case Biz.TipDoc.ID_TIPDOC_COMANDA: // comanda
                    arrimg=ListaFormat.getImageComanda(crs, versiune, context);
                    break;
                case Biz.TipDoc.ID_TIPDOC_FACTURA: // factura sau chitanta
                    arrimg=ListaFormat.getImageFactura(crs, versiune, context);
                    Log.d("CREAFIS","Array creat");
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZCLIENT: // aviz client
                    arrimg=ListaFormat.getImageAvizClient(crs, versiune, context);
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZDESC: // aviz client
                    arrimg=ListaFormat.getImageAvizDesc(crs, versiune, context);
                    Log.d("LIST","Dupa format");
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZINC: // aviz client
                    arrimg=ListaFormat.getImageAvizInc(crs, versiune, context);
                    Log.d("LIST","Dupa format:"+Biz.TipDoc.ID_TIPDOC_AVIZINC);
                    break;
                case Biz.TipDoc.ID_TIPDOC_TRANSAM: // aviz client
                    arrimg=ListaFormat.getImageTransAm(crs, versiune, context);
                    Log.d("LIST","Dupa format:"+Biz.TipDoc.ID_TIPDOC_AVIZINC);
                    break;

                default:
                    break;
            }

            if (arrimg != null) {
                try {
                    if (lPrint) {
                        printer.findBT();
                        printer.openBT();
                        // se verifica starea si nu se listeaza daca nu este deschis
                        if (printer.stare!=1) {
                            Toast.makeText(context, "Adaptorul Bluettoth nu poate fi deschis", Toast.LENGTH_SHORT).show();
                            lPrint=false;
                        }
                    }
                    fos=context.openFileOutput(FileName, Context.MODE_PRIVATE);
                    // pentru condensare
                    if (!sCondens.equals("")) {
                        fos.write(sCondens.getBytes());
                        if (lPrint) {
                            printer.sendData(sCondens+"\n");
                        }
                        //sprint=sprint+sCondens;
                    }
                    for (int i = 0; i < arrimg.length; i++) {
                        fos.write((arrimg[i]+Character.toString ((char) 13)+Character.toString ((char) 10)).getBytes());
                        if(lPrint) {
                            if (!sCondens.equals("")) {
                                if (modelPrinter.equals("SEIKO"))
                                    printer.sendData(sCondens);
                            }
                            printer.sendData(arrimg[i]+"\n"); // Character.toString ((char) 13)+Character.toString ((char) 10);
                        }
                    }
                    fos.close();
                    if (lPrint) {
//						if(printer.findBT().equals("") && printer.openBT().equals("")){
//							Log.d("BT","ininte de listare: lung:"+sprint.length() );
//							Log.d("BT","Erori listare:"+printer.sendData(sprint ));//sprint);
//							Log.d("BT","dupa de listare");
//						}
                        printer.closeBT();
                    }
                    Log.d("FISIMAG","Fisier scris");
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
    }

    private static void createFisImagineDPP350 (Long nIdAntet,int versiune, Context context,Cursor crs) {
        String ssir=ConvertNumar.convert(5567.00);
        ComunicatiiDPP_350 com = new ComunicatiiDPP_350(context);

        crs.moveToFirst();
        int nCopii = 2;
        int nPause=Math.round((6+crs.getCount()/4))*1000;
        Log.d("PRO&","1");
        if (com.getStare() == 2) {
            // se identifica tipul documentului
            Boolean lCuChit = (crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_ID_MODPL)) == 5);
            int nTipDoc = crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_ID_TIPDOC));
            switch (nTipDoc) {
                case Biz.TipDoc.ID_TIPDOC_COMANDA: // comanda
                    com.printComanda(crs);
                    break;
                case Biz.TipDoc.ID_TIPDOC_FACTURA: // factura sau chitanta
                    Log.d("PRO&", "2");
                    int nIndex=crs.getColumnIndex(Table_Antet.TABLE_NAME + "_" +Table_Antet.COL_LISTAT);
                    if (crs.getInt(nIndex)> 0) nCopii = 1;
                    try {
                        for (int i = 0; i <nCopii ; i++) {
                            // daca i>0 inseamna ca s-a facut cel putin un pas si se baga o pauza
                            if (i > 0) {
                                Thread.sleep(nPause+2000);
                            }
                            com.printFactura(crs);
                            // asteapta terminarea listarii
                            while (com.getStare() == 4) ;
                            if (lCuChit) {
                                Thread.sleep(nPause);
                                com.printChitanta(crs);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZCLIENT: // aviz client
                    com.printAviz_client(crs);
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZDESC: // aviz descarcare
                    com.printAvizDescarcare(crs);
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZINC: // aviz incarcare
                    Log.d("PRO&", "Inainte  de aviz");
                    if (crs.getCount()>30) {
                        com.printAvizIncarcare(crs, 0, 30, true, false);
                        try {
                            Thread.sleep(Math.round((4 + 30 / 4) * 1000) + 2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        com.printAvizIncarcare(crs, 30, 100, false, true);
                    } else {
                        com.printAvizIncarcare(crs, 0, 30, true, true);
                    }
                    break;
                case Biz.TipDoc.ID_TIPDOC_TRANSAM: // aviz client

                    Log.d("LIST", "Dupa format:" + Biz.TipDoc.ID_TIPDOC_AVIZINC);
                    break;

                default:
                    break;
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        com.closeActiveConnection();
    }


    private static void createFisImagineDPP450 (Long nIdAntet,int versiune, Context context,
                                                Cursor crs) {
        String ssir=ConvertNumar.convert(5567.00);
        crs.moveToFirst();
        int nCopii = 2;
        int nIndex;
//        int nPause=Math.round((4+crs.getCount()/4))*1000;
        Log.d("PRO& LISTF","1");
        ComunicatiiDPP_450 com = new ComunicatiiDPP_450(context);

            // se identifica tipul documentului
            Boolean lCuChit = (crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_ID_MODPL)) == 5);
            int nTipDoc = crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_ID_TIPDOC));
            switch (nTipDoc) {
                case Biz.TipDoc.ID_TIPDOC_COMANDA: // comanda
                    com.printComanda(crs);
                    break;
                case Biz.TipDoc.ID_TIPDOC_FACTURA: // factura sau chitanta
                    Log.d("PRO&","2");
                    nIndex=crs.getColumnIndex(Table_Antet.TABLE_NAME + "_" +Table_Antet.COL_LISTAT);
                    if (crs.getInt(nIndex)> 0) nCopii = 1;
                        for (int i = 0; i <nCopii ; i++) {
                            Log.d("PRO LISTAFORMAT"," Exemplar:"+1);
                            {
                                int nRec = crs.getCount();
                                int nPas = 0;
                                int nBloc = 15;
                                while (nPas * nBloc < nRec) {
                                    Log.d("PRO LISTAFORMAT"," Pas:"+nPas+"  Bloc:"+nBloc);
                                    if (nPas == 0) {
                                        if (nRec <= nBloc) {
                                            com.printFactura(crs, nPas * nBloc,  nBloc, true, true);
                                            Log.d("PRO LISTAFORMAT","True, True ");
                                        } else {
                                            com.printFactura(crs, nPas * nBloc,  nBloc, true, false);
                                            Log.d("PRO LISTAFORMAT","True, False ");
                                        }
                                    } else if ((nPas + 1) * nBloc >= nRec) {
                                        // suntem la ultimul pas
                                        com.printFactura(crs, nPas * nBloc,  nBloc, false, true);
                                        Log.d("PRO LISTAFORMAT","False, True ");
                                    } else {
                                        com.printFactura(crs, nPas * nBloc,  nBloc, false, false);
                                        Log.d("PRO LISTAFORMAT","False, False ");
                                    }
                                    com.putPause();

                                    try {
                                        Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    nPas = nPas + 1;
                                }
                                com.putPause();
                                if (lCuChit) {
                                    com.printChitanta(crs);
                                    com.putPause();
                                }

                            }

//                            com.printFactura(crs);
                            // asteapta terminarea listarii

                        }
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZCLIENT: // aviz client
//                    com.printAviz_client(crs)
                    nIndex=crs.getColumnIndex(Table_Antet.TABLE_NAME + "_" +Table_Antet.COL_LISTAT);
                    if (crs.getInt(nIndex)> 0) nCopii = 1;
                    for (int i = 0; i <nCopii ; i++) {
                        Log.d("PRO LISTAFORMAT AVIZCLI"," Exemplar:"+1);
                        {
                            int nRec = crs.getCount();
                            int nPas = 0;
                            int nBloc = 15;
                            while (nPas * nBloc < nRec) {
                                Log.d("PRO LISTAFORMAT AVIZCLI"," Pas:"+nPas+"  Bloc:"+nBloc);
                                if (nPas == 0) {
                                    if (nRec <= nBloc) {
                                        com.printAviz_client(crs, nPas * nBloc,  nBloc, true, true);
                                        Log.d("PRO LISTAFORMAT","True, True ");
                                    } else {
                                        com.printAviz_client(crs, nPas * nBloc,  nBloc, true, false);
                                        Log.d("PRO LISTAFORMAT","True, False ");
                                    }
                                } else if ((nPas + 1) * nBloc >= nRec) {
                                    // suntem la ultimul pas
                                    com.printAviz_client(crs, nPas * nBloc,  nBloc, false, true);
                                    Log.d("PRO LISTAFORMAT","False, True ");
                                } else {
                                    com.printAviz_client(crs, nPas * nBloc,  nBloc, false, false);
                                    Log.d("PRO LISTAFORMAT","False, False ");
                                }
                                com.putPause();

                                try {
                                    Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                nPas = nPas + 1;
                            }
                            com.putPause();

                        }

//                            com.printFactura(crs);
                        // asteapta terminarea listarii

                    }

/*                  {// varianta anterioara care nu mergea cu bluet mai nou
                    int nRec = crs.getCount();
                    int nPas = 0;
                    int nBloc = 30;
                    Log.d("PRO& LISTF","Start AVIZCL"+"Nrec:"+nRec);
                    while (nPas * nBloc < nRec) {
                        Log.d("PRO& LISTF","Npas:"+nPas);
                        if (nPas == 0) {
                            if (nRec <= nBloc) {
                                Log.d("PRO& LISTF","Start antet subsol nPas=0 si nRec<=nBloc" );
                                com.printAviz_client(crs, nPas * nBloc,  nBloc, true, true);
                            } else {
                                Log.d("PRO& LISTF","Start antet nPas=0 si nRec>nBloc" );
                                com.printAviz_client(crs, nPas * nBloc,  nBloc, true, false);
                            }
                        } else if ((nPas + 1) * nBloc >= nRec) {
                            // suntem la ultimul pas
                            Log.d("PRO& LISTF","Start subsol nPas>0 si ultimul bloc" );
                            com.printAviz_client(crs, nPas * nBloc,  nBloc, false, true);
                        } else {
                            Log.d("PRO& LISTF","Start nPas>0 si interior " );
                            com.printAviz_client(crs, nPas * nBloc,  nBloc, false, false);
                        }
                        Log.d("PRO& LISTF","Pas:"+nPas+"  Inainte de putpause AVIZCL");
                        com.putPause();

//                        try {
//                            Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                        nPas = nPas + 1;
                    }
                }*/

                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZDESC: // aviz descarcare
                    com.printAvizDescarcare(crs);
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZINC: // aviz incarcare
                    Log.d("PRO&", "Inainte  de aviz");
                {
                    int nRec = crs.getCount();
                    int nPas = 0;
                    int nBloc = 15;
                    while (nPas * nBloc < nRec) {
                        if (nPas == 0) {
                            if (nRec <= nBloc) {
                                com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, true, true);
                            } else {
                                com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, true, false);
                            }
                        } else if ((nPas + 1) * nBloc >= nRec) {
                            // suntem la ultimul pas
                            com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, false, true);
                        } else {
                            com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, false, false);
                        }
                        com.putPause();
//                        try {
//                            Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        nPas = nPas + 1;
                    }
                }
                    break;
                case Biz.TipDoc.ID_TIPDOC_TRANSAM: // aviz client

                    Log.d("LIST", "Dupa format:" + Biz.TipDoc.ID_TIPDOC_AVIZINC);
                    break;

                default:
                    break;
            }


    }

    private static void createFisImagineDPP450_old (Long nIdAntet,int versiune, Context context,Cursor crs) {
        String ssir=ConvertNumar.convert(5567.00);
        ComunicatiiDPP_450 com  = new ComunicatiiDPP_450(context);
        crs.moveToFirst();
        int nCopii = 2;
        int nIndex;
        int nPause=Math.round((4+crs.getCount()/4))*1000;
        Log.d("PRO& LISTF","1");
        if (com.getStare() == 2) {
            // se identifica tipul documentului
            Boolean lCuChit = (crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_ID_MODPL)) == 5);
            int nTipDoc = crs.getInt(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_ID_TIPDOC));
            switch (nTipDoc) {
                case Biz.TipDoc.ID_TIPDOC_COMANDA: // comanda
                    com.printComanda(crs);
                    break;
                case Biz.TipDoc.ID_TIPDOC_FACTURA: // factura sau chitanta
                    Log.d("PRO&","2");
                    nIndex=crs.getColumnIndex(Table_Antet.TABLE_NAME + "_" +Table_Antet.COL_LISTAT);
                    if (crs.getInt(nIndex)> 0) nCopii = 1;
                    for (int i = 0; i <nCopii ; i++) {
                        Log.d("PRO LISTAFORMAT"," Exemplar:"+1);
                        {
                            int nRec = crs.getCount();
                            int nPas = 0;
                            int nBloc = 15;
                            while (nPas * nBloc < nRec) {
                                Log.d("PRO LISTAFORMAT"," Pas:"+nPas+"  Bloc:"+nBloc);
                                if (nPas == 0) {
                                    if (nRec <= nBloc) {
                                        com.printFactura(crs, nPas * nBloc,  nBloc, true, true);
                                        Log.d("PRO LISTAFORMAT","True, True ");
                                    } else {
                                        com.printFactura(crs, nPas * nBloc,  nBloc, true, false);
                                        Log.d("PRO LISTAFORMAT","True, False ");
                                    }
                                } else if ((nPas + 1) * nBloc >= nRec) {
                                    // suntem la ultimul pas
                                    com.printFactura(crs, nPas * nBloc,  nBloc, false, true);
                                    Log.d("PRO LISTAFORMAT","False, True ");
                                } else {
                                    com.printFactura(crs, nPas * nBloc,  nBloc, false, false);
                                    Log.d("PRO LISTAFORMAT","False, False ");
                                }
                                com.putPause();

                                try {
                                    Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                nPas = nPas + 1;
                            }
                            com.putPause();
                            if (lCuChit) {
                                com.printChitanta(crs);
                                com.putPause();
                            }

                        }

//                            com.printFactura(crs);
                        // asteapta terminarea listarii

                    }
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZCLIENT: // aviz client
//                    com.printAviz_client(crs)
                    nIndex=crs.getColumnIndex(Table_Antet.TABLE_NAME + "_" +Table_Antet.COL_LISTAT);
                    if (crs.getInt(nIndex)> 0) nCopii = 1;
                    for (int i = 0; i <nCopii ; i++) {
                        Log.d("PRO LISTAFORMAT AVIZCLI"," Exemplar:"+1);
                        {
                            int nRec = crs.getCount();
                            int nPas = 0;
                            int nBloc = 15;
                            while (nPas * nBloc < nRec) {
                                Log.d("PRO LISTAFORMAT AVIZCLI"," Pas:"+nPas+"  Bloc:"+nBloc);
                                if (nPas == 0) {
                                    if (nRec <= nBloc) {
                                        com.printAviz_client(crs, nPas * nBloc,  nBloc, true, true);
                                        Log.d("PRO LISTAFORMAT","True, True ");
                                    } else {
                                        com.printAviz_client(crs, nPas * nBloc,  nBloc, true, false);
                                        Log.d("PRO LISTAFORMAT","True, False ");
                                    }
                                } else if ((nPas + 1) * nBloc >= nRec) {
                                    // suntem la ultimul pas
                                    com.printAviz_client(crs, nPas * nBloc,  nBloc, false, true);
                                    Log.d("PRO LISTAFORMAT","False, True ");
                                } else {
                                    com.printAviz_client(crs, nPas * nBloc,  nBloc, false, false);
                                    Log.d("PRO LISTAFORMAT","False, False ");
                                }
                                com.putPause();

                                try {
                                    Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                nPas = nPas + 1;
                            }
                            com.putPause();

                        }

//                            com.printFactura(crs);
                        // asteapta terminarea listarii

                    }

/*                  {// varianta anterioara care nu mergea cu bluet mai nou
                    int nRec = crs.getCount();
                    int nPas = 0;
                    int nBloc = 30;
                    Log.d("PRO& LISTF","Start AVIZCL"+"Nrec:"+nRec);
                    while (nPas * nBloc < nRec) {
                        Log.d("PRO& LISTF","Npas:"+nPas);
                        if (nPas == 0) {
                            if (nRec <= nBloc) {
                                Log.d("PRO& LISTF","Start antet subsol nPas=0 si nRec<=nBloc" );
                                com.printAviz_client(crs, nPas * nBloc,  nBloc, true, true);
                            } else {
                                Log.d("PRO& LISTF","Start antet nPas=0 si nRec>nBloc" );
                                com.printAviz_client(crs, nPas * nBloc,  nBloc, true, false);
                            }
                        } else if ((nPas + 1) * nBloc >= nRec) {
                            // suntem la ultimul pas
                            Log.d("PRO& LISTF","Start subsol nPas>0 si ultimul bloc" );
                            com.printAviz_client(crs, nPas * nBloc,  nBloc, false, true);
                        } else {
                            Log.d("PRO& LISTF","Start nPas>0 si interior " );
                            com.printAviz_client(crs, nPas * nBloc,  nBloc, false, false);
                        }
                        Log.d("PRO& LISTF","Pas:"+nPas+"  Inainte de putpause AVIZCL");
                        com.putPause();

//                        try {
//                            Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                        nPas = nPas + 1;
                    }
                }*/

                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZDESC: // aviz descarcare
                    com.printAvizDescarcare(crs);
                    break;
                case Biz.TipDoc.ID_TIPDOC_AVIZINC: // aviz incarcare
                    Log.d("PRO&", "Inainte  de aviz");
                {
                    int nRec = crs.getCount();
                    int nPas = 0;
                    int nBloc = 15;
                    while (nPas * nBloc < nRec) {
                        if (nPas == 0) {
                            if (nRec <= nBloc) {
                                com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, true, true);
                            } else {
                                com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, true, false);
                            }
                        } else if ((nPas + 1) * nBloc >= nRec) {
                            // suntem la ultimul pas
                            com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, false, true);
                        } else {
                            com.printAvizIncarcare(crs, nPas * nBloc,  nBloc, false, false);
                        }
                        com.putPause();
//                        try {
//                            Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        nPas = nPas + 1;
                    }
                }
                break;
                case Biz.TipDoc.ID_TIPDOC_TRANSAM: // aviz client

                    Log.d("LIST", "Dupa format:" + Biz.TipDoc.ID_TIPDOC_AVIZINC);
                    break;

                default:
                    break;
            }
        }
        com.putPause(0,false);
        com.closeActiveConnection();


    }

    // determina sirul de condensare in functie de tipul imprimantei
	// deocamdate se face un singur fel de condensare
	public static String getSirCondensare (int nTipPrinter ) {
        String condensare = "";
        condensare = condensare + Siruri.chr(0x1B) + Siruri.chr(0x40); // esc @ - initializare printer
        //sSirCondensare = sSirCondensare + Chr(0x1B) + Chr(0x43) + Chr(0x63) + Chr(0xF) ' numar de linii pe pagina ?
        // sSirCondensare = sSirCondensare + Chr(0x1B) + Chr(0x35) '' - termina modul italic
        // sSirCondensare = sSirCondensare +Chr(0x1B) + Chr(0x48) '' - termina modul doublestrike
        // Condensare = Condensare + Chr(0x1B) + "E" '' modul emphasized
        condensare = condensare + Siruri.chr(0x1B) + Siruri.chr(0xF); // modul emphasized
        condensare = condensare + Siruri.chr(0x1B) + Siruri.chr(0x30); // - avans de linie de 1/8
        condensare = condensare + Siruri.chr(0x1B) + Siruri.chr(0x43) + Siruri.chr(0x42); //'' numar de linii pe pagina (acum 66)
        return condensare;
	}
	
}
