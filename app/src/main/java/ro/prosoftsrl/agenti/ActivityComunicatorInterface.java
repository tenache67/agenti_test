package ro.prosoftsrl.agenti;

import android.os.Bundle;

public interface ActivityComunicatorInterface {
	// transmite date de la fragment la activity
	// se implementeaza in activity
		public int transmite_iTLD ();
		public long transmite_id_client () ; //pt id client
		//public int transmite_id_tva (); // transmite id tva pt alegere lista produse
		// transmite datele primite de activity in intent
		public Bundle transmite_intent();
}
