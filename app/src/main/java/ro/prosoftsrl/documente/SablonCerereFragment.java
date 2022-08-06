package ro.prosoftsrl.documente;

import java.sql.ResultSet;
import java.sql.SQLException;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Mesaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempContinutDocumente;
import ro.prosoftsrl.agenti.ActivityComunicatorInterface;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.DialogGeneralDaNu;
import ro.prosoftsrl.agenti.FragmentReceiveActionsInterface;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.Siruri;
import ro.prosoftsrl.sqlserver.MySQLDBadapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;

public class SablonCerereFragment extends BazaFragmentDocumente implements FragmentReceiveActionsInterface,
	OnItemSelectedListener 
	 {
	long _idClient;
	int id_cursa;
	long id_ruta ;
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
	String sTipDoc="";
	int nTipTva=0;
	long idAntet = 0 ; // daca este modificare atunci se vatrimite idul documentului
	
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
		this._idClient=getArguments().getLong("_id",0);
		this.idAntet=getArguments().getLong("idantet",0);
		this.id_ruta=getArguments().getLong("id_ruta",0);
		this.id_cursa=getArguments().getInt("id_cursa");
		this.colectie = new ColectieAgentHelper(getActivity());
		String sql = "SELECT * FROM " + Table_Clienti.TABLE_NAME+ 
				" WHERE _id="+this._idClient; 
		this.crsClient=this.colectie.getReadableDatabase().rawQuery(sql , null);
		this.crsClient.moveToFirst();
		final View view=inflater.inflate(R.layout.fragment_sablon_cerere, container,false);
		// schimba titlul cu denumirea clientului
		Bundle arg= new Bundle();
		arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.SCHIMBA_TITLU);
		arg.putString("TITLU",this.crsClient.getString(this.crsClient.getColumnIndexOrThrow(Table_Clienti.COL_DENUMIRE)));
		crsClient.close();
		Log.d("INCERERE","1");
		rec.transmite_actiuni(null, arg);
		Log.d("INCERERE","2");
		//buton activare alt fragment
		Button btncont = (Button) view.findViewById(R.id.btnFrgSablonContinut);
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
		Log.d("INCERERE","3");

		// buton salvare
		Button btnsalv = (Button) view.findViewById(R.id.btnFrgSablonSalveaza);
		btnsalv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				salveaza(view,false);
			}
		});
		Log.d("INCERERE","4");

// blocheaza
		Button btnbloc = (Button) view.findViewById(R.id.btnFrgSablonBlocheaza);
		btnbloc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				salveaza(view,true);
			}
		});
		Log.d("INCERERE","5");

		
		//buton renunta
		Button btnrenunta = (Button) view.findViewById(R.id.btnFrgSablonRenunta);
		btnrenunta.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				renunta(view);
			}
		});

		// la parametrul corespunzator pt idAntet se trece 0 pentru ca continutul sa se preia din tempcontinut
		crsContinut=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_SABLON, 0,0, lPVcuTVA);
		adapter = new SimpleCursorAdapter(this.context,
				Biz.getIdRowListaDenumiri(Biz.TipListaDenumiri.TLD_LINIE_SABLON,0),
				crsContinut , 
				Biz.getArrayColoaneListaDenumiri (Biz.TipListaDenumiri.TLD_LINIE_SABLON,0),
				Biz.getArrayIdViewListaDenumiri(Biz.TipListaDenumiri.TLD_LINIE_SABLON, 0),
				1);
		ListView list = (ListView) view.findViewById(R.id.lstFrgSablonContinut);		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,long rowid) {
				Cursor item = (Cursor) parent.getItemAtPosition(position);
				Long nId=item.getLong(item.getColumnIndexOrThrow("_id")); // idul este codu de produs
				// transmite idul randului la dialog
				Bundle fb=new Bundle();
				fb.putLong("id_produs",nId); // idul articolului
				fb.putBoolean("bonus", false);
				fb.putInt("tipdialog", 1);
				fb.putBoolean("diferente", true);
                fb.putLong("_id_temp",item.getLong(item.getColumnIndexOrThrow("_id_temp")));
                arataDialog(fb);
			}
		});
		// pentru header la listview
		LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View tv = vi.inflate(R.layout.row_listview_continut_sablon,null);
