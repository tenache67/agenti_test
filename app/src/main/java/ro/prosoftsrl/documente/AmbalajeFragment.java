package ro.prosoftsrl.documente;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Ambalaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempPozAmbalaje;
import ro.prosoftsrl.agenti.ActivityComunicatorInterface;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.FragmentReceiveActionsInterface;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.Siruri;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AmbalajeFragment extends Fragment implements FragmentReceiveActionsInterface, OnItemSelectedListener {
	long idAntet = 0 ; // daca este modificare atunci se vatrimite idul documentului
	long idClient=0 ;
	long idAgent=0;
	ActivityComunicatorInterface act;
	ActivityReceiveActionsInterface rec ;
	ColectieAgentHelper colectie;
	Context context;
	String actiune="a";
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		this.act = (ActivityComunicatorInterface) getActivity();
		this.rec= (ActivityReceiveActionsInterface) getActivity();
		this.context=activity;
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// la initializarea fragmentului se primesc parametrii din intentul de la activity . se regasesc prin getarguments de la fragment
		// TODO Auto-generated method stu31
		//argumentele se primesc din activity
		this.actiune= getArguments().getString("actiune")!=null ? getArguments().getString("actiune"):this.actiune ; 
		this.idAntet=getArguments().getLong("idantet",0);
		this.idClient=getArguments().getLong("_id",0);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		this.idAgent= Long.valueOf(pref.getString(context.getString(R.string.key_ecran1_id_agent),"0"));
		final View view=inflater.inflate(R.layout.fragment_ambalaje, container,false);		
		final Button btnsalv = (Button) view.findViewById(R.id.btnFrgAmbSalveaza);
		btnsalv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnsalv.setEnabled(false);
				salveaza(view);
				
			}
		});
		afisAmbalaje(view);
		afisSold(view);
		return view;
	}

	// afis sold 
	private void afisSold (View view) {
		colectie = new ColectieAgentHelper(getActivity());
		SQLiteDatabase db=colectie.getReadableDatabase();
		Cursor crs=db.rawQuery(Biz.getSqlSoldAmbalajeClient(idClient, idAgent) , null);
		Log.d("PRO","Nr inreg ="+crs.getCount()+" Id client="+idClient);
		crs.moveToFirst();
		while (!crs.isAfterLast()) {
			switch (crs.getPosition()) {
			case 0 :
				afisSoldAmbalaj(view, R.id.txtFrgAmbSoldDenAmb1, crs.getString(crs.getColumnIndex(Table_Ambalaje.COL_DENUMIRE)),
						R.id.txtFrgAmbSold1, crs.getDouble(crs.getColumnIndex("cantitate")));
				break;
			case 1 :
				afisSoldAmbalaj(view, R.id.txtFrgAmbSoldDenAmb2, crs.getString(crs.getColumnIndex(Table_Ambalaje.COL_DENUMIRE)),
						R.id.txtFrgAmbSold2, crs.getDouble(crs.getColumnIndex("cantitate")));
				break;
			case 2 :
				afisSoldAmbalaj(view, R.id.txtFrgAmbSoldDenAmb3, crs.getString(crs.getColumnIndex(Table_Ambalaje.COL_DENUMIRE)),
						R.id.txtFrgAmbSold3, crs.getDouble(crs.getColumnIndex("cantitate")));
				break;
			default:
				break;
			}
			crs.moveToNext();
		}
		crs.close();		
		db.close();
		colectie.close();
	}

	private void afisSoldAmbalaj (View view,int idTxtDen,String denAmb,int idTxtCant, double nSold) {
		TextView edt =(TextView) view.findViewById(idTxtDen);
		edt.setText(denAmb);
		edt =(TextView) view.findViewById(idTxtCant);
		edt.setText(Siruri.str(nSold, 5, 0));
	}
	
	// afis ambalaje pt operatiuni
	private void afisAmbalaje (View view) {
		colectie = new ColectieAgentHelper(getActivity());
		SQLiteDatabase db=colectie.getReadableDatabase();
		Cursor crs=db.rawQuery("SELECT denumire,_id from ambalaje order by _id " , null);
		crs.moveToFirst();
		while (!crs.isAfterLast()) {
			switch (crs.getPosition()) {
			case 0 :
				afisAmbalaj(view, R.id.txtAmbDen1, crs.getString(crs.getColumnIndex("denumire")), crs.getLong(crs.getColumnIndex("_id")));
				break;
			case 1 :
				afisAmbalaj(view, R.id.txtAmbDen2, crs.getString(crs.getColumnIndex("denumire")), crs.getLong(crs.getColumnIndex("_id")));
				break;
			case 2 :
				afisAmbalaj(view, R.id.txtAmbDen3, crs.getString(crs.getColumnIndex("denumire")), crs.getLong(crs.getColumnIndex("_id")));
				break;
			default:
				break;
			}
			crs.moveToNext();
		}
		crs.close();		
		db.close();
		colectie.close();
	}
	
	// afiseaza denumirea unui ambalaj in textview
	private void afisAmbalaj (View view,int idTxt,String text,long idAmb) {
		Log.d("PRO","Set ambalaj "+text);
		TextView edt =(TextView) view.findViewById(idTxt);
		edt.setText(text);
		edt.setTag(idAmb);
	}
	
	// verifica daca s-a completat ceva la cantitati
	private boolean totEmpty (View v) {
		Log.d("PRO","Este null="+(((TextView) v.findViewById(R.id.edtAmbCantDat1)).getText().toString().equals("")));
		return 
			(((TextView) v.findViewById(R.id.edtAmbCantDat1)).getText().toString().equals(""))
			&& (((TextView) v.findViewById(R.id.edtAmbCantDat2)).getText().toString().equals(""))
			&& (((TextView) v.findViewById(R.id.edtAmbCantDat3)).getText().toString().equals(""))
			&& (((TextView) v.findViewById(R.id.edtAmbCantLuat1)).getText().toString().equals(""))
			&& (((TextView) v.findViewById(R.id.edtAmbCantLuat2)).getText().toString().equals(""))
			&& (((TextView) v.findViewById(R.id.edtAmbCantLuat3)).getText().toString().equals(""));
	}
	
	// se intoarce la fragmentul anterior
	private void salveaza(View v) {
		if (totEmpty(v)) {
			Log.d("PRO","totempty");
			Toast.makeText(context, "Nu ai completat nimic la ambalaje ! Nu se poate continua.", Toast.LENGTH_SHORT).show();
			Bundle arg= new Bundle();		
			// se transmite la documenteActivity
			arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.INCHIDE_AMBALAJE_FARA_SALVARE);
			rec.transmite_actiuni(null, arg);			
		} else {
			colectie = new ColectieAgentHelper(getActivity());
			SQLiteDatabase db=colectie.getWritableDatabase();
			db.beginTransaction();
			db.delete(Table_TempPozAmbalaje.TABLE_NAME, null, null);
			ContentValues cval=getContentLinieAmb(v,R.id.txtAmbDen1,R.id.edtAmbCantDat1,R.id.edtAmbCantLuat1);
			if (cval!=null) {
				db.insert(Table_TempPozAmbalaje.TABLE_NAME, null, cval);
			}
			cval=getContentLinieAmb(v,R.id.txtAmbDen2,R.id.edtAmbCantDat2,R.id.edtAmbCantLuat2);
			if (cval!=null) {
				db.insert(Table_TempPozAmbalaje.TABLE_NAME, null, cval);
			}
			cval=getContentLinieAmb(v,R.id.txtAmbDen3,R.id.edtAmbCantDat3,R.id.edtAmbCantLuat3);
			if (cval!=null) {
				db.insert(Table_TempPozAmbalaje.TABLE_NAME, null, cval);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			colectie.close();
			Bundle arg= new Bundle();		
			// se transmite la documenteActivity
			arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.INCHIDE_AMBALAJE_CU_SALVARE);
			rec.transmite_actiuni(null, arg);
			
		}
		
	}
	
	private ContentValues getContentLinieAmb (View v,int idTxt,int idDat,int idLuat) {
		ContentValues cval= new ContentValues();
		TextView txt = (TextView) v.findViewById(idTxt);
		long idAmb=(Long) txt.getTag();
		double nDat=Siruri.getDoubleDinString( ((EditText) v.findViewById(idDat)).getText().toString());
		double nLuat=Siruri.getDoubleDinString( ((EditText) v.findViewById(idLuat)).getText().toString());
		if ((nLuat !=0) || (nDat !=0) ) {
			cval.put(Table_TempPozAmbalaje.COL_ID_AMBALAJ, idAmb);
			cval.put(Table_TempPozAmbalaje.COL_CANTITATE_DAT, nDat);
			cval.put(Table_TempPozAmbalaje.COL_CANTITATE_LUAT, nLuat);
		} else {
			cval=null;
		}
		return cval;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transmite_actiuni_la_fragment(View view, Bundle arg) {
		// TODO Auto-generated method stub
		
	}

	
}
