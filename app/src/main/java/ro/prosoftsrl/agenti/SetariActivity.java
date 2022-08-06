package ro.prosoftsrl.agenti;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import ro.prosoftsrl.agenti.R;

public class SetariActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//		settings.edit().remove(getString(R.string.key_ecran4_pret_vanzare)).commit();
		addPreferencesFromResource(R.xml.setari_globale);
//		setContentView(R.layout.activity_setari);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setari, menu);
		return true;
	}

}
