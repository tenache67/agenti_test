package ro.prosoftsrl.agenti;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;

public interface ActivityReceiveActionsInterface  {
	// transmite actiuni de la fragment la activity
	// se implementeaza in activity

	//primeste obiectul ce agenerat actiunea si argumente pt actiune
	//obligatoriu in arg exista un int ce raspunde la arg.getstring("actiune")
		public void transmite_actiuni( View view, Bundle arg) ;

}
