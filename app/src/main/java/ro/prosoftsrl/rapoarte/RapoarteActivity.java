package ro.prosoftsrl.rapoarte;

import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.DialogGeneralDaNu;
import ro.prosoftsrl.agenti.R;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class RapoarteActivity extends FragmentActivity implements ActivityReceiveActionsInterface{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rapoarte);
		if (findViewById(R.id.frame_rapoarte) != null) {
			// verifica sa nu vina din restaurare (de ex dupa rotatie ecran)
			// caz in care nu se face nimic 
			if ( savedInstanceState != null ) {
				return ;
			}
			// se creeaza primul fragment de afisat in activity
			RapoarteFragment principalFrag = new RapoarteFragment();
			// se trimit parametrii primiti prin intent
			principalFrag.setArguments(getIntent().getExtras());
			// adauga fragmentul la activity
			getSupportFragmentManager().beginTransaction().add(R.id.frame_rapoarte, principalFrag,"principal").commit();			
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rapoarte, menu);
		return true;
	}

	@Override
	public void transmite_actiuni(View view, Bundle arg) {
		// TODO Auto-generated method stub
		int iAct=arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE);
		Log.d("RAP","Actiune primita");
		switch (iAct) {
            case ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_POZITIV :
                // vine de la dialogul general la raspunsul pozitiv
                // in functi de acest id se selecteaza actiune de facut ulterior
                switch (arg.getInt(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_ID_EXPEDITOR)) {
                    case 2 : // setare data la butonul " la data" . Acelasi ca la case 1
                    case 1 : { // setare data la butonul "de la data"
                        FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
                        RapoarteFragment princ=(RapoarteFragment) getSupportFragmentManager().findFragmentByTag("principal");
                        if (princ !=null) princ.transmite_actiuni_la_fragment(null,arg);
                    }
                    break;

                }
                break;
            case ConstanteGlobale.Actiuni_la_documente.ARATA_DIALOG_GENERAL: {
                FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
                Fragment prev=getSupportFragmentManager().findFragmentByTag("dialogceredata");
                if (prev !=null) {
                    ft.remove(prev);
                    ft.commit();
                }
                DialogGeneralDaNu dlg = new DialogGeneralDaNu();
                // se transmit argumente mai departe
                dlg.setArguments(arg);
                dlg.show(ft,"dialogceredata");

            }
            break;
		case ConstanteGlobale.Actiuni_la_documente.RAPOARTE_LIST_TABELA: 
			FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
			Fragment prev=getSupportFragmentManager().findFragmentByTag("listatabela");
			if (prev !=null)
				ft.remove(prev);			
			RapoarteListTabelaFragment frag = new RapoarteListTabelaFragment();
			// se transmit argumente mai departe 
			frag.setArguments(arg);
			// se retine interfata fragmentului principal 
			ft.replace(R.id.frame_rapoarte, frag,"listatabela" );
			ft.addToBackStack("principal");
			ft.commit();
			
			break;
		}
	}
}
