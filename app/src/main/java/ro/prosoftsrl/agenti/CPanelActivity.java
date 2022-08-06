package ro.prosoftsrl.agenti;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Discount;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempContinutDocumente;
import ro.prosoftsrl.diverse.ConvertNumar;
import ro.prosoftsrl.diverse.Siruri;
import ro.prosoftsrl.listare.ComunicatiiCasaMarcat;
import ro.prosoftsrl.sqlserver.MySQLDBadapter;
import ro.prosoftsrl.sqlserver.MySQLOpen;
import ro.prosoftsrl.sqlserver.MySQLQuery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CPanelActivity extends FragmentActivity implements ActivityReceiveActionsInterface {
    public static final String PREFS_NAME = "setariagenti";
    private long nrInitGenunic = 0;
    TaskSincro getInitTask = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpanel);

        Button deblocCasa = (Button) findViewById(R.id.btnCPanelDeblocCasa);
        deblocCasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ComunicatiiCasaMarcat.deblocCasa(getApplicationContext(), ConstanteGlobale.Tipuri_case_marcat.DATECS_DP_05);
            }
        });
Button btnTeste =(Button) findViewById(R.id.btnTesteDiverse) ;
btnTeste.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(), ConvertNumar.convert(202.30),Toast.LENGTH_LONG).show();
    }
});

        Button pret0 = (Button) findViewById(R.id.btnCPanelPuePret);
        pret0.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refaPreturi();
                // la doc facute cu pret 0 din greseala se actualizeaza preturile si valorile. Numai pe data de astazi


            }
        });

        Button rapZCasa = (Button) findViewById(R.id.btnCPanelraportZ);
        rapZCasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ComunicatiiCasaMarcat.rapZcasa(getApplicationContext(), ConstanteGlobale.Tipuri_case_marcat.DATECS_DP_05);
            }
        });

        Button decalDb = (Button) findViewById(R.id.btnCpanelCresteSerie);
        decalDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Bundle arg = new Bundle();
                arg.putInt("tipdialog", 1); // dialog de parola
                arg.putInt("actiune_la_activity", 2); // se transmite acest param pt a sti ce trebuie apelat
                // actiune_la_activity= 2 - decalare serie
                arg.putString("titlu", "Parola pentru confirmare");
                arg.putString("text_pozitiv", "Continua"); // apare primul
                arg.putString("text_negativ", "Renunta");
                apel_dialog_general(arg);
            }
        });

        Button copieDb = (Button) findViewById(R.id.btnCopiazaDb);
        copieDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "Copiere db", Toast.LENGTH_LONG).show();
                ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
                colectie.createCopieDb();
                colectie.close();
            }
        });

        // import setari in server
        Button expSetariInSql =(Button) findViewById(R.id.btnCPanelExpSetari);
        expSetariInSql.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Bundle arg = new Bundle();
                arg.putInt("tipdialog",0); // dialog simplu
                arg.putInt("actiune_la_activity",3); // se transmite acest param pt a sti ce trebuie apelat
                arg.putString("titlu", "Confirmati exportul setarilor in server");
                arg.putString("text_pozitiv", "Continua"); // apare primul
                arg.putString("text_negativ", "Renunta");
                apel_dialog_general(arg);

            }

        });


        // export setari in server
        Button impSetariDInSql =(Button) findViewById(R.id.btnCPanelImpSetari);
        impSetariDInSql.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Bundle arg = new Bundle();
                arg.putInt("tipdialog",0); // dialog simplu
                arg.putInt("actiune_la_activity",4); // se transmite acest param pt a sti ce trebuie apelat
                arg.putString("titlu", "Confirmati importul setarilor din server");
                arg.putString("text_pozitiv", "Continua"); // apare primul
                arg.putString("text_negativ", "Renunta");
                apel_dialog_general(arg);

            }

        });

        Button initBDLocal = (Button) findViewById(R.id.btnInitdataLocal);
        initBDLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Bundle arg = new Bundle();
                    arg.putInt("tipdialog", 1); // dialog de parola
                    arg.putInt("actiune_la_activity",1); // se transmite acest param pt a sti ce trebuie apelat
                    // actiune_la_activity= 1 - reinitializare baza de date
                    arg.putString("titlu", "Parola pentru confirmare");
                    arg.putString("text_pozitiv", "Continua"); // apare primul
                    arg.putString("text_negativ", "Renunta");
                    apel_dialog_general(arg);

            }
        });

        // testeaza server
        ((Button) findViewById(R.id.btnTestConnServer)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String error="";
                MySQLDBadapter sqldb = new MySQLDBadapter(getApplicationContext());
                MySQLOpen task = new MySQLOpen(); // deschide conex la sql
                task.execute(sqldb);
                try {
                    task.get(500000, TimeUnit.MILLISECONDS);
                    MySQLQuery query = new MySQLQuery();
                    query.sqlSir = "SELECT TOP 10 cod_int FROM GENUNIC";
                    query.execute(sqldb);
                    ResultSet res = query.get(500000, TimeUnit.MILLISECONDS);
                    while (res.next()) {
                        Toast.makeText(getApplicationContext(), "Cod int=" + res.getInt("cod_int"), Toast.LENGTH_SHORT).show();
                        Log.d("PRO&", "Cod int=" + res.getInt("cod_int"));
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    error=e.getMessage();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    error=e.getMessage();
                } catch (TimeoutException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    error=e.getMessage();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    error=e.getMessage();
                }
                Log.d("PRO&","Eroare la query:"+error);
                //sqldb.open();

//					TestServer test = new TestServer();
//					test.execute(sqldb);
//
//					try {
//						ResultSet res=test.get(10000, TimeUnit.MILLISECONDS);
//						//ResultSet res = test.get();
//						try {
//							while (res.next()) {
//								Toast.makeText(getApplicationContext(), "Cod int="+res.getInt("cod_int") , Toast.LENGTH_SHORT).show();
//								Log.d("", "Cod int="+res.getInt("cod_int"));
//							}
//						} catch (SQLException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}								
//					
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (ExecutionException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (TimeoutException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
                try {
                    sqldb.close();
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        });


        this.initState();

    }

    @Override
    public void onResume() {
        super.onResume();
        this.initState();

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            this.saveState();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getConnString() {
        return "";
    }

    public void saveState() throws SQLException {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
//	     Toast.makeText(this,settings.getString("sqlconnectionstring", "cucu") , Toast.LENGTH_LONG).show();
    }

    public void initState() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    }

    private class TaskSincro extends AsyncTask<String, String, Void> {
        private Activity mActivity = null;
        private Boolean mDone = false;
        private ProgressDialog myPd = null;

        Boolean isDone() {
            return mDone;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int iIdDevice = Integer.valueOf(settings.getString(getString(R.string.key_ecran1_id_agent), "0"));
            int iPrag=70000000; // pragul nu mai este necesar se preia din baza de date de la functia initnumber
            if (iIdDevice > 0) {
                MySQLDBadapter sqldb = new MySQLDBadapter(getApplicationContext());
                sqldb.open();
                if (nrInitGenunic == 0) {
                    publishProgress("Preia numar de initializare");
                    String sqlSir =" select dbo.initnumber("+iIdDevice+")"+" as cod";
/*
                            "select max(isnull(cod,0)) as cod from " +
                            "( SELECT a.cod from " +
                                    "( SELECT MAX(cod_int) as cod FROM antet WHERE id_device= " + iIdDevice +
                                    " and cod_int between "+iPrag +" (" + iIdDevice + " - 1) * 1000000+1 and "+iPrag +"  (" + iIdDevice + ") * 1000000-1" +
                                    " UNION all " +
                                    " SELECT MAX(cod_int) as cod FROM pozitii WHERE id_device=" + iIdDevice +
                                    " and cod_int between "+iPrag +"  (" + iIdDevice + " - 1) * 1000000+1 and "+iPrag +" (" + iIdDevice + ") * 1000000-1" +
                                    " UNION all " +
                                    " SELECT MAX(cod_int) as cod FROM pozambalaje WHERE id_device=" + iIdDevice +
                                    " and cod_int between "+iPrag +" (" + iIdDevice + " - 1) * 1000000+1 and "+iPrag +" (" + iIdDevice + ") * 1000000-1" +
                                    " UNION all " +
                                    " SELECT MAX(cod_int) as cod FROM sablon_antet WHERE id_device= " + iIdDevice +
                                    " and cod_int between "+iPrag +" (" + iIdDevice + " - 1) * 1000000+1 and "+iPrag +" (" + iIdDevice + ") * 1000000-1" +
                                    " UNION all " +
                                    " SELECT MAX(cod_int) as cod FROM sablon_pozitii WHERE id_device=" + iIdDevice +
                                    " and cod_int between "+iPrag +" (" + iIdDevice + " - 1) * 1000000+1 and "+iPrag +" (" + iIdDevice + ") * 1000000-1" +
                                    " UNION all " +
                                    " SELECT cod_int FROM sablon_antet WHERE id_device=" + iIdDevice +
                                    " and cod_int between "+iPrag +" + (" + iIdDevice + " - 1) * 1000000+1 and "+iPrag +" (" + iIdDevice + ") * 1000000-1" +
                                    " UNION all " +
                                    " SELECT MAX(cod_int) as cod FROM client WHERE id_device=" + iIdDevice +
                                    " and cod_int between "+iPrag +" (" + iIdDevice + " - 1) * 1000000+1 and "+iPrag +" + (" + iIdDevice + ") * 1000000-1" +
                                    ") ) ";
*/

                    Log.d("PRO&", "INITDB:" + sqlSir);
//                    sqldb.exec(sqlSir,10);
//                    Log.d("PRO&", "INITDB:" + "dupa crea temp");
//                    ResultSet res = sqldb.query("select max(isnull(cod,0)) as cod from #temptb1", 1);
                    ResultSet res = sqldb.query(sqlSir,10);
                    Log.d("PRO&", "Dupa resultset");
                    if (res != null) {
                        try {
                            res.next();
                            nrInitGenunic = res.getLong(res.findColumn("cod"));
                            Log.d("PRO&", "INITDB:" + "Nr de init=" + nrInitGenunic);
                            res.close();
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.d("PRO&", e.getMessage());
                        }
                    }
                    // sqldb.exec("drop table temptb1",10);
                }
                ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
                SQLiteDatabase db = colectie.getWritableDatabase();
                if (nrInitGenunic>0) {
                    colectie.initTabele(db, nrInitGenunic);
                    publishProgress("Baza de date a fost initializata cu numarul: " + nrInitGenunic);
                    try {
                    this.get(5000, TimeUnit.MILLISECONDS);}
                    catch ( Exception e ) {}
                }
                else {
                    publishProgress("Nu se poate face initializarea bazei de date. Lipsa conexiune. ");
                    try {
                        this.get(5000, TimeUnit.MILLISECONDS);}
                    catch ( Exception e ) {}
                }
                db.close();
                Log.d("PRO&", "INITDB:" + "Baza de date a fost initializata cu numarul: " + nrInitGenunic);
                // TODO Auto-generated method stub
                mDone = true;
            } else {
                //Toast.makeText(getApplicationContext(), "Atentie ! Nu a fost declarat cod agent pentru sincronizare", Toast.LENGTH_LONG).show();
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

        void attach(Activity a) {
            mActivity = a;
            if (mActivity != null) {
                myPd = new ProgressDialog(mActivity);
                myPd.setMessage("Asteapta");
                myPd.show();

            }
        }

        void detach() {
            if (myPd != null) {
                myPd.dismiss();
                myPd = null;
            }
            mActivity = null;
        }
    }

    private void refaPreturi() {
        ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
        SQLiteDatabase db = colectie.getWritableDatabase();
        String sData1 = Siruri.dtos(Calendar.getInstance(), "-");
        String sData2 = Siruri.dtos(Calendar.getInstance(), "-");
//      forteaza
        db.beginTransaction();
//          trece toate fac in aviz client
//			db.execSQL("UPDATE "+Table_Antet.TABLE_NAME +" SET "+Table_Antet.COL_ID_TIPDOC+"=3" +
//			" WHERE "+
//			"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)>='"+sData1+"'"+
//			" AND "+"SUBSTR("+Table_Antet.TABLE_NAME +"."+Table_Antet.COL_DATA+",1,10)<='"+sData2+"'"+
//			" AND "+Table_Antet.TABLE_NAME+"."+Table_Antet.COL_ID_TIPDOC+"=2"					
//			);

        // forteaza rectificarea preturilor
        db.execSQL("UPDATE " + Table_Antet.TABLE_NAME + " SET " + Table_Antet.COL_VAL_FARA + "=0," + Table_Antet.COL_VAL_TVA + "=0" +
                        " WHERE " +
                        "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                        " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                        " AND " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "<>6"
        );
        db.setTransactionSuccessful();
        db.endTransaction();

        String sqlCmd =
                " SELECT " +
                        Table_Antet.TABLE_NAME + "." + Table_Antet._ID + "," +
                        Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_PART + "," +
                        Table_Clienti.TABLE_NAME + "." + Table_Clienti.COL_PROC_RED +
                        " FROM " + Table_Antet.TABLE_NAME +
                        " INNER JOIN " + Table_Clienti.TABLE_NAME + " ON " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_PART + "=" +
                        Table_Clienti.TABLE_NAME + "." + Table_Clienti._ID +
                        " WHERE " +
                        Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_PART + ">0" +
                        " AND (" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_VAL_FARA + "+" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_VAL_TVA + "=0 " +
                        " OR " + Table_Clienti.TABLE_NAME + "." + Table_Clienti.COL_PROC_RED + "=100 ) " +
                        " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                        " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                        " AND " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "<>6";
        db.beginTransaction();
        Log.d("PRO", "Sql antete=" + sqlCmd);
        Cursor crsAntet = db.rawQuery(sqlCmd, null);
        crsAntet.moveToFirst();
        while (!crsAntet.isAfterLast()) {
            @SuppressLint("Range") long idAnt = crsAntet.getLong(crsAntet.getColumnIndex(Table_Antet._ID));
            @SuppressLint("Range") long idPart = crsAntet.getLong(crsAntet.getColumnIndex(Table_Antet.COL_ID_PART));
            String sqlSir = "SELECT " +
                    Table_Pozitii.TABLE_NAME + ".* , " +
                    Table_Discount.TABLE_NAME + "." + Table_Discount.COL_PRET_CU + " as pret , " +
                    " 1 as cu_tva  ," +
                    " 0 as este_bonus ," +
                    Table_Produse.TABLE_NAME + "." + Table_Produse.COL_PRET_CU + " as pret_cu_nom ," +
                    Table_Produse.TABLE_NAME + "." + Table_Produse.COL_PRET_FARA + " as pret_fara_nom " +
                    " FROM " + Table_Pozitii.TABLE_NAME +
                    " INNER JOIN " + Table_Produse.TABLE_NAME + " ON " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                    "=" + Table_Produse.TABLE_NAME + "." + Table_Produse._ID +
                    " LEFT JOIN " + Table_Discount.TABLE_NAME + " ON " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                    "=" + Table_Discount.TABLE_NAME + "." + Table_Discount.COL_ID_PRODUS +
                    " and " + Table_Discount.TABLE_NAME + "." + Table_Discount.COL_ID_CLIENT + "=" + idPart +
                    " WHERE " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_ANTET + "=" + idAnt;
            double nValFara = 0.0;
            double nValTva = 0.0;
            @SuppressLint("Range") double nProcRed = crsAntet.getLong(crsAntet.getColumnIndex(Table_Clienti.COL_PROC_RED));
            Log.d("PRO", "Sql pt pozitii: " + sqlSir);
            Cursor crsc = db.rawQuery(sqlSir, null);
            crsc.moveToFirst();
            while (!crsc.isAfterLast()) {
                Bundle poz = new Bundle();
                poz.putLong(Table_Pozitii._ID, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii._ID)));
                poz.putLong(Table_Pozitii.COL_ID_ANTET, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_ANTET)));
                poz.putLong(Table_Pozitii.COL_ID_PRODUS, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_PRODUS)));
                poz.putDouble(Table_Pozitii.COL_CANTITATE, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE)));
                poz.putDouble(Table_Pozitii.COL_COTA_TVA, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_COTA_TVA)));
                if (nProcRed == 100) {
                    poz.putDouble(Table_Pozitii.COL_PRET_FARA, 0.0);
                    poz.putDouble(Table_Pozitii.COL_PRET_CU, 0.0);
                    poz.putDouble("pret", 0.0);
                } else {
                    poz.putDouble(Table_Pozitii.COL_PRET_FARA, crsc.getDouble(crsc.getColumnIndexOrThrow("pret_fara_nom")));
                    poz.putDouble(Table_Pozitii.COL_PRET_CU, crsc.getDouble(crsc.getColumnIndexOrThrow("pret_cu_nom")));
                    if (crsc.getDouble(crsc.getColumnIndexOrThrow("pret")) == 0) {
                        poz.putDouble("pret", crsc.getDouble(crsc.getColumnIndexOrThrow("pret_cu_nom")));
                    } else {
                        poz.putDouble("pret", crsc.getDouble(crsc.getColumnIndexOrThrow("pret")));
                    }
                }
                poz.putDouble(Table_Pozitii.COL_PRET_FARA1, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_FARA1)));
                poz.putDouble(Table_Pozitii.COL_PRET_CU1, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_CU1)));
                poz.putInt("cu_tva", crsc.getInt(crsc.getColumnIndexOrThrow("cu_tva")));
                poz.putInt("este_bonus", crsc.getInt(crsc.getColumnIndexOrThrow("este_bonus")));
                ContentValues cvalpoz = Biz.getInsertPozitii(poz);
                cvalpoz.put(Table_Pozitii.COL_C_TIMESTAMP, "A");
                nValFara = nValFara + cvalpoz.getAsDouble(Table_Pozitii.COL_VAL_FARA) - cvalpoz.getAsDouble(Table_Pozitii.COL_VAL_RED);
                nValTva = nValTva + cvalpoz.getAsDouble(Table_Pozitii.COL_VAL_TVA) - cvalpoz.getAsDouble(Table_Pozitii.COL_TVA_RED);
                Log.d("PRO", "Randuri update:" +
                        db.update(Table_Pozitii.TABLE_NAME, cvalpoz, Table_Pozitii._ID + "=" + crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii._ID)), null));
                cvalpoz.clear();
                crsc.moveToNext();
            }
            crsc.close();
            sqlSir = "UPDATE " + Table_Antet.TABLE_NAME + " SET " + Table_Antet.COL_VAL_FARA + "=" + nValFara +
                    "," + Table_Antet.COL_VAL_TVA + "=" + nValTva + " WHERE " +
                    Table_Antet._ID + "=" + idAnt;
            Log.d("PRO", "Sql update antet:" + sqlSir);
            db.execSQL(sqlSir);
            crsAntet.moveToNext();
        }


        crsAntet.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    private void apel_dialog_general(Bundle arg) {
        // APEL dialog care cere parola
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialoggeneral");
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }
        DialogGeneralDaNu dlg = DialogGeneralDaNu.newinstance(arg);
        dlg.show(ft, "dialoggeneral");
    }

    //dialogul trimite aici actiunilw
    @Override
    public void transmite_actiuni(View view, Bundle arg) {
        // mai trebuie facuta structura de selectie pt tipul actiunii

        switch (arg.getInt("actiune_la_activity", 0)) {
            case 1: { // initi baza da date
                Log.d("PRO", arg.getString("valoare_retur1"));
                if (arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, 0) ==
                        ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_POZITIV) {
                    if (arg.getString("valoare_retur1").toUpperCase().equals("PRO"))
                        reinitializare_baza_date();
                    else
                        Toast.makeText(getApplicationContext(), "Parola gresita", Toast.LENGTH_LONG).show();
                }
            }
            break;
            case 2: {
                if (arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, 0) ==
                        ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_POZITIV) {
                    if (arg.getString("valoare_retur1").toUpperCase().equals("PRO"))
                        decalare_serie();
                    else
                        Toast.makeText(getApplicationContext(), "Parola gresita", Toast.LENGTH_LONG).show();
                }

            }
            break;
            case 3: {
                exportSetariInServer();
            }
            break;
            case 4: {
                importSetariDinServer();
            }
            break;
        }
    }

    public void importSetariDinServer(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String sqlSir =
                "EXEC myproc_get_setare_agent " +
                        settings.getString(getString(R.string.key_ecran1_id_agent), "0") ;
        TaskSQLImpSetariAgent sqlTask = new TaskSQLImpSetariAgent();
        sqlTask.attach(CPanelActivity.this);
        sqlTask.execute(sqlSir);
    }

    // exporta setarile in server
    public void exportSetariInServer () {
        String sqlSir ="";
        List<String> sqlCmd = new ArrayList<String>();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String,?> keys = settings.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
                String cTip="S";
            if ( entry.getValue().getClass().equals(String.class))
                cTip="S";
            else if ( entry.getValue().getClass().equals(Integer.class))
                cTip="I";
            else if ( entry.getValue().getClass().equals(Boolean.class))
                cTip="B";

                sqlSir =
                        "EXEC myproc_set_setare_agent " +
                                settings.getString(getString(R.string.key_ecran1_id_agent), "0") + ","  +
                                "'" + entry.getKey() + "', " +
                                "'" + entry.getValue() + "', " +
                                "'"+cTip+"'";
                sqlCmd.add(sqlSir);
        }
        TaskSQLExec sqlTask = new TaskSQLExec();
        sqlTask.attach(CPanelActivity.this);
        sqlTask.execute(sqlCmd);


    }

    @SuppressLint("Range")
    public void decalare_serie() {
        int nrDecal = 0;
        EditText edtNrDecal = (EditText) findViewById(R.id.txtCpanelCresteSerie);
        nrDecal = Siruri.getIntDinString(edtNrDecal.getText().toString());
        if (nrDecal != 0) {
            // se adauca nrdecal la toate idurile
            ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
            SQLiteDatabase db = colectie.getWritableDatabase();
            String sqlCmd =
                    " SELECT max(_id) as max,min(_id) as min from " +
                            " ( SELECT DISTINCT " + Table_Antet._ID + " from " + Table_Antet.TABLE_NAME +
                            " UNION " +
                            " SELECT DISTINCT " + Table_Pozitii._ID + " from " + Table_Pozitii.TABLE_NAME +
                            " UNION " +
                            " SELECT DISTINCT " + ColectieAgentHelper.Table_PozAmbalaje._ID + " from "
                                + ColectieAgentHelper.Table_PozAmbalaje.TABLE_NAME +
                            ")";
            Cursor crs = db.rawQuery(sqlCmd, null);
            if (crs != null && crs.getCount() > 0) {
                crs.moveToFirst();
                @SuppressLint("Range") long nDif = crs.getLong(crs.getColumnIndex("max")) - crs.getLong(crs.getColumnIndex("min"));
                // la numarul de decalare se adauga si diferenta pentru a nu da erori de dublare de index
                db.beginTransaction();
                sqlCmd = "UPDATE " + Table_Antet.TABLE_NAME + " SET " + Table_Antet._ID + "=" + Table_Antet._ID + "+" + (nrDecal + nDif);
                db.execSQL(sqlCmd);
                sqlCmd = "UPDATE " + Table_Pozitii.TABLE_NAME + " SET " + Table_Pozitii._ID + "=" + Table_Pozitii._ID + "+" + (nrDecal + nDif);
                db.execSQL(sqlCmd);
                sqlCmd = "UPDATE " + Table_Pozitii.TABLE_NAME + " SET " + Table_Pozitii.COL_ID_ANTET + "=" + Table_Pozitii.COL_ID_ANTET + "+" + (nrDecal + nDif);
                db.execSQL(sqlCmd);
                sqlCmd = "UPDATE " + ColectieAgentHelper.Table_PozAmbalaje.TABLE_NAME + " SET " +
                        ColectieAgentHelper.Table_PozAmbalaje._ID + "=" +
                        ColectieAgentHelper.Table_PozAmbalaje._ID + "+" + (nrDecal + nDif);
                db.execSQL(sqlCmd);
                sqlCmd = "UPDATE " + ColectieAgentHelper.Table_PozAmbalaje.TABLE_NAME + " SET " +
                        ColectieAgentHelper.Table_PozAmbalaje.COL_ID_ANTET + "=" +
                        ColectieAgentHelper.Table_PozAmbalaje.COL_ID_ANTET + "+" + (nrDecal + nDif);
                db.execSQL(sqlCmd);
                colectie.initGenunic(db, crs.getLong(crs.getColumnIndex("max")) + nrDecal + nDif);
                db.setTransactionSuccessful();
                db.endTransaction();
                Toast.makeText(getApplicationContext(), "Decalare finalizata", Toast.LENGTH_LONG).show();
                edtNrDecal.setText("");
            }
            crs.close();
            db.close();
            colectie.close();
        }

    }

    public void reinitializare_baza_date() {
        EditText viewInitGenunic = (EditText) findViewById(R.id.editNrInitdataLocal);
        try {
            nrInitGenunic = Integer.parseInt(viewInitGenunic.getText().toString());
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        // crearea bazei de date se face in asink
        Boolean lCreeaza = false;
        if (getInitTask == null) {
            lCreeaza = true;
        } else {
            if (getInitTask.isDone()) {
                getInitTask.detach();
                getInitTask = null;
                lCreeaza = true;
            }
        }
        if (lCreeaza) {
            getInitTask = new TaskSincro();
            getInitTask.attach(CPanelActivity.this);
            getInitTask.execute();
        }

    }
    // clasa pentru executare comanda sql fara returnare de rezultate
    private class TaskSQLExec extends AsyncTask<List<String>, String,Void > {
        private Activity mActivity = null;
        private Boolean mDone = false;
        private ProgressDialog myPd = null;


        Boolean isDone() {
            return mDone;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(List<String>... params) {
            // params e array de lista de string . Fiecare poz in lista este o instr sql
            List<String> sqlCMD =params[0];
            MySQLDBadapter sqldb = new MySQLDBadapter(getApplicationContext());
                sqldb.open();
                    publishProgress("Executa comanda in server");
            for (String sqlSir : sqlCMD ) {
                sqldb.exec(sqlSir,10);
            }
            try {
                sqldb.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            mDone = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result ) {
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

        void attach(Activity a) {
            mActivity = a;
            if (mActivity != null) {
                myPd = new ProgressDialog(mActivity);
                myPd.setMessage("Asteapta");
                myPd.show();

            }
        }

        void detach() {
            if (myPd != null) {
                myPd.dismiss();
                myPd = null;
            }
            mActivity = null;
        }
    }


    private class TaskSQLImpSetariAgent extends AsyncTask<String, String,Void > {
        private Activity mActivity = null;
        private Boolean mDone = false;
        private ProgressDialog myPd = null;

        Boolean isDone() {
            return mDone;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            // params e array de lista de string . Fiecare poz in lista este o instr sql
            String sqlSir =params[0];
            MySQLDBadapter sqldb = new MySQLDBadapter(getApplicationContext());
            sqldb.open();
            publishProgress("Executa comanda in server");
            ResultSet res=   sqldb.query(sqlSir,10);
            if (res!=null) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
                // exemplu editare preference
                SharedPreferences.Editor editor = settings.edit();
                try {
                    while (res.next()) {
                            if (res.getString("tip").equals("B")) {
                                if (res.getString("valoare").equals("false"))
                                    editor.putBoolean(res.getString("key"), false);
                                else
                                    editor.putBoolean(res.getString("key"), true);
                            } else {
                                editor.putString(res.getString("key"), res.getString("valoare"));
                            }
                    }
                } catch (SQLException throwables) {
                        throwables.printStackTrace();
                }
                editor.commit();
            }



            try {
                sqldb.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            mDone = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void result ) {
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

        void attach(Activity a) {
            mActivity = a;
            if (mActivity != null) {
                myPd = new ProgressDialog(mActivity);
                myPd.setMessage("Asteapta");
                myPd.show();

            }
        }

        void detach() {
            if (myPd != null) {
                myPd.dismiss();
                myPd = null;
            }
            mActivity = null;
        }
    }

}
