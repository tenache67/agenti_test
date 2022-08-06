package ro.prosoftsrl.clienti;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.DialogGeneralDaNu;
import ro.prosoftsrl.agenti.R;

import android.content.Intent;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ClientActivity extends FragmentActivity implements ActivityReceiveActionsInterface {
	Long iIdClient;
	String actiune;
    ColectieAgentHelper colectie  ;
    int iIdRuta=0;
    long nIdAgent=0;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        colectie = new ColectieAgentHelper(this);
		setContentView(R.layout.activity_client);
		actiune=getIntent().getStringExtra("actiune");
		iIdClient=getIntent().getLongExtra("_id", 0);
        nIdAgent= Long.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(getApplicationContext().getString(R.string.key_ecran1_id_agent), "0"));

		if (iIdClient > 0) {
			Cursor crs=Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_UN_CLIENT, 0, iIdClient, false);
			if (crs.getCount()>0){
				crs.moveToFirst();
				parcurge(crs,"bind",(View) findViewById(R.id.layClientMain));
			}
            // idul rutei se retine
            crs.close();
            crs=Biz.getCursorListaDenumiri(colectie,Biz.TipListaDenumiri.TLD_CLIENT_AGENT,nIdAgent,iIdClient,false);
            if (crs.getCount()>0) {
                crs.moveToFirst();
                iIdRuta = crs.getInt(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Client_Agent.COL_ID_RUTA));
            }
            crs.close();
		}
        if (actiune.equals("s"))
            this.onBackPressed();
        int iIdDevice=Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(getString(R.string.key_ecran1_id_agent), "0"));
        Cursor crsRute=Biz.getCursorListaDenumiri(colectie,Biz.TipListaDenumiri.TLD_RUTE,0,iIdDevice,false);
        crsRute.moveToFirst();
        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item ,
                crsRute,
                new String[] {ColectieAgentHelper.Table_Rute.COL_DENUMIRE, ColectieAgentHelper.Table_Rute._ID},
                new int[] {android.R.id.text1},0 );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spn = (Spinner) findViewById(R.id.spnClientRuta);
        spn.setAdapter(adapter);
        for (int i=0 ; i<spn.getCount(); i++) {
            Cursor value=(Cursor) spn.getItemAtPosition(i);
            int id=value.getInt(value.getColumnIndex("_id"));
            if (id==iIdRuta) {
                spn.setSelection(i);
            }
        }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.clientactivity, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		FragmentTransaction ft =getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialoggeneral");
		if (prev !=null) {
			ft.remove(prev);
			ft.commit();
		}
		Bundle arg = new Bundle();
        if (actiune.equals("s")) {
            // meniu de stergere
            arg.putString("titlu", "Confirmati stergerea clientului?");
            arg.putString("text_pozitiv", "DA"); // apare primul
            arg.putString("text_negativ", "NU");
            //arg.putString("text_neutru", "Renunta la inchidere");
        } else {
            arg.putString("titlu", "Actiune la inchiderea ferestrei:");
            arg.putString("text_pozitiv", "Salveaza actualizarea"); // apare primul
            arg.putString("text_negativ", "Inchide fara salvare");
            arg.putString("text_neutru", "Renunta la inchidere");
        }
			DialogGeneralDaNu dlg = DialogGeneralDaNu.newinstance(arg);
			dlg.show(ft, "dialoggeneral");
		//super.onBackPressed();
	}
	
	// completeaza valori pt campuri
	private void bindData (Cursor crs, View view) {
		try {
			if (!view.getTag().toString().equals("")) {
				EditText edt = (EditText) view;
				edt.setText(crs.getString(crs.getColumnIndexOrThrow(view.getTag().toString())));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	// pune in cval 
	private ContentValues unBindData ( View view) {
        ContentValues cval = new ContentValues();
		try {
			if (!view.getTag().toString().equals("")) {
				Log.d("UNBD","1");
				EditText edt=(EditText) view;
				cval=(Biz.putValPtColoana(view.getTag().toString().toUpperCase(), Table_Clienti.STR_CLIENTI, edt.getText().toString()));
                Log.d("UNBD",view.getTag().toString().toUpperCase()+"="+cval.getAsString(view.getTag().toString().toUpperCase()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
        return cval;
	}
	// parcurge un viewgrup si face bind sau unbind
	private ContentValues parcurge (Cursor crs,String act,View view) {
        ContentValues cval=new ContentValues();
		if (view !=null) {
			Log.d("PARC","Id:"+view.getId());
			ViewGroup grup=null;
			try {
				grup = (ViewGroup) view;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			if (act.equals("bind")){
				// bind
				Log.d("PARC","1");
				bindData(crs,view);

			} else {
				// unbind
				cval=unBindData( view);
				Log.d("PARC","Den:"+cval.getAsString(Table_Clienti.COL_DENUMIRE));
			}
			// se incearca parcurgerea in continuare
			if (grup!=null) {
				for(int i=0; i<grup.getChildCount(); ++i) {
				    View nextChild = grup.getChildAt(i);
					Log.d("PARC","Id child:"+nextChild.getId());
				    cval.putAll(parcurge(crs, act, nextChild));
				}
			}
		}
        return cval;
	}

	// verifica daca se poate salva
	private boolean sePoateSalva () {
		boolean lRez=true;
		if (((EditText) findViewById(R.id.edtClientCodFisc)).getText().toString().equals("")) lRez=false;
		if (((EditText) findViewById(R.id.edtClientDenumire)).getText().toString().equals("")) lRez=false;
		return lRez;
	}
	
	private void salveaza (){
		ContentValues cval=new ContentValues();
		Log.d("SALV","Act:"+actiune);
		// verifica anumite cvampuri
		if (sePoateSalva() ) {
			Log.d("SALV","Se salveaza");
			cval=parcurge(null, "unbind",(View) findViewById(R.id.layClientMain));
			Log.d("SALV","Den dupa parcurg:"+cval.getAsString(Table_Clienti.COL_DENUMIRE));
            Spinner spn =(Spinner) findViewById(R.id.spnClientRuta);
            Cursor crs =(Cursor) spn.getSelectedItem();
            iIdRuta=0;
            try {
                if (crs!=null)
                    iIdRuta=crs.getInt(crs.getColumnIndex("_id"));
                Log.d("PRO","id ruta="+iIdRuta);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                iIdRuta=0;
            }
			if (actiune.equals("m")) {
				// modificare
				cval.put(Table_Clienti.COL_C_TIMESTAMP, "M");
                SQLiteDatabase db=colectie.getWritableDatabase();;
				db.beginTransaction();
                db.update(Table_Clienti.TABLE_NAME, cval, Table_Clienti._ID + "=" + iIdClient, null);
                cval.clear();
                cval.put(ColectieAgentHelper.Table_Client_Agent.COL_ID_RUTA, iIdRuta);
                cval.put(ColectieAgentHelper.Table_Client_Agent.COL_C_TIMESTAMP, actiune);
                Log.d("PRO","Upd:"+db.update(ColectieAgentHelper.Table_Client_Agent.TABLE_NAME, cval,
                        ColectieAgentHelper.Table_Client_Agent.COL_ID_CLIENT+"="+iIdClient+" and "+
                        ColectieAgentHelper.Table_Client_Agent.COL_ID_AGENT+"="+nIdAgent,null));
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            } else {
				SQLiteDatabase db=colectie.getWritableDatabase();;
				Long nextId=colectie.getNextId(db);
                iIdClient=nextId;
				cval.put(Table_Clienti._ID, nextId);
				cval.put(Table_Clienti.COL_C_TIMESTAMP, "A");
				db.beginTransaction();
				db.insert(Table_Clienti.TABLE_NAME, null, cval);
                cval.clear();
                // se adauga si pentru client_agent
                cval.put(ColectieAgentHelper.Table_Client_Agent.COL_ID_RUTA, iIdRuta);
                cval.put(ColectieAgentHelper.Table_Client_Agent.COL_ID_AGENT,nIdAgent);
                cval.put(ColectieAgentHelper.Table_Client_Agent.COL_ID_CLIENT,iIdClient);
                cval.put(ColectieAgentHelper.Table_Client_Agent.COL_C_TIMESTAMP,actiune);
                nextId=colectie.getNextId(db);
                cval.put(ColectieAgentHelper.Table_Client_Agent._ID,nextId);
                db.insert(ColectieAgentHelper.Table_Client_Agent.TABLE_NAME, null, cval);
				db.setTransactionSuccessful();
				db.endTransaction();
				db.close();
			}
		} else {
			Toast.makeText(this, "Nu se poate salva ! Denumirea sau codul fiscal trebuie completate", Toast.LENGTH_LONG).show();
		}
	}

    public void sterge () {
        ContentValues cval=new ContentValues();
        cval.put(Table_Clienti.COL_BLOCAT,1);
        cval.put(Table_Clienti.COL_C_TIMESTAMP, "M");
        SQLiteDatabase db=colectie.getWritableDatabase();;
        db.beginTransaction();
        db.update(Table_Clienti.TABLE_NAME, cval, Table_Clienti._ID+"="+iIdClient, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

	@Override
	public void transmite_actiuni(View view, Bundle arg) {
		// TODO Auto-generated method stub
		int iAct=arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE);
		switch (iAct) {
		case ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_POZITIV:
			// salveaza
			if (actiune.equals("s")) {
                sterge();
            } else {
                salveaza();
            }
            // se transmite la listadenumiriactivity pt refresh
            Intent intent=new Intent();
            setResult(1,intent);
            finish();
			break;
		case ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_NEGATIV:
			finish();
			break;
		case ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_NEUTRU:
			// nu face nimic
			break;

		default:
			break;
		}
		
	}
}
