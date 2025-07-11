package ro.prosoftsrl.documente;
// inainte de vizualizarea unui documente tabela tempcontiunt are inregistrari numai daca 
// este pregatit pentru modificare


import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Bloc_Cursa;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_PozAmbalaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempContinutDocumente;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempPozAmbalaje;
import ro.prosoftsrl.agenti.ActivityComunicatorInterface;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.DialogGeneralDaNu;
import ro.prosoftsrl.agenti.FragmentReceiveActionsInterface;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.Siruri;
import ro.prosoftsrl.listare.ComunicatiiCasaMarcat;
import ro.prosoftsrl.listare.PrintActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DocumenteFragment extends BazaFragmentDocumente implements FragmentReceiveActionsInterface, OnItemSelectedListener {
	long _idClient;
	Context context ; 
	Cursor crsClient;
	Cursor crsContinut;
	ActivityComunicatorInterface act;
	ActivityReceiveActionsInterface rec ;
	ColectieAgentHelper colectie;
	SimpleCursorAdapter adapter ;
	String actiune="a";
	//int iTLD=Biz.TipListaDenumiri.TLD_LINIE_CONTINUT;
	Boolean lPVcuTVA ;
	int nNumarDoc =0;
	int nNumarChitanta=0 ;
	String sTipDoc="";
	int nTipTva=0;
	long idAntet = 0 ; // daca este modificare atunci se vatrimite idul documentului 
	long id_ruta=0;
	int id_cursa=0;
	boolean lCuIncasare=false; // daca este true nu se mai inreg incasarea chiar daca se listeaza factura
	boolean lIncasPeLoc=false; // se face true daca documentul este factura si se inc pe loc
	boolean lDupaAmbalaje=false;
	int actiuneFinala=0 ; // primeste valori la apelarea fragmentului de ambalaje (0 - salvare 1 - listare) si serveste pt reluarea actiunii din care s-a apelate fragmentul de ambalaje   
	boolean lCuAmbalaje=false ;
	boolean lSerieSeparataChitanta=false;
	double nProcRed; // daca este 100 se fac preturi 0
	int LAUNCH_SECOND_ACTIVITY = 1;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		this.act = (ActivityComunicatorInterface) getActivity();
		this.rec= (ActivityReceiveActionsInterface) getActivity();
		this.context=activity;
		this.lPVcuTVA=Biz.pretCuTva(context);
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// la initializarea fragmentului se primesc parametrii din intentul de la activity . se regasesc prin getarguments de la fragment
		// TODO Auto-generated method stu31
		//argumentele se primesc din activity
		this.actiune= getArguments().getString("actiune")!=null ? getArguments().getString("actiune"):this.actiune ;
		Log.d("PRO","Actiune document:"+this.actiune);
		this._idClient=getArguments().getLong("_id",0);
		this.id_ruta=getArguments().getLong("id_ruta",0);
		this.id_cursa=getArguments().getInt("id_cursa",-1);
		this.idAntet=getArguments().getLong("idantet",0);
		this.colectie = new ColectieAgentHelper(getActivity());
		String sql = "SELECT * FROM " + Table_Clienti.TABLE_NAME+ 
				" WHERE _id="+this._idClient; 
		this.crsClient=this.colectie.getReadableDatabase().rawQuery(sql , null);
		this.crsClient.moveToFirst();
		this.lCuAmbalaje=PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_ecran5_ambalaje_obligat), false);
		this.lSerieSeparataChitanta = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getString(R.string.key_ecran5_chitanta_serie_separata) , false);
		final View view=_idClient==-1 ?  inflater.inflate(R.layout.fragment_avize, container,false) : 
				inflater.inflate(R.layout.fragment_documente, container,false) ;		
		// schimba titlul cu denumirea clientului
		Bundle arg= new Bundle();		
		arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.SCHIMBA_TITLU);
		arg.putString("TITLU",this.crsClient.getString(this.crsClient.getColumnIndexOrThrow(Table_Clienti.COL_DENUMIRE)));
		this.nProcRed=this.crsClient.getDouble(this.crsClient.getColumnIndexOrThrow(Table_Clienti.COL_PROC_RED));
		Log.d("PROCRED","Proc="+this.nProcRed);
		crsClient.close();
		rec.transmite_actiuni(null, arg);
		// pentru numar document salveaza referinta 
		// pune valoarea soldului
		setSold(_idClient,view);

		//buton activare alt fragment
		Button btncont = (Button) view.findViewById(R.id.btnFrgDocListaArticole);
        //fisierul contine pe aceeasi linie cantitatea normala si cea de la bonus
		btncont.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Bundle arg = new Bundle();
				arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ARATA_LISTA_ARTICOLE);
				arg.putInt(Table_Produse.COL_COTA_TVA, nTipTva);
				rec.transmite_actiuni(v, arg); // la documenteactivity
			}
		});
		// buton salvare
		Button btnsalv = (Button) view.findViewById(R.id.btnFrgDocSalveaza);
		btnsalv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (idAntet==-2) {
					// vine de la avizul generat pentru incarcare pe drum
					salveaza_aviz_stoc_0(view);
				} else if (idAntet==-1){
					salveaza_aviz_inc_gen(view);
				}
				else {
					salveaza(view);
				}
			}
		});
		//buton listare
		Button btnlist = (Button) view.findViewById(R.id.btnFrgDocListeaza);
		btnlist.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listeaza(view);
			}
		});
		//buton renunta
		Button btnrenunta = (Button) view.findViewById(R.id.btnFrgDocRenunta);
		btnrenunta.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				renunta(view);
			}
		});
		// setarea butonului de incasare numai pt tip doc comanda se face in spinner de tip doc
		EditText btnincaseaza = (EditText) view.findViewById(R.id.btnFrgDocIncasare);
		btnincaseaza.setEnabled(false);
		btnincaseaza.setVisibility(View.INVISIBLE);
		
		// spinner pt tip document
		Spinner spndoc = (Spinner) view.findViewById(R.id.spnFrgDocTipDoc);
		spndoc.setOnItemSelectedListener(this);
		// spinner pt cota de tva
		Spinner spncota= (Spinner) view.findViewById(R.id.spnFrgDocCotaTva);
		spncota.setOnItemSelectedListener(this);
		//in cazul in care se cere incasare pe loc se apeleaza un dialog pt a sti daca s-au incasat banii
		ToggleButton togg=(ToggleButton) view.findViewById(R.id.togFrgDocIncTot);
		togg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (actiune.equals("a")) {
					lIncasPeLoc = true;
					if (isChecked ) {
						FragmentTransaction ft =getFragmentManager().beginTransaction();
						Fragment prev = getFragmentManager().findFragmentByTag("dialoggeneral");
						if (prev !=null) {
							ft.remove(prev);
							ft.commit();
							ft =getFragmentManager().beginTransaction();
						}					
						Bundle arg = new Bundle();
						arg.putString("titlu", "Se incaseaza si Banii ACUM ?");
						arg.putString("text_pozitiv", "DA");
						arg.putString("text_negativ", "NU");
						arg.putInt("actiune_pozitiv", ConstanteGlobale.Actiuni_la_documente.FACTURA_PE_LOC_FARA_INCAS);
							DialogGeneralDaNu dlg = DialogGeneralDaNu.newinstance(arg);
							dlg.show(ft, "dialoggeneral");
					} else {
						lCuIncasare=false;
					}
				}
			}
		});

		// daca nProcRed=100 ,este cu pret zero . Se face ca si cum ar fi bonus in tempcontiunt
		if (this.nProcRed==100) {
			set_fara_pret();
		}
		
		// se seteaza adapterul pt lista de continut
		// se ataseaza header la listview
		// se determina valoarea documentului 
		crsContinut=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, idAntet,_idClient, lPVcuTVA);
		
		// seteaza totalul
		setTotal(crsContinut, view);
		adapter = new SimpleCursorAdapter(this.context,
				Biz.getIdRowListaDenumiri(Biz.TipListaDenumiri.TLD_LINIE_CONTINUT,0),
				crsContinut , 
				Biz.getArrayColoaneListaDenumiri (Biz.TipListaDenumiri.TLD_LINIE_CONTINUT,0),
				Biz.getArrayIdViewListaDenumiri(Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, 0),
				1);
		ListView list = (ListView) view.findViewById(R.id.lstFrgDocContinut);
		// atasare dialog pt schimbare cantitate in linie
		if (actiune.equals("a")) {
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position,long rowid) {
					Log.d("PRO","la click 1");
					try {
                        boolean lForme=(
                            PreferenceManager.getDefaultSharedPreferences(context).getString("key_ecran5_varianta","").toUpperCase().equals("SOROLI") ||
                            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_ecran5_forme_ambalare",false) );
						Cursor item = (Cursor) parent.getItemAtPosition(position);
						Long nId=item.getLong(item.getColumnIndexOrThrow("_id")); // idul este codu de produs
                        // preia informatii din randul de continut
                        SQLiteDatabase db=colectie.getReadableDatabase();
                        String scmd="SELECT * FROM "+Table_TempContinutDocumente.TABLE_NAME+
                                " WHERE "+ Table_TempContinutDocumente._ID+"="+item.getLong(item.getColumnIndexOrThrow("_id_temp"));
                        Cursor crsLinie=db.rawQuery(scmd,null);
                        crsLinie.moveToFirst();

                        // transmite idul randului la dialog
						Bundle fb=new Bundle();
						fb.putLong("id_produs",nId); // idul articolului
						fb.putBoolean("bonus", false);
                        fb.putInt(Table_TempContinutDocumente.COL_C_ESTE_BONUS,crsLinie.
                                getInt(crsLinie.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_C_ESTE_BONUS)));
                        // se transmite _id_temp pentru a putea face update
                        fb.putLong("_id_temp",item.getLong(item.getColumnIndexOrThrow("_id_temp")));
						fb.putInt("tipdialog", 1);
						fb.putBoolean("diferente", false);
                        fb.putBoolean("forme_ambalare",lForme);
						// modif de pret este doar pt FLORISGIN si SEMROPACK
						boolean lModPret=false;
						String sCodFiscal = PreferenceManager.getDefaultSharedPreferences(context).
								getString("key_ecran4_cf","");
						if (sCodFiscal.toLowerCase().contains("27670851")) {
							// florisgin
							lModPret=true;
						} else if (sCodFiscal.toLowerCase().contains("33984123")) {
							// semrompack
							lModPret = true;
						}
						fb.putBoolean("modifica pret",lModPret);
						// aici se ia pretul din temp_continut si se trimite la dialog
						// se ia din campul pret
						fb.putDouble("pret_cu",item.getDouble(item.getColumnIndexOrThrow("pret")));
                        fb.putLong(Table_TempContinutDocumente.COL_ID_FA1,
                                crsLinie.getLong(crsLinie.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_ID_FA1)));
                        crsLinie.close();
                        db.close();
                        arataDialog(fb);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Log.d("PRO","Eroare:"+e.getMessage());
					}
				}
			});
		}
		// pentru header la listview
		LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View tv = vi.inflate(R.layout.row_listview_continut_document,null);
		float txtsize=((TextView) view.findViewById(R.id.txtFrgDocNrDoc)).getTextSize();
		((TextView) tv.findViewById(R.id.lblRowContinutDenumire)).setTextSize(txtsize) ;
		((TextView) tv.findViewById(R.id.lblRowContinutCantitate)).setTextSize(txtsize);
		((TextView) tv.findViewById(R.id.lblRowContinutPret)).setTextSize(txtsize);
        tv.setEnabled(false);
