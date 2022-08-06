package ro.prosoftsrl.sqlserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ro.prosoftsrl.agenti.R;
import net.sourceforge.jtds.jdbc.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView.FindListener;
import android.widget.Toast;

@SuppressWarnings("unused")
public class MySQLDBadapter {
	private Context context ;
	public Connection con =null;
	public boolean err =false;
	public String db_connect_string,db_userid,db_password ;
	private static final String TAGLOG = "SQLADAPTER";
	
	
	public MySQLDBadapter(Context context,String db_connect_string,String db_userid,
			String db_password) {
		try {
			this.close();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			Log.d(TAGLOG,e.getMessage());
		}
		this.context=context;
		this.db_connect_string=db_connect_string;
		this.putSqlDriver();
		this.db_password="pro" ;//db_password;
		this.db_userid="sa";  //db_userid;
		Log.d(TAGLOG,"Final init sqladapter");
	}

	public MySQLDBadapter (Context context) {
		// se cauta sirul de conectare in preferinte
		try {
			this.close();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			Log.d(TAGLOG,e.getMessage());
		}
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		this.db_userid=pref.getString(context.getString(R.string.key_ecran3_user),"");
		this.db_password=pref.getString(context.getString(R.string.key_ecran3_parola),"");
		this.db_connect_string=
				pref.getString(context.getString(R.string.key_ecran3_ipserver),"")+
				'/'+pref.getString(context.getString(R.string.key_ecran3_nume_db),"")+";"+
				"encrypt=false;"+
				"user="+this.db_userid+";"+
				"password="+this.db_password+";"+
				"instance="+pref.getString(context.getString(R.string.key_ecran3_instanta_sql),"SQLEXPRESS")+";"+
				"loginTimeout=0";
		this.putSqlDriver();
		this.context=context;
	}

	public ResultSet query(String sqlSir) {
		return query(sqlSir,0);
	}

	public ResultSet query(String sqlSir,int nTimeoutSec) {
		Statement stat = null ;
		ResultSet res=null;
        Log.d("PRO&","1");
		try {
			if ((this.con !=null) && (!this.con.isClosed())) {
                Log.d("PRO&","2");
				this.resetLastErr();
                Log.d("PRO&","3");
				try {
                    Log.d("PRO&","4");
					stat=this.con.createStatement();
                    Log.d("PRO&","5");
					if (nTimeoutSec>0) {
                        Log.d("PRO&","6");
						stat.setQueryTimeout(20);
					}
                    Log.d("PRO&","7");

					res=stat.executeQuery(sqlSir);
                    Log.d("PRO&","8");
					}
				 catch (SQLException e ){
					 e.printStackTrace();
                     Log.d("PRO&","9");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            Log.d("PRO&","10");

		}
        Log.d("PRO&","11");
		return res;
	}
	
	public void exec(String sqlSir,int nSec) {
		Statement stat = null ;
		Boolean response =true;
		try {
			if (!this.con.isClosed()) {
				this.resetLastErr();
				try {
					stat=this.con.createStatement();
					if (nSec>0) {
						stat.setQueryTimeout(nSec);
					}
					stat.execute(sqlSir);
					}
				 catch (Exception e ){
					 response=false;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response=false;
		}
		this.setLastErr(!response);	
		
	}
	
	
	public void exec(String sqlSir) {
		exec(sqlSir,0);
	}
	
	public void open() {
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
			this.con = DriverManager.getConnection(this.db_connect_string,this.db_userid,this.db_password);
			//Log.d(TAGLOG,"Dupa conectare:"+this.con.isClosed());
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
	
	
	public void close() throws java.sql.SQLException{
		if (this.con !=null) {
			if (!this.con.isClosed()) {
				this.con.close();
			}
		}
			
	}
	public boolean getLastErr () {
		return err ;
	}
	public void setLastErr(Boolean lVal){
		err=lVal;
	}
	public void resetLastErr () {
		err=false ;
	}

		
//completeaza cu detaliile driverului la sirul de conectare	
	private void putSqlDriver () {
        if (! this.db_connect_string.contains("jdbc:jtds:sqlserver://"))
        	this.db_connect_string="jdbc:jtds:sqlserver://"+db_connect_string;
		
	}
}
