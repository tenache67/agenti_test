package ro.prosoftsrl.sqlserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.os.AsyncTask;
import android.util.Log;

// se aduc date din server
public class MySQLQuery extends AsyncTask<MySQLDBadapter, Void, ResultSet>{
	public String sqlSir="";
	
	@Override
	protected ResultSet doInBackground(MySQLDBadapter... params) {
		// TODO Auto-generated method stub
		ResultSet res =null;
		Statement stat ;
		try {
			if (!params[0].con.isClosed()) {
                params[0].resetLastErr();

				try {
                    stat=params[0].con.createStatement();
//					Toast.makeText(context, sqlSir,Toast.LENGTH_LONG).show();
                    res= stat.executeQuery(sqlSir);
//					Toast.makeText(context, "dupa select",Toast.LENGTH_LONG).show();
				} catch (Exception e ){
					params[0].setLastErr(true);
				    Log.d("PRO&", "Erorare executeQuery:" + e.getMessage());
				}
			} else {
                Log.d("PRO&", "fara conexiune");
            }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            Log.d("PRO&", "Eroare :" + e.getMessage());
		}
        return res;
	}

}