//		TextView tv=new TextView(context);
//		tv.setText("   Denumire                       Cant              Pret");
		list.addHeaderView(tv);                     
		list.setAdapter(adapter);
		
		if (_idClient==-1) setAviz(view) ; 
		else if (_idClient==-3) setTransferAmanunt(view);
		else {
			// daca nu este completat adaptorul
			setAlteDoc(view);
		}
		if (actiune.equals("m")) setModifica(view);
		
		return view;
		
	}

	@Override
	public void onResume() {
		super.onResume();


	}

	@Override
	public void transmite_actiuni_la_fragment(View transview, Bundle arg) {
		int iAct=arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE);
		switch (iAct) {
			case ConstanteGlobale.Actiuni_la_documente.SET_CANTITATE_IN_LINIE:{
				crsContinut=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, idAntet,_idClient, lPVcuTVA);

				// seteaza totalul
				setTotal(crsContinut,this.getView());
				this.adapter.changeCursor(crsContinut);
				this.adapter.notifyDataSetChanged();
//				crsContinut=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, idAntet,_idClient, lPVcuTVA);
//				ListView list = (ListView) this.getView().findViewById(R.id.lstFrgDocContinut);
//				SimpleCursorAdapter adapter= (SimpleCursorAdapter) list.getAdapter();
//				adapter.changeCursor(crsContinut);
//				adapter.notifyDataSetChanged();
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.RELISTEAZA_DOCUMENT: {
			this.listeaza(null);
			}
			break;
		case ConstanteGlobale.Actiuni_la_documente.INCHIDE_AMBALAJE_FARA_SALVARE: {
			// se pune false ca sa se faca din nou afisarea ambalajelor la salvare
			this.lDupaAmbalaje=false;
			}
			break;
		case ConstanteGlobale.Actiuni_la_documente.INCHIDE_AMBALAJE_CU_SALVARE: {
			((Button) this.getView().findViewById(R.id.btnFrgDocListeaza)).setEnabled(false);
			((Button) this.getView().findViewById(R.id.btnFrgDocSalveaza)).setEnabled(false);
			switch (this.actiuneFinala) {
			case 0:
				this.salveaza(transview);
				break;
			case 1:
				this.listeaza(transview);
				break;
			default:
				break;
			}
			
			}
			break;
		case ConstanteGlobale.Actiuni_la_documente.FACTURA_PE_LOC_FARA_INCAS: {
			lCuIncasare=true;
			}
			break;
		case ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT:
			try {
				if (this.crsClient!=null) this.crsClient.close();
				colectie.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}
	
	private void setSold(long _id, View view) {
		// daca idClient <0 vine de la doc de incarcare descarcare 
		if (_idClient>0) {
			String sql=Biz.getSqlSoldPart(_idClient);
			Cursor crsSold=this.colectie.getReadableDatabase().rawQuery(sql, null);
			double nSold=0.00;
			if (crsSold.getCount()>0) {
				crsSold.moveToFirst();
				nSold=crsSold.getDouble(crsSold.getColumnIndexOrThrow("soldc"));
			}
			crsSold.close();
			TextView lbl = (TextView) view.findViewById(R.id.lblFrgDocValSold);
			lbl.setText("Sold: "+Siruri.str(nSold, 10, 2));
		} else {
			TextView lbl = (TextView) view.findViewById(R.id.lblFrgDocValSold);
			lbl.setText("");
		}
	}
	private void setTotal(Cursor crsc, View view ) {
		TextView lbl=(TextView) view.findViewById(R.id.lblfrgDocTotal);
		lbl.setText("Total: " + Siruri.str(Biz.getTotalCuTVA(crsc, lPVcuTVA), 10, 2));
	}

	private void arataAmbalaje (View view) {
		// lDuapaAmbalaje se pune true dupa ce se iese din fragmentul cu ambalajele ca sa nu se mai apeleze inca o data fereastra de ambalaje
		// daca este activ modul cu ambalaje se activeaza fragmentul de ambalaje
		// se dezactiveaza butoanele de salvare si listare 
		Button btn=(Button) view.findViewById(R.id.btnFrgDocListeaza);
		btn.setEnabled(false);
		btn=(Button) view.findViewById(R.id.btnFrgDocSalveaza);
		btn.setEnabled(false);
		
		lDupaAmbalaje=true;
		Bundle arg= new Bundle();		
		arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ARATA_AMBALAJE);
		rec.transmite_actiuni(null, arg);
	}
	
	@SuppressLint("Range")
	private Long salveaza (View viewbaza, boolean lFaraInchidere ) {
		// se salveaza antetul
		// lFaraInchidere - nu se inchide fereastra
		View view=getView();
		Long nIdAntet=(long) 0;
		if (this.nProcRed==100) {
			set_fara_pret();
		}
		Cursor crsc=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, 0, _idClient, lPVcuTVA);