//		float txtsize=((TextView) view.findViewById(R.id.btnFrgSablonContinut)).getTextSize();
//		((TextView) tv.findViewById(R.id.lblRowSablonDenumire)).setTextSize(txtsize);
//		((TextView) tv.findViewById(R.id.lblRowSablonCantitate)).setTextSize(txtsize);
//		TextView tv=new TextView(context);
//		tv.setText("   Denumire                       Cant              Pret");
		list.addHeaderView(tv);
		list.setAdapter(adapter);

		//if (actiune.equals("m")) setModifica(view);
		return view;
		
	}
	@Override
	public void transmite_actiuni_la_fragment(View transview, Bundle arg) {
		int iAct=arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE);
		switch (iAct) {
			case ConstanteGlobale.Actiuni_la_documente.SET_CANTITATE_IN_LINIE:{
				crsContinut=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_SABLON, 0,0, lPVcuTVA);

				this.adapter.changeCursor(crsContinut);
				this.adapter.notifyDataSetChanged();
//				crsContinut=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_CONTINUT, idAntet,_idClient, lPVcuTVA);
//				ListView list = (ListView) this.getView().findViewById(R.id.lstFrgDocContinut);
//				SimpleCursorAdapter adapter= (SimpleCursorAdapter) list.getAdapter();
//				adapter.changeCursor(crsContinut);
//				adapter.notifyDataSetChanged();
			}
			break;

		case ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT:
			try {
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
	
	private long salveaza_local (View view,boolean lBlocheaza) {
		// se salveaza antetul
		// se pune 0 la param pt idAntet pt ca continutul sa vina din tempcontinut
		Cursor crsc=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_SABLON, 0, _idClient, lPVcuTVA);
		Log.d("SALVSABLON","Id antet="+idAntet);
		if (crsc.getCount()>0 ) {
			if (idAntet==0)
				idAntet=colectie.getNextId(colectie.getWritableDatabase());
			SQLiteDatabase db=colectie.getWritableDatabase();
			db.beginTransaction();
			db.execSQL("DELETE FROM "+Table_Sablon_Pozitii.TABLE_NAME + " where " + Table_Sablon_Pozitii.COL_ID_ANTET+"="+idAntet );
			crsc.moveToFirst();
			while (! crsc.isAfterLast()) {
				long idPoz=colectie.getNextId(colectie.getWritableDatabase());
				Bundle poz =new Bundle();
				poz.putLong(Table_Sablon_Pozitii._ID, idPoz);
				poz.putLong(Table_Sablon_Pozitii.COL_ID_ANTET, idAntet);
				poz.putLong(Table_Sablon_Pozitii.COL_ID_PRODUS, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Sablon_Pozitii.COL_ID_PRODUS)));
				poz.putDouble(Table_Sablon_Pozitii.COL_CANTITATE, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE)));
				poz.putDouble(Table_Sablon_Pozitii.COL_DIFERENTE, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_DIFERENTE)));
				poz.putDouble(Table_Sablon_Pozitii.COL_S_TIMESTAMP, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_S_TIMESTAMP)));
				ContentValues cvalpoz=Biz.getInsertPozitiiSablon(poz);
				cvalpoz.put(Table_Sablon_Pozitii.COL_C_TIMESTAMP, "A");
				Log.d("SALVEAZA","Inainte de insert pozitie:"+cvalpoz.get("_ID"));
				db.insertWithOnConflict(Table_Sablon_Pozitii.TABLE_NAME, null, cvalpoz,SQLiteDatabase.CONFLICT_REPLACE);
				Log.d("SALVEAZA","Dupa pozitie");
				cvalpoz.clear();
				crsc.moveToNext();
			}
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			long nIdAgent= Long.valueOf(pref.getString(context.getString(R.string.key_ecran1_id_agent),"0"));
			ContentValues val = new ContentValues();
			val.put(Table_Sablon_Antet._ID, idAntet);
			val.put(Table_Sablon_Antet.COL_DATA, Siruri.ttos(Siruri.getDateTime()));
			val.put(Table_Sablon_Antet.COL_ID_AGENT, nIdAgent);
			val.put(Table_Sablon_Antet.COL_ID_DEVICE, nIdAgent);
			val.put(Table_Sablon_Antet.COL_ID_PART, _idClient);
			val.put(Table_Sablon_Antet.COL_ID_RUTA ,id_ruta );
			val.put(Table_Sablon_Antet.COL_ID_CURSA ,id_cursa);
			val.put(Table_Sablon_Antet.COL_BLOCAT ,(lBlocheaza ? 1 : 0) );			
			val.put(Table_Sablon_Antet.COL_C_TIMESTAMP, "A");
			if (actiune.toUpperCase()=="A")
				db.insert(Table_Sablon_Antet.TABLE_NAME, null, val);
			else
				db.replace(Table_Sablon_Antet.TABLE_NAME, null, val);
			Log.d("SALVEAZA","Dupa antet");
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		crsc.close();
//		Bundle arg = new Bundle();
//		arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT);
//		rec.transmite_actiuni(null, arg);
		return idAntet;
		
	}

	// salveaza in server
	@SuppressLint("Range")
	private long salveaza_server (View view, boolean lBlocheaza) {
		final Context mycontext=this.context;
		final long myIdClient=this._idClient;
		final long myIdAgent=getArguments().getLong("id_agent",0);
		final long myIdRuta=this.id_ruta;
		final int myIdCursa=this.id_cursa;
		final ColectieAgentHelper colectie = new ColectieAgentHelper(mycontext);
		final SQLiteDatabase db=colectie.getWritableDatabase();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				long nIdAntet=0;
				// TODO Auto-generated method stub
				// se cauta in server existenta unei inreg coresp pt antet sablon
				String sqlCmd="SELECT "+Table_Sablon_Antet.SCOL_COD_INT+" FROM "+Table_Sablon_Antet.STABLE_NAME+
					" WHERE "+
					"SABLON_ANTET.id_part= "+myIdClient+" and "+
		    		"SABLON_ANTET.id_agent= "+myIdAgent +" and "+ 
		    		"SABLON_ANTET.id_ruta= "+myIdRuta +" and "+ 
		    		"SABLON_ANTET.id_cursa="+myIdCursa ;
				db.beginTransaction();
				db.delete(Table_Mesaje.TABLE_NAME, Table_Mesaje.COL_ID_MESAJ+"="+ConstanteGlobale.Mesaje.SUCCES_SALVEAZA_SABLON, null);
				db.setTransactionSuccessful();
				db.endTransaction();

				MySQLDBadapter sqldb= new MySQLDBadapter(mycontext);
				try {
					Log.d("PRO"," Inainte de open ");
					sqldb.open();
					Log.d("PRO"," dupa open ");
					if (! sqldb.con.isClosed()) {
						Log.d("PRO"," Cauta sablon="+sqlCmd);
						ResultSet res=sqldb.query(sqlCmd,30);
						if (res.next() ) {
							// daca exista rand inseamna ca exista sablonul
							nIdAntet=res.getLong(Table_Sablon_Antet.SCOL_COD_INT);
							res.close();
							sqlCmd="UPDATE "+Table_Sablon_Antet.STABLE_NAME +" set blocat=blocat WHERE cod_int="+nIdAntet;
							Log.d("PRO"," update sablon="+sqlCmd);
							sqldb.exec(sqlCmd,30);
						} else {
							res.close();
							// nu exista antetul si se creeaza o inreg noua
							sqlCmd="INSERT INTO genunic DEFAULT VALUES";
							sqldb.exec(sqlCmd,30);
							sqlCmd="SELECT @@IDENTITY AS nr_unic";
							res=sqldb.query(sqlCmd,30);
							if (res.next()) {
								nIdAntet=res.getLong("nr_unic");
								sqlCmd="INSERT INTO "+Table_Sablon_Antet.STABLE_NAME +" ( "+
									Table_Sablon_Antet.SCOL_COD_INT +","+
									Table_Sablon_Antet.SCOL_ID_AGENT +","+
									Table_Sablon_Antet.SCOL_ID_CURSA +","+
									Table_Sablon_Antet.SCOL_ID_DEVICE +","+
									Table_Sablon_Antet.SCOL_ID_PART +","+
									Table_Sablon_Antet.SCOL_ID_RUTA +","+
									Table_Sablon_Antet.SCOL_ID_TIPDOC +" ) VALUES ( " +
									nIdAntet+","+
									myIdAgent+","+
									myIdCursa+","+
									myIdAgent+","+
									myIdClient+","+
									myIdRuta+","+
									1+" ) ";
								Log.d("PRO"," Insert sablon="+sqlCmd);
								sqldb.exec(sqlCmd,30);
								
							}
							res.close();
						}
						// daca avem idAntet se trece mai departe
						if (nIdAntet!=0) {
							Cursor crsc=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_LINIE_SABLON, 0, _idClient, lPVcuTVA);
							crsc.moveToFirst();
							sqlCmd="";
							while (! crsc.isAfterLast()) {
								if (crsc.getLong(crsc.getColumnIndex(Table_TempContinutDocumente.COL_S_TIMESTAMP))!=0 ) {
									// daca s_timestamp are valoare inseamna ca este o inreg venita din server
									sqlCmd=sqlCmd+
										" UPDATE "+Table_Sablon_Pozitii.STABLE_NAME +" SET "+
										Table_Sablon_Pozitii.SCOL_CANTITATE+"="+
											crsc.getDouble(crsc.getColumnIndex(Table_TempContinutDocumente.COL_CANTITATE))+" , "+
										Table_Sablon_Pozitii.SCOL_DIFERENTE+"="+
												crsc.getDouble(crsc.getColumnIndex(Table_TempContinutDocumente.COL_DIFERENTE))+" , "+
										Table_Sablon_Pozitii.SCOL_ID_PRODUS+"="+
												crsc.getDouble(crsc.getColumnIndex(Table_TempContinutDocumente.COL_ID_PRODUS))+" , "+
										Table_Sablon_Pozitii.SCOL_ID_ANTET+"="+nIdAntet+
										" where "+Table_Sablon_Pozitii.SCOL_COD_INT+"="+crsc.getLong(crsc.getColumnIndex(Table_TempContinutDocumente.COL_S_TIMESTAMP))+
										" \n";
									
								} else {
									String sqlCmdUnic="INSERT INTO genunic DEFAULT VALUES";
									sqldb.exec(sqlCmdUnic,30);
									sqlCmdUnic="SELECT @@IDENTITY AS nr_unic";
									res=sqldb.query(sqlCmdUnic,30);
									res.next(); 
									long idpoz=res.getLong("nr_unic");
									res.close();
									sqlCmd=sqlCmd+
											" INSERT INTO "+Table_Sablon_Pozitii.STABLE_NAME +" ( "+
											Table_Sablon_Pozitii.SCOL_COD_INT+","+
											Table_Sablon_Pozitii.SCOL_CANTITATE+","+
											Table_Sablon_Pozitii.SCOL_DIFERENTE+","+
											Table_Sablon_Pozitii.SCOL_ID_PRODUS+","+
											Table_Sablon_Pozitii.SCOL_ID_ANTET+" ) VALUES ("+
											idpoz+","+
											crsc.getDouble(crsc.getColumnIndex(Table_TempContinutDocumente.COL_CANTITATE))+" , "+
											crsc.getDouble(crsc.getColumnIndex(Table_TempContinutDocumente.COL_DIFERENTE))+" , "+
											crsc.getDouble(crsc.getColumnIndex(Table_TempContinutDocumente.COL_ID_PRODUS))+" , "+
											nIdAntet+" ) "+
											"\n";
								}
								crsc.moveToNext();
							}
							crsc.close();
							if (! sqlCmd.equals("")) {
								Log.d("PRO"," update pozitii sablon="+sqlCmd);
								sqldb.exec(sqlCmd,30);
								db.beginTransaction();
								ContentValues cval = new ContentValues();
								cval.put(Table_Mesaje.COL_ID_MESAJ, ConstanteGlobale.Mesaje.SUCCES_SALVEAZA_SABLON);
								cval.put(Table_Mesaje.COL_VAL_INTREG, nIdAntet);
								db.insert(Table_Mesaje.TABLE_NAME, null, cval);
								db.setTransactionSuccessful();
								db.endTransaction();
							}
						} else {
							// Toast.makeText(mycontext, "Eroare la salvare ID ANTET=0 ", Toast.LENGTH_LONG).show();
						}
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//Toast.makeText(mycontext, "Eroare la salvare", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (Exception e) {
					//Toast.makeText(mycontext, "Eroare la salvare", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		};
		try {
			Thread t= new Thread(runnable);
			t.start();
			while (t.isAlive()) {
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// se verifica daca s-a primit mesajul de succes salvare
		Cursor crs=db.rawQuery("SELECT "+Table_Mesaje.COL_VAL_INTREG+" from "+Table_Mesaje.TABLE_NAME+" WHERE "+
			Table_Mesaje.COL_ID_MESAJ+"="+ConstanteGlobale.Mesaje.SUCCES_SALVEAZA_SABLON, null);
		if (crs.moveToFirst()) {
			idAntet=crs.getLong(crs.getColumnIndex(Table_Mesaje.COL_VAL_INTREG));
			Toast.makeText(mycontext, "Salvare reusita", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mycontext, "Nu s-a salvat", Toast.LENGTH_LONG).show();
			idAntet=0;
		}
		crs.close();
		db.close();
		colectie.close();
		Log.d("PRO","id antet salvat: "+idAntet);
		return idAntet;		
	}
		
	private long salveaza (View view,boolean lBlocheaza) {
		// daca este varianta de comenzi online se salveaza sablonul in server
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean lComenziOnline=settings.getBoolean(getString(R.string.key_ecran5_comenzi_online), false);
		if ( ! lComenziOnline) {
			// se salveaza in local
			idAntet= salveaza_local(view,lBlocheaza);
		} else {
			idAntet= salveaza_server(view,lBlocheaza);
		}
		inchide();
		return idAntet;
	}
	
	
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
		// parent reprezinta view container , view reprezinta optiunea din spinner
		Toast.makeText(getActivity(),"Apasat ", Toast.LENGTH_SHORT).show();
		switch (parent.getId()) {
		case R.id.lstFrgSablonContinut:
			Toast.makeText(getActivity(), "Apasat poz "+position, Toast.LENGTH_SHORT).show();
			break;
		case R.id.spnFrgDocTipDoc:
			sTipDoc=parent.getItemAtPosition(position).toString();
			nNumarDoc=Biz.getNumarCrtDoc(context, sTipDoc);
			// pentru avizele facute manual se pune nr 1
			if ((Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_AVIZDESC || 
				Biz.getIdTipDoc(sTipDoc)==Biz.TipDoc.ID_TIPDOC_AVIZINC) && actiune=="a" )
				nNumarDoc=1;
			setNrDoc(nNumarDoc);
			if (sTipDoc.equals("COMANDA")) {
				// apare butonul de incasare numai pt comanda
				EditText btnincaseaza = (EditText) getView().findViewById(R.id.btnFrgDocIncasare);
				if (!actiune.equals("m"))
					btnincaseaza.setEnabled(true);
				btnincaseaza.setVisibility(View.VISIBLE);
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
			"select "+Table_Antet.COL_NR_DOC+","+Table_Antet.COL_INCASAT+","+Table_Antet.COL_ID_TIPDOC+
			" from "+Table_Antet.TABLE_NAME+
			" where "+Table_Antet._ID+"="+idAntet, null);
		Log.d("MODIF","row:"+crstemp.getCount()+"  id:"+idAntet);
		if (crstemp.getCount()>0) {
			crstemp.moveToFirst();
			if (crstemp.getDouble(crstemp.getColumnIndexOrThrow(Table_Antet.COL_INCASAT))>0) {
				ToggleButton tog= (ToggleButton) v.findViewById(R.id.togFrgDocIncTot);
				tog.performClick();
				//v.findViewById(R.id.togFrgDocIncTot).setPressed(true);
				//v.findViewById(R.id.togFrgDocIncTot).setSelected(true);
			}
			((TextView)  v.findViewById(R.id.txtFrgDocNrDoc)).setText(crstemp.getString(crstemp.getColumnIndexOrThrow(Table_Antet.COL_NR_DOC)));
			Long idTipDoc=crstemp.getLong(crstemp.getColumnIndexOrThrow(Table_Antet.COL_ID_TIPDOC));
			Spinner spn=(Spinner) v.findViewById(R.id.spnFrgDocTipDoc);
			// trebuie sincronizat spinnerul cu tipul de doc
			if(idTipDoc==Biz.TipDoc.ID_TIPDOC_FACTURA) spn.setSelection(0);
			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_COMANDA) spn.setSelection(1);
			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_AVIZCLIENT) spn.setSelection(2);
			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_AVIZINC) spn.setSelection(0);
			else if (idTipDoc==Biz.TipDoc.ID_TIPDOC_AVIZDESC) spn.setSelection(1);
			// daca este comanda se afiseaza si caseta de incasare
			if (idTipDoc==Biz.TipDoc.ID_TIPDOC_COMANDA) {
				EditText btnincaseaza = (EditText) v.findViewById(R.id.btnFrgDocIncasare);
				btnincaseaza.setEnabled(false);
				btnincaseaza.setVisibility(View.VISIBLE);
				if (crstemp.getDouble(crstemp.getColumnIndexOrThrow(Table_Antet.COL_INCASAT))!=0)
					btnincaseaza.setText(Siruri.str(crstemp.getDouble(crstemp.getColumnIndexOrThrow(Table_Antet.COL_INCASAT)),10,2));
			}

		}
		crstemp.close();
	}
	// pentru aviz incarcare descarcare
	public void setAviz (View v) {
		v.findViewById(R.id.togFrgDocIncTot).setEnabled(false);
		Spinner spn=(Spinner) v.findViewById(R.id.spnFrgDocTipDoc);
		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(context, R.array.spnTipDocOptIncDesc, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spn.setAdapter(adapter);
		
	}
	
	private void salveaza_aviz_stoc_0 (View v) {
		// avizul este deja creat si doar se schimba idul de antet in antet si pozitii
		long nidAntet=colectie.getNextId(colectie.getWritableDatabase());
		ContentValues cval=new ContentValues();
		cval.put(Table_Antet._ID, nidAntet);
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
		//dlg.getView().findViewById(R.id.txtDlgProdContinutCant).requestFocus();
		
	}

}
