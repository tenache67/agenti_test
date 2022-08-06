package ro.prosoftsrl.agenti;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Ambalaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_PozAmbalaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Pozitii;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.Siruri;
import ro.prosoftsrl.sqlserver.MySQLDBadapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import static ro.prosoftsrl.agenthelper.ColectieAgentHelper.*;

public class SincroActivity extends Activity {
//	ProgressDialog myPd;
	TaskSincro task=null;
	private int iTipSincro =0 ; //0 - sincro total , 1 - sincro documente
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("crea","1");
	//	myPd=new ProgressDialog(this);
		Log.d("crea","2");
		super.onCreate(savedInstanceState);
		Log.d("crea","3");
		setContentView(R.layout.activity_sincro);
		Log.d("crea","4");
		task=(TaskSincro) getLastNonConfigurationInstance();
		Log.d("crea","5");
		if (task!=null) {
			Log.d("crea","7");
			if (!task.isDone()) {
				Log.d("crea","8");
				task.attach(this);				
			}
			Log.d("crea","10");

		}
		Log.d("crea", "6");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Button btn = (Button) findViewById(R.id.sincro_btn_sincronizare);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//myPd.setMessage("Asteapta");
				//myPd.show();
				iTipSincro=0;
				Boolean lCreeaza =false;
				if (task==null) {
					lCreeaza=true;
				} else {
					if (task.isDone()) {
						task.detach();
						task=null;
						lCreeaza=true;
					}
				}
				if (lCreeaza) {
					task=new TaskSincro();
					task.attach(SincroActivity.this);
					task.execute("");					
				}
			}

		});
		Button btnRetrans = (Button) findViewById(R.id.sincro_btn_retransmitezi);
		btnRetrans.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				retransZi();
			}
		});