//				colectie.getReadableDatabase().
//				rawQuery(Biz.getSqlListaDenumiri(Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, 0,_idClient, lPVcuTVA), null);
		// salvare pozitii
		// in cazul facturii , daca s-a dat pe loc se intreaba daca s-au incasat banii
//		ToggleButton togg=(ToggleButton) view.findViewById(R.id.togFrgDocIncTot);
//		if (togg.isChecked()) {
////			DialogGeneralDaNu dlg =
//		}

		
		EditText edtinc = (EditText) view.findViewById(R.id.btnFrgDocIncasare);
		Double nSumaInc=Siruri.getDoubleDinString(edtinc.getText().toString());
		if (crsc.getCount()>0 || nSumaInc!=0 || lDupaAmbalaje ) {
			double nValFara=0.0;
			double nValTva=0.0;
			nIdAntet=colectie.getNextId(colectie.getWritableDatabase());
			SQLiteDatabase db=colectie.getWritableDatabase();
			db.beginTransaction();
			crsc.moveToFirst();
			while (! crsc.isAfterLast()) {
				Log.d("SALVEAZA","Inainte de get id");
				double nCant=crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE));
				if (nCant!=0) {
					long idPoz=colectie.getNextId(colectie.getWritableDatabase());
					Log.d("SALVEAZA","Dupa get id:"+idPoz);
					Bundle poz =new Bundle();
					poz.putLong(Table_Pozitii._ID, idPoz);
					poz.putLong(Table_Pozitii.COL_ID_ANTET, nIdAntet);
					poz.putLong(Table_Pozitii.COL_ID_PRODUS, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_PRODUS)));
					poz.putDouble(Table_Pozitii.COL_CANTITATE, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE)));
					poz.putDouble(Table_Pozitii.COL_COTA_TVA, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_COTA_TVA)));
					poz.putDouble(Table_Pozitii.COL_PRET_FARA, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_FARA)));
					poz.putDouble(Table_Pozitii.COL_PRET_CU, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_CU)));
					Log.d("PRO","Pret cu 1 = "+crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_CU1)));
					poz.putDouble(Table_Pozitii.COL_PRET_FARA1, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_FARA1)));
					poz.putDouble(Table_Pozitii.COL_PRET_CU1, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_CU1)));
					poz.putDouble("pret", crsc.getDouble(crsc.getColumnIndexOrThrow("pret")));
					poz.putInt("cu_tva", crsc.getInt(crsc.getColumnIndexOrThrow("cu_tva")));
					poz.putInt("este_bonus", crsc.getInt(crsc.getColumnIndexOrThrow("este_bonus")));
					poz.putInt(Table_TempContinutDocumente.COL_FARA_PRET, crsc.getInt(crsc.getColumnIndexOrThrow("fara_pret")));
                    poz.putLong(Table_Pozitii.COL_ID_FA1,crsc.getInt(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_FA1)));
                    poz.putLong(Table_Pozitii.COL_ID_FA2,crsc.getInt(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_FA2)));
                    poz.putInt(Table_Pozitii.COL_BONUS,crsc.getInt(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_C_ESTE_BONUS)));
					ContentValues cvalpoz=Biz.getInsertPozitii(poz);
					cvalpoz.put(Table_Pozitii.COL_C_TIMESTAMP, "A");
					nValFara=nValFara+cvalpoz.getAsDouble(Table_Pozitii.COL_VAL_FARA)-cvalpoz.getAsDouble(Table_Pozitii.COL_VAL_RED);
					nValTva=nValTva+cvalpoz.getAsDouble(Table_Pozitii.COL_VAL_TVA)-cvalpoz.getAsDouble(Table_Pozitii.COL_TVA_RED);
					Log.d("SALVEAZA","Inainte de insert pozitie:"+cvalpoz.get("_ID"));
					db.insertOrThrow(Table_Pozitii.TABLE_NAME, null, cvalpoz);
					Log.d("SALVEAZA","Dupa pozitie");
					cvalpoz.clear();				
				}
				crsc.moveToNext();
			}
			// salvare ambalaje daca exista 
			if (lCuAmbalaje) {
				Cursor crsAmb=db.rawQuery("SELECT * from "+Table_TempPozAmbalaje.TABLE_NAME, null);
				if (crsAmb.getCount()>0) {
					crsAmb.moveToFirst();
					while (!crsAmb.isAfterLast()) {
						long idPoz=colectie.getNextId(colectie.getWritableDatabase());
						ContentValues cvamb = new ContentValues();
						cvamb.put(Table_PozAmbalaje._ID, idPoz);
						cvamb.put(Table_PozAmbalaje.COL_CANTITATE_DAT,crsAmb.getDouble(crsAmb.getColumnIndex(Table_TempPozAmbalaje.COL_CANTITATE_DAT)));
						cvamb.put(Table_PozAmbalaje.COL_CANTITATE_LUAT,crsAmb.getDouble(crsAmb.getColumnIndex(Table_TempPozAmbalaje.COL_CANTITATE_LUAT)));
						cvamb.put(Table_PozAmbalaje.COL_ID_AMBALAJ,crsAmb.getLong(crsAmb.getColumnIndex(Table_TempPozAmbalaje.COL_ID_AMBALAJ)));
						cvamb.put(Table_PozAmbalaje.COL_ID_ANTET,nIdAntet);
						cvamb.put(Table_PozAmbalaje.COL_C_TIMESTAMP, "A");
						db.insert(Table_PozAmbalaje.TABLE_NAME, null, cvamb);
						crsAmb.moveToNext();
					}
				}
				crsAmb.close();
			}
			
			Bundle arg=new Bundle();
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			arg.putLong(Table_Antet._ID, nIdAntet);
			arg.putString(Table_Antet.COL_CORESP, "" + nNumarDoc);
			arg.putString(Table_Antet.COL_DATA,Siruri.ttos(Siruri.getDateTime()));
			long nIdAgent= Long.valueOf(pref.getString(context.getString(R.string.key_ecran1_id_agent),"0"));
			arg.putLong(Table_Antet.COL_ID_AGENT,nIdAgent);
			arg.putLong(Table_Antet.COL_ID_DEVICE,nIdAgent);
			arg.putLong(Table_Antet.COL_ID_PART,_idClient);
			//Spinner spn=(Spinner) view.findViewById(R.id.spnFrgDocTipDoc);
			arg.putLong(Table_Antet.COL_ID_TIPDOC,Biz.getIdTipDoc(sTipDoc));
			arg.putInt(Table_Antet.COL_LISTAT,0);
			arg.putString(Table_Antet.COL_NR_DOC,""+nNumarDoc);
			arg.putInt(Table_Antet.COL_TERM_PL,0);
			arg.putDouble(Table_Antet.COL_VAL_FARA,nValFara);
			arg.putDouble(Table_Antet.COL_VAL_TVA,nValTva);
			arg.putString(Table_Antet.COL_NR_CHITANTA," ");
			ToggleButton togg=(ToggleButton) view.findViewById(R.id.togFrgDocIncTot);
			if (togg.isChecked()) {
				// incas pe loc
				if (sTipDoc.equals("FACTURA")) {
					if (this.lSerieSeparataChitanta) {
						nNumarChitanta=Biz.getNumarCrtDoc(context, "CHITANTA");
					} else {
						nNumarChitanta=nNumarDoc;
					}
				}
				arg.putLong(Table_Antet.COL_ID_MODPL,5);
				if (lCuIncasare) {		
					arg.putDouble(Table_Antet.COL_INCASAT,nValFara+nValTva);
					Toast.makeText(context, "Atentie ! Trebuie sa incasezi banii !", Toast.LENGTH_LONG).show();
				}
				arg.putString(Table_Antet.COL_NR_CHITANTA,""+nNumarChitanta);
			} else {
				arg.putLong(Table_Antet.COL_ID_MODPL,0);
				if (sTipDoc.equals("COMANDA")) {
					arg.putDouble(Table_Antet.COL_INCASAT,nSumaInc);
				} else
					arg.putDouble(Table_Antet.COL_INCASAT,0);
			}
			// numai pt avize la adaugare ( ca sa nu se puna si al avizul de drum)
			if ((_idClient==-1) && (actiune.equals("a")) ) {
                if (pref.getBoolean(getString(R.string.key_ecran5_transmite_avincdesc), false)) {
                    // se marcheaza avizele salvate pt transmitere
                    arg.putString(Table_Antet.COL_CORESP, "AVIZINI");
                } else {

                    ToggleButton toggaviz = (ToggleButton) view.findViewById(R.id.togFrgDocMarfaDepozit);
// nu se mai foloseste aviz plecare in cursa
//				if (toggaviz.isChecked()) {
//					// este aviz de plecare in cursa
//					arg.putString(Table_Antet.COL_CORESP, "AVIZINI");
//				}
                }
            }
			ContentValues cval = Biz.getInsertAntet(arg);
			cval.put(Table_Antet.COL_C_TIMESTAMP, "A");
			db.insertOrThrow(Table_Antet.TABLE_NAME, null, cval);
			Log.d("SALVEAZA","Dupa antet");
			// && (this.id_ruta!=0)
			if (this.id_cursa==0 || this.id_cursa==3 ) {
				// se salveaza in blocari numai pt cursa 0
				cval.clear();
				cval.put(Table_Bloc_Cursa.COL_ID_CURSA, this.id_cursa);
				cval.put(Table_Bloc_Cursa.COL_ID_PART, this._idClient);
				cval.put(Table_Bloc_Cursa.COL_ID_RUTA, this.id_ruta);
				cval.put(Table_Bloc_Cursa.COL_DATA, Siruri.dtos(Siruri.getDateTime()));
				db.insertWithOnConflict(Table_Bloc_Cursa.TABLE_NAME, null, cval, SQLiteDatabase.CONFLICT_IGNORE);
			}
				
			db.setTransactionSuccessful();
			db.endTransaction();
			// se inscrie nr de doc curent in serii numai daca este salvare de fact,aviz cli sau comanda
			if (!((Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_AVIZDESC || 
					Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_AVIZINC) && actiune=="a") )
				{
					Biz.setNumarCrtDoc(context, sTipDoc, 0);
				}
			// daca este factura cu incas pe loc si in preferinte este trecut ca avem serie separata de chitanta se mareste si
			// seria chitantei
			if ( (Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_FACTURA)
					&& lSerieSeparataChitanta
					&& actiune=="a"
					&& lIncasPeLoc
					) {
				Biz.setNumarCrtDoc(context, "CHITANTA", 0);
			}
		}
		crsc.close();
		if (!lFaraInchidere)
			inchide();
