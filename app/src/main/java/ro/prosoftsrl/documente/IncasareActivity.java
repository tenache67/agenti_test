package ro.prosoftsrl.documente;

import androidx.fragment.app.FragmentActivity;
// import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ro.prosoftsrl.agenti.ActivityComunicatorInterface;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.R;

public class IncasareActivity extends FragmentActivity
        implements
        ActivityComunicatorInterface,
        ActivityReceiveActionsInterface

{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incasare);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_incasare, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int transmite_iTLD() {
        return 0;
    }

    @Override
    public long transmite_id_client() {
        return 0;
    }

    @Override
    public Bundle transmite_intent() {
        return null;
    }

    @Override
    public void transmite_actiuni(View view, Bundle arg) {

    }
}
