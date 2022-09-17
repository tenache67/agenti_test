package ro.prosoftsrl.documente;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempContinutDocumente;
import ro.prosoftsrl.agenti.ActivityComunicatorInterface;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.FragmentReceiveActionsInterface;
import ro.prosoftsrl.agenti.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class DocumenteActivity extends FragmentActivity 
	implements 
		ActivityComunicatorInterface,
		ActivityReceiveActionsInterface {
	int iTLD;
	long _idClient;
	int nTipTva;
	int LAUNCH_SECOND_ACTIVITY = 1;
	//public int  ACT_ARATA_LISTA_ARTICOLE=1;
	FragmentReceiveActionsInterface setcantitate; //se transmite cantitatea la ListaProdusePtContinutFragment
	// se transmite articolul , cu cant si pret la DocumenteFragment si se transmite flagul lFaraIncasare
	FragmentReceiveActionsInterface setcontinut;
	Toast sToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_idClient = getIntent().getLongExtra("_id", 0);
		iTLD = getIntent().getIntExtra("iTLD", 0);
		setContentView(R.layout.activity_documente);
		sToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
		if (findViewById(R.id.documente_continut_fragment) != null) {
			// verifica sa nu vina din restaurare (de ex dupa rotatie ecran)
			// caz in care nu se face nimic 
			if (savedInstanceState != null) {
				return;
			}

			if (iTLD == Biz.TipListaDenumiri.TLD_SABLON_CERERE) {
				// se creeaza primul fragment de afisat in activity
				SablonCerereFragment principalFrag = new SablonCerereFragment();
				// se trimit parametrii primiti prin intent
				principalFrag.setArguments(getIntent().getExtras());
				// adauga fragmentul la activity
				getSupportFragmentManager().beginTransaction().add(R.id.documente_continut_fragment, principalFrag, "principal").commit();
			} else {
				// se creeaza primul fragment de afisat in activity
				DocumenteFragment principalFrag = new DocumenteFragment();
				// se trimit parametrii primiti prin intent
				principalFrag.setArguments(getIntent().getExtras());
				// adauga fragmentul la activity
				getSupportFragmentManager().beginTransaction().add(R.id.documente_continut_fragment, principalFrag, "principal").commit();
			}

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.documente, menu);
		return true;
	}

	@Override
	public int transmite_iTLD() {
		// TODO Auto-generated method stub
		return this.iTLD;
	}

	@Override
	public long transmite_id_client() {
		// TODO Auto-generated method stub
		return this._idClient;
	}

	@Override
	public void transmite_actiuni(View view, Bundle arg) {
		// TODO Auto-generated method stub
		// primeste de la fragmete indicatii despre ce se executa la apasarea unor butoane sau altor obiecte in fragment
		// se extrage actiunea din arg la eticheta ddata de constanta ETICHETA_ACTIUNE
		// aici se primeste si daca se incaseaza banii
		int iAct = arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE);
		switch (iAct) {
			case ConstanteGlobale.Actiuni_la_documente.NU_RELISTEAZA_DOCUMENT: {
				// daca nu se doreste continuarea relistarii documentului se inchide fereastra
				FragmentReceiveActionsInterface inchide = (FragmentReceiveActionsInterface) getSupportFragmentManager().findFragmentByTag("principal");
				inchide.transmite_actiuni_la_fragment(null, arg);
				this.finish();
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.RELISTEAZA_DOCUMENT: {
				// se transmite cererea de relistare la fragment
				FragmentReceiveActionsInterface doc = (FragmentReceiveActionsInterface) getSupportFragmentManager().findFragmentByTag("principal");
				doc.transmite_actiuni_la_fragment(null, arg);
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.FACTURA_PE_LOC_FARA_INCAS: {
				// se transmite actiunea mai departe la fragment
				setcontinut = (FragmentReceiveActionsInterface) getSupportFragmentManager().findFragmentByTag("principal");
				setcontinut.transmite_actiuni_la_fragment(null, arg); // se transmite la DocumenteFragment

				break;
			}
			case ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT: {
				Log.d("PRO", "La inchidere in docacti");
				FragmentReceiveActionsInterface inchide = (FragmentReceiveActionsInterface) getSupportFragmentManager().findFragmentByTag("principal");
				inchide.transmite_actiuni_la_fragment(null, arg);
				this.finish();
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.INCHIDE_LISTA_PRODUSE_CONTINUT: {
				//inchide lista de alegere de articole si cantitati

					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					Fragment prev = getSupportFragmentManager().findFragmentByTag("listaarticole");
					if (prev != null)
						ft.remove(prev).commit();

				getSupportFragmentManager().popBackStackImmediate("principal", FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.INCHIDE_AMBALAJE_FARA_SALVARE: {
				//inchide fragmentul de ambalaje
				{
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					Fragment prev = getSupportFragmentManager().findFragmentByTag("ambalaje");
					if (prev != null)
						ft.remove(prev).commit();
				}
				getSupportFragmentManager().popBackStackImmediate("principal", FragmentManager.POP_BACK_STACK_INCLUSIVE);
				// se transmite mai departe la principal pt a continua actiunea ( salvare sau listare )
				DocumenteFragment princ = (DocumenteFragment) getSupportFragmentManager().findFragmentByTag("principal");
				// in arg se transmite INCHIDE_AMBALAJE. transmiterea se face ctre documenteFragment
				princ.transmite_actiuni_la_fragment(view, arg);
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.INCHIDE_AMBALAJE_CU_SALVARE: {
				//inchide fragmentul de ambalaje
				{
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					Fragment prev = getSupportFragmentManager().findFragmentByTag("ambalaje");
					if (prev != null)
						ft.remove(prev).commit();
				}
				getSupportFragmentManager().popBackStackImmediate("principal", FragmentManager.POP_BACK_STACK_INCLUSIVE);
				// se transmite mai departe la principal pt a continua actiunea ( salvare sau listare )
				DocumenteFragment princ = (DocumenteFragment) getSupportFragmentManager().findFragmentByTag("principal");
				// in arg se transmite INCHIDE_AMBALAJE. transmiterea se face ctre documenteFragment
				princ.transmite_actiuni_la_fragment(view, arg);
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.ARATA_AMBALAJE: {
				// se activeaza fragmentul de ambalaje
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				Fragment prev = getSupportFragmentManager().findFragmentByTag("ambalaje");
				if (prev != null)
					ft.remove(prev);
				AmbalajeFragment frag = new AmbalajeFragment();
				// se transmit argumente mai departe
				frag.setArguments(getIntent().getExtras());
				// se retine interfata fragmentului
				ft.replace(R.id.documente_continut_fragment, frag, "listaarticole");
				ft.addToBackStack("principal");
				ft.commit();
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.ARATA_LISTA_ARTICOLE: {
				// se activeaza fragmentul de lista articole pt cautare
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				Fragment prev = getSupportFragmentManager().findFragmentByTag("listaarticole");
				if (prev != null)
					ft.remove(prev);
				ListaProdusePtContinutFragment frag = new ListaProdusePtContinutFragment();
				// se transmit argumente mai departe
				frag.setArguments(arg);
				// se retine interfata fragmentului pt a putea transmite cantitatea
				ft.replace(R.id.documente_continut_fragment, frag, "listaarticole");
				ft.addToBackStack("principal");
				ft.commit();
			}
			break;
			case ConstanteGlobale.Actiuni_la_documente.SET_CANTITATE_LA_DOCUMENT: {
				//cantitatea este stocata la eticheta "cantitate"
				setcantitate = (FragmentReceiveActionsInterface) getSupportFragmentManager().findFragmentByTag("listaarticole");
				Log.d("Inaninte de setcant", "1 " + (setcantitate == null));
				setcantitate.transmite_actiuni_la_fragment(null, arg); // se transmite la ListaProduseContinutFragment
				Log.d("dupa setcant", "3");


				// nu a mai fost necesara transmiterea la DocumenteFragment deoarece continutul se actualizeaza la onCreateView la fiecare afisare
//			setcontinut= (FragmentReceiveActionsInterface) getSupportFragmentManager().findFragmentByTag("principal");
//			setcontinut.transmite_actiuni_la_fragment(null, arg); // se transmite la DocumenteFragment
			}
			break;
			// in cazul sablonului setarea cantitati inu a mers decat aici pentru ca se apeleaza din dialogul
			// de setare cantitate si nu din cel cu lista de articole
			case ConstanteGlobale.Actiuni_la_documente.SET_CANTITATE_IN_LINIE:
				Double nCant = arg.getDouble("cantitate", 0);
				Double nDif = arg.getDouble("diferente", 0);
				Double nPret=arg.getDouble("pret_cu"); // pretul poate veni modificat fata de cel curent ( pe poz separata fata de pretul cu discoun)
				long nIdProdus = arg.getLong("id_produs");
				int nBonus = arg.getInt("este_bonus", 0);
				long _id_temp = arg.getLong("_id_temp");

				ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
				SQLiteDatabase db = colectie.getWritableDatabase();
				db.beginTransaction();
				if (nCant == -1) {
					db.delete(Table_TempContinutDocumente.TABLE_NAME, " id_produs=" + nIdProdus + " and este_bonus=" + nBonus, null);
				} else {
					ContentValues cVal = new ContentValues();
					// forma de ambalare
					cVal.put(Table_TempContinutDocumente.COL_ID_FA1, arg.getLong("forma_ambalare_selectata", 0));
					Log.d("ACTCANT", "faracant:" + arg.getBoolean("faracant", false) + "  faradif:" + arg.getBoolean("faradif", false));
					if (!arg.getBoolean("faracant", false))
						cVal.put(Table_TempContinutDocumente.COL_CANTITATE, nCant);
					if (!arg.getBoolean("farapret", false))
						cVal.put(Table_TempContinutDocumente.COL_PRET_CU, nPret);
					if (!arg.getBoolean("faradif", false))
						cVal.put(Table_TempContinutDocumente.COL_DIFERENTE, nDif);
					cVal.put(Table_TempContinutDocumente.COL_C_ESTE_BONUS, nBonus);
					cVal.put(Table_TempContinutDocumente.COL_ID_PRODUS, nIdProdus);

					db.beginTransaction();
					int rez = db.update(Table_TempContinutDocumente.TABLE_NAME, cVal, Table_TempContinutDocumente._ID + "=" + _id_temp, null);
					db.setTransactionSuccessful();
					db.endTransaction();
					Log.d("SETCANT", "Repl:" + rez + "  cant:" + nCant);
				}
//				ContentValues cVal=Biz.getUpdateContinutDocument(crstemp, nIdProdus, nCant,nBonus);			
				db.setTransactionSuccessful();
				db.endTransaction();
				db.close();
				colectie.close();
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				BazaFragmentDocumente prev = (BazaFragmentDocumente) getSupportFragmentManager().findFragmentByTag("principal");
				ft.detach(prev);
				ft.attach(prev);
				ft.replace(R.id.documente_continut_fragment, prev, "principal");
				ft.commit();

//				getSupportFragmentManager().popBackStackImmediate("principal", FragmentManager.POP_BACK_STACK_INCLUSIVE);
				// se transmite mai departe la principal pt a continua actiunea ( salvare sau listare )
//				DocumenteFragment princ = (DocumenteFragment) getSupportFragmentManager().findFragmentByTag("principal");
				// in arg se transmite INCHIDE_AMBALAJE. transmiterea se face ctre documenteFragment
				prev.transmite_actiuni_la_fragment(view, arg);




				break;
			case ConstanteGlobale.Actiuni_la_documente.SCHIMBA_TITLU:
				// se schimba titlul ferestrei cu parametrul din arg de la eticheta TITLU
				this.setTitle(arg.getString("TITLU"));
				break;
			default:
				break;
		}
	}
//	@Override
//	public void transmiteDlg(Bundle arg) {
//		// TODO Auto-generated method stub
//		setcantitate.transmite_actiuni_la_fragment(null, arg);		
//	}

	@Override
	public Bundle transmite_intent() {
		// TODO Auto-generated method stub
		return getIntent().getExtras();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
	}
}