//		Bundle arg = new Bundle();
//		arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT);
//		rec.transmite_actiuni(null, arg);
		return nIdAntet;
	}

    // verifica daca exista pozitii cu bonus
    private void are_bonus (View view) {

    }

	private void salveaza (View view) {
        Cursor crsc=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, 0, _idClient, lPVcuTVA);
        crsc.moveToFirst();
        double nSuma=0;
        boolean lCont=true;
        String cMesaj="";
        while (! crsc.isAfterLast()) {
            nSuma=nSuma+crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE));
            Log.d("PROS","PF:"+crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_FARA))+
                    " pcu:"+crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_CU))+
                    " cant:"+crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_CANTITATE)));
            if (crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_C_ESTE_BONUS))==1)
                {
                    if (Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_BONFISC) {
                        lCont=false;
                        cMesaj="Nu se accepta bonusuri pe bon fiscal";
                    }
                }

            crsc.moveToNext();
        }

	        if (lCont) {
			Log.d("PROS", " " + Biz.getIdTipDoc(sTipDoc));
			if (nSuma == 0) {
				lCont = false;
				cMesaj = "Nu poate fi inchis fara cantitati";
				if ((Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_COMANDA)) {
					lCont = true;
				}
				if (Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_AVIZDESC) {
					lCont = true;
				}
				if (Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_AVIZINC) {
					lCont = true;

				}
			}
		}
        crsc.close();
        if (lCont) {
            if (lCuAmbalaje && !lDupaAmbalaje) {
                this.actiuneFinala = 0;
                this.arataAmbalaje(view);
            } else {
                salveaza(view, false);
            }
        }
        else {
            Toast.makeText(getActivity(), cMesaj, Toast.LENGTH_LONG).show();
        }
	}