//		Button btnDoc=(Button) findViewById(R.id.btnSincroTransDoc);
//		btnDoc.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				iTipSincro=1;
//				Boolean lCreeaza =false;
//				if (task==null) {
//					lCreeaza=true;
//				} else {
//					if (task.isDone()) {
//						task.detach();
//						task=null;
//						lCreeaza=true;
//					}
//				}
//				if (lCreeaza) {
//					task=new TaskSincro();
//					task.attach(SincroActivity.this);
//					task.execute("");					
//				}
//				
//			}
//		});
		
	}

	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		if (task !=null) {
			Log.d("11","");
			task.detach();
			Log.d("12","");
			return task;
		} else {
			Log.d("13","");
			return null ;
		}
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.sincro, menu);
		return true;
	}

	private class TaskSincro extends AsyncTask<String, String, Void> {
		private Activity mActivity =null;
		private Boolean mDone=false;
		private ProgressDialog myPd =null;

		Boolean isDone() { return mDone;}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(String... params) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Boolean lComenziOnline=settings.getBoolean(getString(R.string.key_ecran5_comenzi_online), false);
			int iIdDevice=Integer.valueOf(settings.getString(getString(R.string.key_ecran1_id_agent), "0"));
			ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
			SQLiteDatabase db = colectie.getWritableDatabase();	
			MySQLDBadapter sqldb= new MySQLDBadapter(getApplicationContext());
			sqldb.open();
			Sincronizare sincro=new Sincronizare(db,sqldb, iIdDevice);

			if (lComenziOnline) {
				// sincronizare pentru comenzi online. Se preiau in totalitate agentii , clientii si produsele
				publishProgress("Parteneri");
//                sincro.sincroPreiaClient(iIdDevice);
                sincro.sincroAdaugInServer(
                        Table_Partener.TABLE_NAME,
                        Table_Partener.STABLE_NAME,
                        Table_Partener.STR_PARTENER,
                        "",false
                );
                sincro.sincroPreiaDinServer(
                        Table_Partener.TABLE_NAME,
                        Table_Partener.TABLE_NAME,
                        Table_Partener.STR_PARTENER,
                        -1, ""
                );
                Log.d("PRO&","Dupa client");
				publishProgress("ClientAgent");
				sincro.sincroPreiaClientAgent(-1);
				publishProgress("Agent");
				sincro.sincroPreiaAgent(-1);
				publishProgress("Preia produse");
				sincro.sincroPreiaProduse(-1); // se preiau toate produsele
				
				
			} else {
				if (iIdDevice>0) {					
					switch (iTipSincro) {
                        case -1:


                            break;
                        case 0:
						// totala
						// se fac stergerile din tabela stergeri din server
                        publishProgress("Preia sabloane");
						sincro.sincroSablon(iIdDevice);
						publishProgress("Stergeri");
						sincro.sincroSterge(iIdDevice);
						publishProgress("Preia discounturi");
                            sincro.sincroPreiaDinServer(
									Table_Discount.TABLE_NAME,
									Table_Discount.STABLE_NAME,
									Table_Discount.STR_DISCOUNT,
									1, iIdDevice, ""
							);
						//sincro.sincroPreiaDiscount(iIdDevice);
						publishProgress("Preia produse");
						sincro.sincroPreiaProduse(-1); // se preiau toate produsele
                        //preluare coduri de bare sau alternative
                        // se pune -1 la idangent ca sa se preia toata tabela
						publishProgress("Preia coduri alternative");
							sincro.sincroPreiaDinServer(
									Table_Cod_Bare.TABLE_NAME,
									Table_Cod_Bare.STABLE_NAME,
									Table_Cod_Bare.STR_COD_BARE,
									1, -1 , ""
							);


								// sincro.sincroPreiaClient(iIdDevice);
								publishProgress("Preia clienti");
                        Log.d("PRO","1");
                        sincro.sincroAdaugInServerNou(
                                    Table_Clienti.TABLE_NAME,
                                    "partener",
                                    Table_Clienti.STR_CLIENTI,
                                    "",false
                            );
                        Log.d("PRO","2");
                        sincro.sincroPreiaDinServer(
                                    Table_Clienti.TABLE_NAME,
                                    Table_Clienti.STABLE_NAME,
                                    Table_Clienti.STR_CLIENTI,
                                    1,iIdDevice,""
                            );
                        Log.d("PRO","3");
                        publishProgress("Preia client_agent");
                            sincro.sincroAdaugInServerNou(
                                    Table_Client_Agent.TABLE_NAME,
                                    Table_Client_Agent.STABLE_NAME,
                                    Table_Client_Agent.STR_CLIENT_AGENT,
                                    "",false);
                            sincro.sincroPreiaDinServer(
                                    Table_Client_Agent.TABLE_NAME,
                                    Table_Client_Agent.STABLE_NAME,
                                    Table_Client_Agent.STR_CLIENT_AGENT,
                                    -1,Table_Client_Agent.COL_ID_AGENT+"="+iIdDevice);

                        publishProgress("Preia rute");
                        sincro.sincroPreiaDinServer(Table_Rute.TABLE_NAME,
                                Table_Rute.STABLE_NAME,
                                Table_Rute.STR_RUTE,1,-1,"");
						// pentru ca aici nu este break se trece mai departe la documente
					case 1:

						// sincro documnete
						Log.d("SINCRO","Sincornizare ambalaje");
						publishProgress("Preia ambalaje");
						sincro.sincroPreiaDinServer(Table_Ambalaje.TABLE_NAME, Table_Ambalaje.STABLE_NAME,
							Table_Ambalaje.STR_AMBALAJE, -1, "blocat=0");
						Log.d("SINCRO","Sincornizare documente");
						publishProgress("Transmite documente - pozitii ambalaje");
						sincro.sincroAdaugInServer(Table_PozAmbalaje.TABLE_NAME, Table_PozAmbalaje.STABLE_NAME, Table_PozAmbalaje.STR_POZAMBALAJE,
							Table_PozAmbalaje.COL_ID_ANTET+" IN ( SELECT "+Table_Antet._ID+" FROM "+Table_Antet.TABLE_NAME +" WHERE "+Table_Antet.COL_ID_TIPDOC+
							" IN  ("+Biz.TipDoc.ID_TIPDOC_COMANDA+","+Biz.TipDoc.ID_TIPDOC_FACTURA+","+Biz.TipDoc.ID_TIPDOC_BONFISC+
							","+Biz.TipDoc.ID_TIPDOC_AVIZCLIENT+"))",
									true);
						publishProgress("Transmite documente - antet");
						sincro.sincroAdaugInServer(Table_Antet.TABLE_NAME, Table_Antet.STABLE_NAME, Table_Antet.STR_ANTET,
									Table_Antet.COL_ID_TIPDOC+" IN ("+Biz.TipDoc.ID_TIPDOC_COMANDA+","+Biz.TipDoc.ID_TIPDOC_FACTURA+","+Biz.TipDoc.ID_TIPDOC_BONFISC+
									","+Biz.TipDoc.ID_TIPDOC_AVIZCLIENT+")",
									false);
						publishProgress("Transmite documente - pozitii");
						sincro.sincroAdaugInServer(Table_Pozitii.TABLE_NAME, Table_Pozitii.STABLE_NAME, Table_Pozitii.STR_POZITII,
									Table_Pozitii.COL_ID_ANTET+" IN ( SELECT "+Table_Antet._ID+" FROM "+Table_Antet.TABLE_NAME +" WHERE "+Table_Antet.COL_ID_TIPDOC+
									" IN  ("+Biz.TipDoc.ID_TIPDOC_COMANDA+","+Biz.TipDoc.ID_TIPDOC_FACTURA+","+Biz.TipDoc.ID_TIPDOC_BONFISC+
									","+Biz.TipDoc.ID_TIPDOC_AVIZCLIENT+"))",
									false);
						publishProgress("Transmite documente - antet AVIZE");
                            sincro.sincroAdaugInServer(Table_Antet.TABLE_NAME, Table_Antet.STABLE_NAME, Table_Antet.STR_ANTET,
                                    Table_Antet.COL_ID_TIPDOC+
                                            " IN  ("+Biz.TipDoc.ID_TIPDOC_AVIZINC+","+Biz.TipDoc.ID_TIPDOC_AVIZDESC +") AND "+
                                            Table_Antet.COL_CORESP+"="+"'AVIZINI'",
                                    false);

                            publishProgress("Transmite documente - pozitii AVIZE");
                            sincro.sincroAdaugInServer(Table_Pozitii.TABLE_NAME, Table_Pozitii.STABLE_NAME, Table_Pozitii.STR_POZITII,
                                    Table_Pozitii.COL_ID_ANTET+" IN ( SELECT "+Table_Antet._ID+" FROM "+Table_Antet.TABLE_NAME +" WHERE "+Table_Antet.COL_ID_TIPDOC+
                                            " IN  ("+Biz.TipDoc.ID_TIPDOC_AVIZINC+","+Biz.TipDoc.ID_TIPDOC_AVIZDESC +") AND "+
                                            Table_Antet.COL_CORESP+"="+"'AVIZINI'"+ " )",
                                    false);


						publishProgress("Transmite documente - Inc / Desc ambalaje ");
						// transmite antete incarcare descarcare care au ambalaje
						sincro.sincroAdaugInServer(Table_Antet.TABLE_NAME, Table_Antet.STABLE_NAME, Table_Antet.STR_ANTET,
									Table_Antet.COL_ID_TIPDOC+" IN ("+Biz.TipDoc.ID_TIPDOC_AVIZINC+","+Biz.TipDoc.ID_TIPDOC_AVIZDESC+")"+
									" AND "+Table_Antet._ID+" IN (SELECT "+Table_PozAmbalaje.COL_ID_ANTET+" FROM "+
											Table_PozAmbalaje.TABLE_NAME+" )",
									false);
						// transmite poz ambalaje la desc inc
						sincro.sincroAdaugInServer(Table_PozAmbalaje.TABLE_NAME, Table_PozAmbalaje.STABLE_NAME, Table_PozAmbalaje.STR_POZAMBALAJE,
								Table_PozAmbalaje.COL_ID_ANTET+" IN ( SELECT "+Table_Antet._ID+" FROM "+Table_Antet.TABLE_NAME +" WHERE "+Table_Antet.COL_ID_TIPDOC+
								" IN  ("+Biz.TipDoc.ID_TIPDOC_AVIZDESC+","+Biz.TipDoc.ID_TIPDOC_AVIZINC+"))",
										false);						
						publishProgress("Preia solduri partener");
						Log.d("SINCRO","Sincornizare sold part");					
						sincro.sincroPreiaSoldpart(iIdDevice);
						publishProgress("Preia solduri ambalaje");
						sincro.sincroPreiaSoldAmb(iIdDevice);
						Log.d("SINCRO","Sincronizare aviz inc");
						publishProgress("Preia aviz incarcare");
						sincro.sincroPreiaAvizGenerat(iIdDevice);
						publishProgress("Activare agent");
						break;
					default:
						break;
					}
                    // daca este varianta fara descarcare de stoc se preia din server avizul pentru stocul initial al zilei ( adica stocul final al zilei anterioare)
                    if (settings.getBoolean("key_ecran5_fara_descarcare",false)) {
                        publishProgress("Preia stoc initial zilnic");
                        sincro.sincroPreiaStocIniZi(iIdDevice, getApplicationContext());
						Log.d("SINCRO", "Dupa sincronizare stoc ini");
                    }
					publishProgress("Activeaza sincronizare agent");
					sincro.sincro_activeaza_agent();
					publishProgress("Terminare sincronizare");
					try {
						sqldb.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// TODO Auto-generated method stub
					mDone=true;
				} else {
					Toast.makeText(getApplicationContext(), "Atentie ! Nu a fost declarat cod agent pentru sincronizare", Toast.LENGTH_LONG).show();
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			myPd.dismiss();
		}	
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			//super.onProgressUpdate(values);
			myPd.setMessage(values[0]);
		}
		
		void attach (Activity a ) {
			mActivity=a;
			if (mActivity!=null) {
				myPd=new ProgressDialog(mActivity);
				myPd.setMessage("Asteapta");
				myPd.show();
				
			}
		}
		void detach () {
			if (myPd!=null) {
			myPd.dismiss();
			myPd=null;
			}
			mActivity=null;
		}
	}
	
	private void retransZi () {
		String sData1=Siruri.dtos(Calendar.getInstance(TimeZone.getTimeZone("GMT"),Locale.getDefault()),"-");
		String scmd1=
				" UPDATE " +Table_Antet.TABLE_NAME+" set "+Table_Antet.COL_C_TIMESTAMP+"='A' " +
				" where  "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)='"+sData1+"'";
		String scmd2=
			" UPDATE "+Table_Pozitii.TABLE_NAME+" set "+Table_Pozitii.COL_C_TIMESTAMP+"='A' "+
			" where "+Table_Pozitii.COL_ID_ANTET+" in (select _id from "+Table_Antet.TABLE_NAME+
				" where "+Table_Antet.COL_C_TIMESTAMP+"='A') ";
		String scmd3=
				" UPDATE "+Table_PozAmbalaje.TABLE_NAME+" set "+Table_PozAmbalaje.COL_C_TIMESTAMP+"='A' "+
						" where "+Table_PozAmbalaje.COL_ID_ANTET+" in (select _id from "+Table_Antet.TABLE_NAME+
						" where "+Table_Antet.COL_C_TIMESTAMP+"='A') ";

		ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
		SQLiteDatabase db = colectie.getWritableDatabase();	
		db.beginTransaction();
		db.execSQL(scmd1);
		db.execSQL(scmd2);
		db.execSQL(scmd3);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

//public void old_sincronizare() {
//	TextView view = (TextView) findViewById(R.id.sincro_txt_mesaje);
//	view.setText(this.getString(R.string.sincro_start));
//	ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
//	SQLiteDatabase db = colectie.getWritableDatabase();	
//	MySQLDBadapter sqldb= new MySQLDBadapter(getApplicationContext());
//	MySQLOpen open=new MySQLOpen();
//	try {
//		open.execute(sqldb).get(20000, TimeUnit.MILLISECONDS);
//		new Sincronizare(db,sqldb).sincroPreiaProduse();
//		new Sincronizare(db,sqldb).sincroPreiaClient();			
//	} catch (InterruptedException e1) {
//		// TODO Auto-generated catch block
//		e1.printStackTrace();
//	} catch (ExecutionException e1) {
//		// TODO Auto-generated catch block
//		e1.printStackTrace();
//	} catch (TimeoutException e1) {
//		// TODO Auto-generated catch block
//		e1.printStackTrace();
//	}		
//	finally {
//		db.close();
//		colectie.close();
//		try {
//			sqldb.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}			
//	}		
//	view.setText(this.getString(R.string.sincro_final));
//}
