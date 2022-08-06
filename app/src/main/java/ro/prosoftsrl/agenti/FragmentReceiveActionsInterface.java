package ro.prosoftsrl.agenti;

import android.os.Bundle;
import android.view.View;

public interface FragmentReceiveActionsInterface {
	// transmite actiuni de la activity la fragment
	// se implementeaza in fragment

	//primeste obiectul ce agenerat actiunea si argumente pt actiune
	//obligatoriu in arg exista un int ce raspunde la arg.getstring("actiune")
		public void transmite_actiuni_la_fragment( View view, Bundle arg) ;

}
