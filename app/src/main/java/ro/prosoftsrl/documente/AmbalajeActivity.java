package ro.prosoftsrl.documente;

import ro.prosoftsrl.agenti.ActivityComunicatorInterface;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.R;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import android.view.View;

public class AmbalajeActivity extends FragmentActivity
	implements 
	ActivityComunicatorInterface,
	ActivityReceiveActionsInterface 
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ambalaje);
		if (findViewById(R.id.ambalaje_continut_fragment) != null) {
			// verifica sa nu vina din restaurare (de ex dupa rotatie ecran)
			// caz in care nu se face nimic 
			if ( savedInstanceState != null ) {
				return ;
			}
			// se creeaza primul fragment de afisat in activity
			AmbalajeFragment principalFrag = new AmbalajeFragment();
			// se trimit parametrii primiti prin intent
			principalFrag.setArguments(getIntent().getExtras());
			// adauga fragmentul la activity
			getSupportFragmentManager().beginTransaction().add(R.id.ambalaje_continut_fragment, principalFrag,"principal").commit();
		}
				
	}

	@Override
	public void transmite_actiuni(View view, Bundle arg) {
		int iAct=arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE);
		switch (iAct) {
		case ConstanteGlobale.Actiuni_la_documente.INCHIDE_AMBALAJE_FARA_SALVARE: {
			this.finish();
			}
			break;
		}
	}
	@Override
	public int transmite_iTLD() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long transmite_id_client() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Bundle transmite_intent() {
		// TODO Auto-generated method stub
		return null;
	}
}


