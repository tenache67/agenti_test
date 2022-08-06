package ro.prosoftsrl.sqlserver;

import java.sql.DriverManager;

import android.os.AsyncTask;
import android.util.Log;
import net.sourceforge.jtds.jdbc.*;
@SuppressWarnings("unused")

// pentru deschidere conexiune la server sql
public class MySQLOpen extends AsyncTask<MySQLDBadapter, Void, Boolean>{
	private final static String TAGLOG="OPEN";
	@Override
	protected Boolean doInBackground(MySQLDBadapter... params) {
		// TODO Auto-generated method stub
		Boolean raspuns=false;
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
				Log.d(TAGLOG,"inainte de open pt conectare:");
				Log.d(TAGLOG,"SQL:3" +params[0].db_connect_string);
				params[0].con = DriverManager.getConnection(params[0].db_connect_string,params[0].db_userid,params[0].db_password);
				raspuns=!params[0].con.isClosed();
				Log.d(TAGLOG,"Dupa conectare:"+params[0].con.isClosed());
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				Log.d(TAGLOG,"SQL:4" +"Eroare la conectare:"+e.getMessage());
			//To	ast.makeText(MySQLDBadapter.this.context,"SQL:4" +"Eroare la conectare:"+e.getMessage(), Toast.LENGTH_LONG).show();
			}
		Log.d(TAGLOG,"Inainte de return");
		return raspuns;
		
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		//super.onPostExecute(result);
		Log.d(TAGLOG,"1 On post execute");
	}

}
