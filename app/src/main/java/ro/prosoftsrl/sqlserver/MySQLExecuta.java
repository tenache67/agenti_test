package ro.prosoftsrl.sqlserver;

import java.sql.SQLException;
import java.sql.Statement;

import android.os.AsyncTask;
import android.widget.Toast;

public class MySQLExecuta extends AsyncTask<MySQLDBadapter, Void, Boolean>{

	@Override
	protected Boolean doInBackground(MySQLDBadapter... params) {
		// TODO Auto-generated method stub
		String sqlSir="";
		Boolean result=true ;
		Statement stat = null ;
		try {
			if (!params[0].con.isClosed()) {
				params[0].resetLastErr();
				try {
					stat=params[0].con.createStatement();
					stat.execute(sqlSir);
					}
				 catch (Exception e ){
					result=false;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result=false;
		}
		params[0].setLastErr(!result);	
		return result;

	}

}