/*
    private void listeaza (View viewbaza) {
        String modelPrinter=PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_ecran5_model_printer), "EPSON") ;
        if ("DPP-450".contains(modelPrinter)) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialoggeneral");
            if (prev != null) {
                ft.remove(prev);
                ft.commit();
                ft = getFragmentManager().beginTransaction();
            }
            Bundle arg = new Bundle();
            arg.putString("titlu", "Listare docmunet");
            arg.putString("text_pozitiv", "DA");
            arg.putString("text_negativ", "NU");
            //arg.putInt("actiune_pozitiv", ConstanteGlobale.Actiuni_la_documente.RELISTEAZA_DOCUMENT);
            //arg.putInt("actiune_negativ", ConstanteGlobale.Actiuni_la_documente.NU_RELISTEAZA_DOCUMENT);
            DialogGeneralDaNu dlg = DialogGeneralDaNu.newinstance(arg);
            dlg.show(ft, "dialoggeneral");
        } else {
            listeaza_document(viewbaza);
        }

    }
*/

	private void listeaza(View viewbaza) {
		View view=getView();
		Log.d("LISTARE","actiune="+actiune);
		int nListat=0;
		boolean lCont = true;
		// in plus
		Button btnlist = (Button) view.findViewById(R.id.btnFrgDocListeaza);
		btnlist.setEnabled(false);
		//
		if (!actiune.equals("m")) {
//---------------
			Cursor crsc = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, 0, _idClient, lPVcuTVA);
			crsc.moveToFirst();
			double nSuma = 0;
			String cMesaj = "";
			while (!crsc.isAfterLast()) {
				nSuma = nSuma + crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE));
				Log.d("PROS", "PF:" + crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_FARA)) +
						" pcu:" + crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_CU)) +
						" cant:" + crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_CANTITATE)));
				if (crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_C_ESTE_BONUS)) == 1) {
					if (Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_BONFISC) {
						lCont = false;
						cMesaj = "Nu se accepta bonusuri pe bon fiscal";
					}
				}

				crsc.moveToNext();
			}
			crsc.close();

			if (lCont) {
				Log.d("PROS", " " + Biz.getIdTipDoc(sTipDoc));
				if (nSuma == 0) {
					lCont = false;
					cMesaj = "Nu poate fi inchis fara cantitati";
					if ((Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_COMANDA)) {
						lCont = true;
					}
					if (Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_AVIZDESC) {
						lCont = true;
					}
					if (Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_AVIZINC) {
						lCont = true;

					}
				}
			}

