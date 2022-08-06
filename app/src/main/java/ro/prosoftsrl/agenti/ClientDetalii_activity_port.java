package ro.prosoftsrl.agenti;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import android.view.Menu;

public class ClientDetalii_activity_port extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_detalii_port);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.client_detalii_port, menu);
		return true;
	}

}