//-------------------------


			if (lCont) {
				if (lCuAmbalaje && !lDupaAmbalaje) {
					this.actiuneFinala = 1;
					this.arataAmbalaje(view);
					// aici era return
					lCont=false;
				} else {
					idAntet = this.salveaza(view, true); // nu se inchide
				}
			} else {
					Toast.makeText(getActivity(), cMesaj, Toast.LENGTH_LONG).show();
				// aici era return
					lCont=false ;
				}

		}

		if (lCont) {

			if (idAntet != 0) {
				Log.d("LIST", "" + idAntet);
				if (Biz.getIdTipDoc(sTipDoc) == Biz.TipDoc.ID_TIPDOC_BONFISC) {
					//pentru casa de marcat

// varianta veche ( inainte de wp50)

//					SQLiteDatabase db = colectie.getWritableDatabase();
//					if (ComunicatiiCasaMarcat.createFisImagine(idAntet, db, ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY,
//							context, true, ConstanteGlobale.Tipuri_case_marcat.DATECS_DP_05) == 0) {
//						nListat = 1;
//					} else {
//
//						actiune = "m"; // previne salvarea inca o data
//						FragmentTransaction ft = getFragmentManager().beginTransaction();
//						Fragment prev = getFragmentManager().findFragmentByTag("dialoggeneral");
//						if (prev != null) {
//							ft.remove(prev);
//							ft.commit();
//							ft = getFragmentManager().beginTransaction();
//							Log.d("PRO", "1");
//						}
//						Bundle arg = new Bundle();
//						arg.putString("titlu", "ATENTIE ! Nu a iesit bonul fiscal la casa de marcat. Incercati din nou ?");
//						arg.putString("text_pozitiv", "DA");
//						arg.putString("text_negativ", "NU");
//						arg.putInt("actiune_pozitiv", ConstanteGlobale.Actiuni_la_documente.RELISTEAZA_DOCUMENT);
//						arg.putInt("actiune_negativ", ConstanteGlobale.Actiuni_la_documente.NU_RELISTEAZA_DOCUMENT);
//						DialogGeneralDaNu dlg = DialogGeneralDaNu.newinstance(arg);
//						Log.d("PRO", "2");
//						dlg.show(ft, "dialoggeneral");
//					}
//					db.close();
				} else {

//					ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
//							new ActivityResultContracts.StartActivityForResult(),
//							result -> {
//								if (result.getResultCode() == LAUNCH_SECOND_ACTIVITY) {
//									// ToDo : Do your stuff...
//								}
//							});

					Intent i = new Intent(this.getContext(),PrintActivity.class);
					Bundle arg = new Bundle();
					arg.putLong("IdAntet",idAntet);
					i.putExtras(arg);
					this.startActivityForResult(i,LAUNCH_SECOND_ACTIVITY);
					nListat = 1;
				}
			}
			// idAntet pentru documente soeciale
			// -1 gen aviz inc
			// -2 gen aviz stoc
			// -3 gen transfer am
			// se inchide dupa listare numai daca este listat si document obisnuit sau transfer amanunat
		}
		btnlist.setEnabled(true);

	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("ONACT","La final print");
		if (requestCode == LAUNCH_SECOND_ACTIVITY) {
//			if(resultCode == Activity.RESULT_OK){
				int nListat=1;
				idAntet=data.getLongExtra("IdAntet",0);
				if (nListat >= 1 && (idAntet > 0 || idAntet == -3)) {
					// se salveaza daca este listat in baza de date
					SQLiteDatabase db = colectie.getWritableDatabase();
					db.beginTransaction();
					db.execSQL("UPDATE " + Table_Antet.TABLE_NAME + " SET " + Table_Antet.COL_LISTAT + "=" + nListat);
					db.setTransactionSuccessful();
					db.endTransaction();
					Log.d("PRO", "Inainte de inchide");
					db.close();
					inchide();
				}
//			}
//			if (resultCode == Activity.RESULT_CANCELED) {
//				// Write your code if there's no result
//			}
		}
	} //onActivityResult


	private void renunta(View view) {
		// dialogul primeste in bundle informatia despre id_produs 
		if (!actiune.equals("m")) {
			FragmentTransaction ft =getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag("dialoggeneral");
			if (prev !=null) {
				ft.remove(prev);
				ft.commit();
			}
			Bundle arg = new Bundle();
			arg.putString("titlu", "Sigur inchizi macheta de adaugare document si renunti la datele introduse pana acum ?");
			arg.putString("text_pozitiv", "DA");
			arg.putString("text_negativ", "NU");
			arg.putInt("actiune_pozitiv", ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT);
				DialogGeneralDaNu dlg = DialogGeneralDaNu.newinstance(arg);
				dlg.show(ft, "dialoggeneral");
		} else {
			inchide();
//			Bundle arg = new Bundle();
//			arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT);
//			rec.transmite_actiuni(null, arg);
		}

		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		try {
			crsClient.close();
			crsContinut.close();
			colectie.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position ,long arg3) {
		// aici apare un blocaj pe comanda cu parola
		// parent reprezinta view container , view reprezinta optiunea din spinner
		switch (parent.getId()) {
		case R.id.spnFrgDocTipDoc:
			sTipDoc=parent.getItemAtPosition(position).toString();
			nNumarDoc=Biz.getNumarCrtDoc(context, sTipDoc);

			if (sTipDoc.equals("FACTURA") || sTipDoc.equals("AVIZ CLIENT")) {
				((Button) this.getView().findViewById(R.id.btnFrgDocSalveaza)).setEnabled(false);
			} else {
				((Button) this.getView().findViewById(R.id.btnFrgDocSalveaza)).setEnabled(true);
			}


			if (sTipDoc.equals("FACTURA")) {
				nNumarChitanta = nNumarDoc;
			}

			// pentru avizele facute manual se pune nr 1
			if ((Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_AVIZDESC || 
				Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_AVIZINC) && actiune.equals("a") )
				nNumarDoc=1;
			setNrDoc(nNumarDoc);
			if (sTipDoc.equals("COMANDA")) {
				// apare butonul de incasare numai pt comanda
				EditText btnincaseaza = (EditText) getView().findViewById(R.id.btnFrgDocIncasare);
				if (!actiune.equals("m")) {
					btnincaseaza.setEnabled(true);
					// se blochaza butonul de continut
					Button btncont = (Button) this.getView().findViewById(R.id.btnFrgDocListaArticole);
					if (btncont != null) {
						if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_ecran5_fara_comanda",false)) {
							btncont.setEnabled(true);
						} else {
							// daca este comanda blocata se sterge continutul si se blocheaza butonul de act continut
							SQLiteDatabase db = colectie.getWritableDatabase();
							db.beginTransaction();
							db.delete(Table_TempContinutDocumente.TABLE_NAME, " 1=1", null);
							db.setTransactionSuccessful();
							db.endTransaction();
							btncont.setEnabled(false);
							Toast.makeText(context, "Atentie ! Comanda se foloseste doar pentru incasare sau reglare ambalaje !", Toast.LENGTH_SHORT).show();
						}
					}

				}
				btnincaseaza.setVisibility(View.VISIBLE);
			} else {
				Button btncont = (Button) this.getView().findViewById(R.id.btnFrgDocListaArticole);
				if (btncont != null)
					btncont.setEnabled(true);



			}

			break;
		case R.id.spnFrgDocCotaTva:
			nTipTva=position;			
			break;
		default:
			break;
		}
		
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	public void setNrDoc (int nNumarDoc) {
		if (!actiune.equals("m")) {
		TextView txt = (TextView) getView().findViewById(R.id.txtFrgDocNrDoc);
		txt.setText("Nr. doc: "+nNumarDoc);
		}
	}
	
	// dezactiveaza anumite controale pentru m,odificare 
	public void setModifica ( View v) {
		
		// dezactiveaza butoane
		v.findViewById(R.id.btnFrgDocListaArticole).setEnabled(false);
		// in cazul in care idAntet =-2 este aviz de stoc 0 generat si se permite salvarea
		if (! ((idAntet==-2) || (idAntet==-1)))
			v.findViewById(R.id.btnFrgDocSalveaza).setEnabled(false);
		
		v.findViewById(R.id.togFrgDocIncTot).setEnabled(false);
		
		Cursor crstemp = colectie.getReadableDatabase().rawQuery(
			"select "+Table_Antet.COL_NR_DOC+","+Table_Antet.COL_INCASAT+","+Table_Antet.COL_ID_TIPDOC+","+
					Table_Antet.COL_LISTAT+","+Table_Antet.COL_CORESP+
			" from "+Table_Antet.TABLE_NAME+
			" where "+Table_Antet._ID+"="+idAntet, null);
		Log.d("MODIF","row:"+crstemp.getCount()+"  id:"+idAntet);
		if (crstemp.getCount()>0) {
			crstemp.moveToFirst();
			//la avize se sincronizeaza toggle cu val din coresp
			if (_idClient==-1) {
				if (crstemp.getString(crstemp.getColumnIndexOrThrow(Table_Antet.COL_CORESP)).equals("AVIZINI")
					|| crstemp.getString(crstemp.getColumnIndexOrThrow(Table_Antet.COL_CORESP)).trim().equals("A")) {
					ToggleButton tgb=(ToggleButton) v.findViewById(R.id.togFrgDocMarfaDepozit);
					tgb.setChecked(true);
					tgb.setEnabled(false);
				}
			}
				 
				
			int idTipDoc=crstemp.getInt(crstemp.getColumnIndexOrThrow(Table_Antet.COL_ID_TIPDOC));
			// daca este bon fiscal si s-a listat se blocheaza listarea
			if (idTipDoc==Biz.TipDoc.ID_TIPDOC_BONFISC) {
				if (crstemp.getDouble(crstemp.getColumnIndexOrThrow(Table_Antet.COL_LISTAT))==1) {
					Button list= (Button) v.findViewById(R.id.btnFrgDocListeaza);
					list.setEnabled(false);
				}
			}
			if (crstemp.getDouble(crstemp.getColumnIndexOrThrow(Table_Antet.COL_INCASAT))>0) {
				ToggleButton tog= (ToggleButton) v.findViewById(R.id.togFrgDocIncTot);
				tog.performClick();
				//v.findViewById(R.id.togFrgDocIncTot).setPressed(true);
				//v.findViewById(R.id.togFrgDocIncTot).setSelected(true);
			}
			((TextView)  v.findViewById(R.id.txtFrgDocNrDoc)).setText(crstemp.getString(crstemp.getColumnIndexOrThrow(Table_Antet.COL_NR_DOC)));
			Log.d("PRO","id tipdoc din antet="+idTipDoc);
			Spinner spn=(Spinner) v.findViewById(R.id.spnFrgDocTipDoc);
			ArrayAdapter adapter=(ArrayAdapter) spn.getAdapter();
			for(int i = 0; i < adapter.getCount(); i++) {
//					 Log.d("PRO","Set tip doc in modifica: id poz="+i+" tip="+Biz.TipDoc.DEN_TIPDOC[idTipDoc]);
					if (Biz.getIdTipDoc(adapter.getItem(i).toString())==idTipDoc) {
		            	Log.d("PRO","Set tip doc in modifica: id poz gasit="+i);
		                spn.setSelection(i);  						
					}
//		            if(adapter.getItem(i).equals(Biz.TipDoc.DEN_TIPDOC[idTipDoc])) {
//		            	Log.d("PRO","Set tip doc in modifica: id poz gasit="+i);
//		                spn.setSelection(i);  
//		            }
		      } 
			// trebuie sincronizat spinnerul cu tipul de doc
//			if (idTipDoc==Biz.TipDoc.ID_TIPDOC_BONFISC) spn.setSelection(0);
//			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_FACTURA) spn.setSelection(1);
//			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_COMANDA) spn.setSelection(2);
//			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_AVIZCLIENT) spn.setSelection(3);
//			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_AVIZINC) spn.setSelection(0);
//			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_AVIZDESC) spn.setSelection(1);
//			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_TRANSAM) spn.setSelection(0);
			
			
			spn.setEnabled(false);
			// daca este comanda se afiseaza si caseta de incasare
			if (idTipDoc==Biz.TipDoc.ID_TIPDOC_COMANDA) {
				EditText btnincaseaza = (EditText) v.findViewById(R.id.btnFrgDocIncasare);
				btnincaseaza.setEnabled(false);
				btnincaseaza.setVisibility(View.VISIBLE);
				if (crstemp.getDouble(crstemp.getColumnIndexOrThrow(Table_Antet.COL_INCASAT))!=0) {
					Log.d("PRO","setincas");
					btnincaseaza.setText(Siruri.str(crstemp.getDouble(crstemp.getColumnIndexOrThrow(Table_Antet.COL_INCASAT)),10,2));
				} else {
					Log.d("PRO","setgol");
					btnincaseaza.setText(" ");
				}
			}

		}
		crstemp.close();
		
	}
	
	// set optiuni alte documente
	public void setAlteDoc (View v) {
		// daca este completata casa de marcat se pune arrayul cu bon fiscal
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String casaAdaptor=settings.getString(getString(R.string.key_ecran3_bluetcasamarcat), "");
		if (casaAdaptor.equals("")) {
			Spinner spn=(Spinner) v.findViewById(R.id.spnFrgDocTipDoc);
			ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(context, R.array.spnTipDocOptFaraBf, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spn.setAdapter(adapter);
		}
	}
	
	// pentru aviz incarcare descarcare
	public void setAviz (View v) {
		v.findViewById(R.id.togFrgDocIncTot).setEnabled(false);
		Spinner spn=(Spinner) v.findViewById(R.id.spnFrgDocTipDoc);
		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(context, R.array.spnTipDocOptIncDesc, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spn.setAdapter(adapter);
		
	}
	
	// pentru aviz amanunt
	public void setTransferAmanunt (View v) {
		v.findViewById(R.id.togFrgDocIncTot).setEnabled(false);
		Spinner spn=(Spinner) v.findViewById(R.id.spnFrgDocTipDoc);
		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(context, R.array.spnTipDocOptTransAmanunt, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spn.setAdapter(adapter);
		
	}

	
	private void salveaza_aviz_stoc_0 (View v) {
		// avizul este deja creat si doar se schimba idul de antet in antet si pozitii
		long nidAntet=colectie.getNextId(colectie.getWritableDatabase());
		ContentValues cval=new ContentValues();
		cval.put(Table_Antet._ID, nidAntet);
		cval.put(Table_Antet.COL_CORESP, "STOC0");
		colectie.getWritableDatabase().update(Table_Antet.TABLE_NAME, cval, Table_Antet._ID+"=-2",null);
		cval.clear();
		cval.put(Table_Pozitii.COL_ID_ANTET, nidAntet);
		colectie.getWritableDatabase().update(Table_Pozitii.TABLE_NAME, cval, Table_Pozitii.COL_ID_ANTET+"=-2",null);
		cval.clear();
		cval.put(Table_Antet.COL_BLOCAT,1);
		colectie.getWritableDatabase().update(Table_Antet.TABLE_NAME, cval, null,null);
		Biz.setNumarCrtDoc(context, "AVIZ DESC", 0);
		inchide();
	}
	
	private void salveaza_aviz_inc_gen(View v) {
		// mareste numarul curent de aviz si nu se face nici o salvare
		Biz.setNumarCrtDoc(context, "AVIZ INC", 0);
		inchide();
	}

	// transmite actiunea de inchidere a documetului
	private void inchide() {
		Bundle arg = new Bundle();
		arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT);
		rec.transmite_actiuni(null, arg);
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		// daca idul este -1 sau -2 se sterge din baza de date
		if (idAntet==-1) {
			colectie.getWritableDatabase().delete(Table_Antet.TABLE_NAME, Table_Antet._ID+" =-1", null);
			colectie.getWritableDatabase().delete(Table_Pozitii.TABLE_NAME, Table_Pozitii.COL_ID_ANTET+" =-1", null);
		} else if(idAntet==-2) {
			colectie.getWritableDatabase().delete(Table_Antet.TABLE_NAME, Table_Antet._ID+" =-2", null);
			colectie.getWritableDatabase().delete(Table_Pozitii.TABLE_NAME, Table_Pozitii.COL_ID_ANTET+" =-2", null);
		}
		
		colectie.close();
		super.onDestroyView();
	}
	public void arataDialog ( Bundle arg) {
		// dialogul primeste in bundle informatia despre id_produs 
		FragmentTransaction ft =getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("listdialog");
		if (prev !=null) {
			ft.remove(prev);
			ft.commit();
		}
		DialogContinutProdus dlg = DialogContinutProdus.newinstance(arg);
		dlg.show(ft, "listdialog");
	}
	public void set_fara_pret () {
		SQLiteDatabase db=colectie.getWritableDatabase();
		db.beginTransaction();
		db.execSQL("UPDATE "+Table_TempContinutDocumente.TABLE_NAME+" SET "+Table_TempContinutDocumente.COL_FARA_PRET+" =1 ");
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		
	}
}
