package ro.prosoftsrl.agenti;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Agent;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Bloc_Cursa;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Client_Agent;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Mesaje;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Partener;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Sablon_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempContinutDocumente;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Tipdoc;
import ro.prosoftsrl.agenti.DialogListaDenumiri.onDlgClick;
import ro.prosoftsrl.diverse.Siruri;
import ro.prosoftsrl.sqlserver.MySQLDBadapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ListaDenumiriActivity extends FragmentActivity
        implements ActivityComunicatorInterface, onDlgClick, ActivityReceiveActionsInterface {
    int iIdMaster = 0;
    int iCodIntCrt = 0;
    int iTLD = 0;
    EditText etCautare;
    List<String> arraySpinner = new ArrayList<String>();
    ArrayAdapter<String> adapterSpin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iTLD = getIntent().getIntExtra("idTipLista", 0);
        setContentView(R.layout.activity_lista_denumiri);

        InitCautare(); //bara cautare

        InitSpinnerRezultate(); //unde sunt pusi itemi gasiti
    }

    private void InitSpinnerRezultate() {
        Spinner sp = (Spinner) findViewById(R.id.spinnerOptiuni);

        adapterSpin = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arraySpinner);


        adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapterSpin);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int z, long l) {
                ListaDenumiriFragment frg = (ListaDenumiriFragment) getSupportFragmentManager().findFragmentById(R.id.lista_denumiri_fragment);
                int childCount = frg.getListView().getAdapter().getCount();
                Cursor crs = (Cursor) frg.getListView().getItemAtPosition(1);
                crs.moveToFirst();
                String cautareString = ((String) sp.getSelectedItem()).toLowerCase();
                Log.d("EU CAUT", cautareString);
                for (int i = 0; i < childCount; i++) {
                    @SuppressLint("Range") String numeBase = crs.getString(crs.getColumnIndex(Table_Produse.COL_DENUMIRE));
                    numeBase = numeBase.toLowerCase();
                    if (numeBase.equals(cautareString)) {

                        frg.getListView().setSelection(i);
                        Log.d("GASIT", "YAY");
                        break;
                    }

                    crs.moveToNext();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void InitCautare() {
        etCautare = (EditText) findViewById(R.id.editTextCautare);
        etCautare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //frg.getListView();
                //RelativeLayout rl = (RelativeLayout) frg.getListView().getChildAt(i);
                //TextView tv = (TextView) rl.getChildAt(0);
                //int childCount = frg.getListView().getChildCount();
                adapterSpin.clear();
                Spinner sp = (Spinner) findViewById(R.id.spinnerOptiuni);


                String cautareString = etCautare.getText().toString().toLowerCase();
                if (cautareString.length() < 3)
                    return;

                ListaDenumiriFragment frg = (ListaDenumiriFragment) getSupportFragmentManager().findFragmentById(R.id.lista_denumiri_fragment);
                //frg.getListView().getChildAt(0).findViewById(R.id.listDen_txt_denumire_client).setVisibility((View.INVISIBLE));


                int childCount = frg.getListView().getAdapter().getCount();

                Cursor crs = (Cursor) frg.getListView().getItemAtPosition(1);

                crs.moveToFirst();
                // @SuppressLint("Range") String data = crs.getString(crs.getColumnIndex("name"));
                for (int i = 0; i < childCount; i++) {
                    @SuppressLint("Range") String numeBase = crs.getString(crs.getColumnIndex(Table_Produse.COL_DENUMIRE));
                    numeBase = numeBase.toLowerCase();
                    StringTokenizer stringTokenizer = new StringTokenizer(numeBase); //cauta fiecare cuvant al liniei

                    for (int o = 1; stringTokenizer.hasMoreTokens(); o++) {
                        if (stringTokenizer.nextToken().startsWith(cautareString) && !cautareString.isEmpty()) {
                            //rl.setVisibility(View.INVISIBLE);
                            //frg.getListView().setSelection(i);
                            sp.performClick();
                            adapterSpin.add(numeBase);


                            Log.d("FOUND", "YEEES");
                            break;
                        }

                    }


                    if (cautareString.equals("")) {
                        frg.getListView().setSelection(0);

                    }
                    crs.moveToNext();

                }
            }
        });
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    //getMenuInflater().inflate(R.menu.lista_denumiri, menu);
    //return true;
    //}

    public void closeActivity() {
        this.finish();
    }


    @Override
    public int transmite_iTLD() {
        // TODO Auto-generated method stub
        return this.iTLD;
    }

    // implementare pt onDlgClick
    // de la dialogul activat de lista de denumiri ( de ex la laista de clienti)
    @Override
    public void onListItemSelected(Bundle arg) {
        int iTLD = arg.getInt("iTLD", 0);
        long _id = arg.getLong("_id");
        int position = arg.getInt("which");
        String selection = arg.getStringArray("opt")[position];
        Long idSelection = arg.getLongArray("ids")[position];
        int idTipDialog = arg.getInt("tipdialog");
        if (selection != null) {
            Log.d("OPT", "0");
            switch (idTipDialog) {
                case ConstanteGlobale.TipDialogListaDenumiri.OPT_LISTA_CLIENTI:
                    // se apeleaza din dialogul care arata actiunile clientilor
                    Log.d("OPTLISTACLIENTI", "1");
                    actiuni_client(selection, iTLD, _id, idSelection, idTipDialog);
                    break;
                case ConstanteGlobale.TipDialogListaDenumiri.OPT_INC_DESC:
//				Toast.makeText(getApplicationContext(), "Inc desc", Toast.LENGTH_SHORT).show();
                    actiuni_inc_desc(selection, iTLD, _id, idSelection, idTipDialog);
                    Log.d("PRO", "Inc desc " + "id selectie :" + _id + "  idselection: " + idSelection);
                    break;
                case ConstanteGlobale.TipDialogListaDenumiri.OPT_LISTA_PRODUSE:
                    Log.d("PRO", "la produse");
                    break;
                case ConstanteGlobale.TipDialogListaDenumiri.OPT_ISTORIE_DOCUMENTE:
                    // se apeleaza din dialogul care afiseaza istoria documentelor
                    Log.d("OPTISTDOC", "2");
                    arata_document(arg); //selection, iTLD, _id, position, idTipDialog);
                    break;
                default:
                    break;
            }
        }
    }

    // se apeleaza cand in lista se afiseaza pentru aiz inc desc
    public void actiuni_inc_desc(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        switch ((int) idSelectie) {
            case ConstanteGlobale.Optiuni_ListaClienti.GEN_AVIZ_STOC_0: {
                actiune_GEN_AVIZ_STOC_0(selection, iTLD, _id, idSelectie, idTipDialog);
            }
            break;
            // generare aviz de inc pe drum. Se genereaza cu id antet -1
            case ConstanteGlobale.Optiuni_ListaClienti.GEN_AVIZ_INC: {
                // lista produse
                actiune_GEN_AVIZ_INC(selection, iTLD, _id, idSelectie, idTipDialog);
            }
            break;
            case ConstanteGlobale.Optiuni_ListaClienti.ADAUGA_DOCUMENT: {
                actiune_ADAUGA_DOCUMENT(selection, iTLD, _id, idSelectie, idTipDialog);
            }
            break;
            case ConstanteGlobale.Optiuni_ListaClienti.ISTORIE_DOCUMENTE: {
                actiune_ISTORIE_DOCUMENTE(selection, iTLD, _id, idSelectie, idTipDialog);
                break;
            }
            case ConstanteGlobale.Optiuni_ListaClienti.GEN_TRANSFER_AMANUNT: {
                Log.d("PRO", "TRANSFER AMANUNT");
                actiune_GEN_TRANSFER_AMANUNT(selection, Biz.TipListaDenumiri.TLD_TRANSFER_AMANUNT, _id, idSelectie, idTipDialog);
                break;
            }
            case ConstanteGlobale.Optiuni_ListaClienti.RENUNTA:
                break;
            default:
                break;
        }
    }

    // pt meniul care apara la click pe client
    public void actiuni_client(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        switch ((int) idSelectie) {
            // generarea aviz descarcare stoc
            case ConstanteGlobale.Optiuni_ListaClienti.ADAUGA_DOCUMENT: {
                actiune_ADAUGA_DOCUMENT(selection, iTLD, _id, idSelectie, idTipDialog);
            }
            break;
            case ConstanteGlobale.Optiuni_ListaClienti.ISTORIE_DOCUMENTE: {
                actiune_ISTORIE_DOCUMENTE(selection, iTLD, _id, idSelectie, idTipDialog);
            }
            break;
            case ConstanteGlobale.Optiuni_ListaClienti.SABLON_CERERE_MARFA: {
                actiune_SABLON_CERERE_MARFA(selection, Biz.TipListaDenumiri.TLD_SABLON_CERERE, _id, idSelectie, idTipDialog);
                break;
            }
            case ConstanteGlobale.Optiuni_ListaClienti.ADAUGA_CLIENT: {
                // porneste activitatea asociata
                Intent intent = new Intent();
                intent.setClassName(this, "ro.prosoftsrl.clienti" + "." + "ClientActivity");
                intent.putExtra("iTLD", 0);
                intent.putExtra("_id", (long) 0);
                intent.putExtra("actiune", "a");
                startActivityForResult(intent, 1);
                break;
            }
            case ConstanteGlobale.Optiuni_ListaClienti.MODIFICA_CLIENT: {
                // porneste activitatea asociata
                Intent intent = new Intent();
                intent.setClassName(this, "ro.prosoftsrl.clienti" + "." + "ClientActivity");
                intent.putExtra("iTLD", 0);
                intent.putExtra("_id", (long) _id);
                intent.putExtra("actiune", "m");
                startActivityForResult(intent, 1);
                break;
            }

            case ConstanteGlobale.Optiuni_ListaClienti.BLOCHEAZA_CLIENT: {
                // porneste activitatea asociata
                Intent intent = new Intent();
                intent.setClassName(this, "ro.prosoftsrl.clienti" + "." + "ClientActivity");
                intent.putExtra("iTLD", 0);
                intent.putExtra("_id", (long) _id);
                intent.putExtra("actiune", "s");
                startActivityForResult(intent, 1);
                break;
            }


            case ConstanteGlobale.Optiuni_ListaClienti.ACTUAL_AMBALAJE: {
                actiune_ACTUAL_AMBALAJE(selection, iTLD, _id, idSelectie, idTipDialog);
            }
            break;
            case ConstanteGlobale.Optiuni_ListaClienti.RENUNTA:
                break;
            default:
                break;
        }
    }

    // creeaza sirul de afisat in dialogul pentru istoria documentelor
    private String getOptiuneIstoric(String denDoc, String data, String numar, Double valFact, Double incasat) {
        String cSir = "";
        cSir = Siruri.padR(denDoc, 13);
        cSir = cSir + Siruri.dtoc(Siruri.cTod(data)) + "  ";
        cSir = cSir + Siruri.padR(numar, 13);
        cSir = cSir + Siruri.str(valFact, 12, 2);
        cSir = cSir + Siruri.str(incasat, 12, 2);
        return cSir;
    }

    @Override
    public long transmite_id_client() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Bundle transmite_intent() {
        // TODO Auto-generated method stub
        return getIntent().getExtras();
    }

    private void actiune_GEN_AVIZ_INC(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        int _idClient = -1;
        Bundle show = null;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        ColectieAgentHelper colectie = new ColectieAgentHelper(this);
        // se sterg eventuale avize anterioare
        SQLiteDatabase db = colectie.getWritableDatabase();
        db.beginTransaction();
        db.delete(Table_Antet.TABLE_NAME, Table_Antet._ID + "=-1", null);
        db.delete(Table_Pozitii.TABLE_NAME, Table_Pozitii.COL_ID_ANTET + "=-1", null);
        db.setTransactionSuccessful();
        db.endTransaction();
        Cursor crsc;


        if (pref.getBoolean("key_ecran5_fara_descarcare", false)) {
            // se trimite 1  la idMaster ca sa se faca generarea inclusiv cu cele blocate ( transmise prin sincronizare)
            crsc = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_GEN_AVIZ_INC, 1, 0, false);
        } else {
            crsc = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_GEN_AVIZ_INC, 0, 0, false);
        }
        // se salveaza antetul
        Long nIdAntet = (long) -1;
        // salvare pozitii
        if (crsc.getCount() > 0) {
            double nValFara = 0.0;
            double nValTva = 0.0;
            db.beginTransaction();
            crsc.moveToFirst();
            while (!crsc.isAfterLast()) {
                Log.d("SALVEAZA", "Inainte de get id");
                long idPoz = colectie.getNextId(colectie.getWritableDatabase());
                Log.d("SALVEAZA", "Dupa get id:" + idPoz);
                Bundle poz = new Bundle();
                poz.putLong(Table_Pozitii._ID, idPoz);
                poz.putLong(Table_Pozitii.COL_ID_ANTET, nIdAntet);
                poz.putLong(Table_Pozitii.COL_ID_PRODUS, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_PRODUS)));
                poz.putDouble(Table_Pozitii.COL_CANTITATE, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE)));
                poz.putDouble(Table_Pozitii.COL_COTA_TVA, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_COTA_TVA)));
                if (poz.getDouble(Table_Pozitii.COL_CANTITATE) != 0) {
                    ContentValues cvalpoz = Biz.getInsertPozitii(poz);
                    nValFara = 0;
                    nValTva = 0;
                    Log.d("SALVEAZA", "Inainte de insert pozitie:" + cvalpoz.get("_ID"));
                    db.insertOrThrow(Table_Pozitii.TABLE_NAME, null, cvalpoz);
                    Log.d("SALVEAZA", "Dupa pozitie");
                    cvalpoz.clear();
                }
                crsc.moveToNext();
                poz.clear();
            }
            Bundle arg = new Bundle();
            String nNumarDoc = Integer.toString(Biz.getNumarCrtDoc(this, "AVIZ INC"));
            arg.putLong(Table_Antet._ID, nIdAntet);
            arg.putString(Table_Antet.COL_CORESP, "" + nNumarDoc);
            long nIdAgent = Long.valueOf(pref.getString(this.getString(R.string.key_ecran1_id_agent), "0"));
            arg.putLong(Table_Antet.COL_ID_AGENT, nIdAgent);
            arg.putLong(Table_Antet.COL_ID_DEVICE, nIdAgent);
            arg.putLong(Table_Antet.COL_ID_PART, _idClient);
            arg.putLong(Table_Antet.COL_ID_TIPDOC, Biz.TipDoc.ID_TIPDOC_AVIZINC);
            arg.putInt(Table_Antet.COL_LISTAT, 0);
            arg.putString(Table_Antet.COL_NR_DOC, "" + nNumarDoc);
            arg.putInt(Table_Antet.COL_TERM_PL, 0);
            arg.putString(Table_Antet.COL_DATA, Siruri.dtoc(Siruri.getDateTime()));
            arg.putDouble(Table_Antet.COL_VAL_FARA, nValFara);
            arg.putDouble(Table_Antet.COL_VAL_TVA, nValTva);
            arg.putString(Table_Antet.COL_NR_CHITANTA, "");
            ContentValues cval = Biz.getInsertAntet(arg);
            db.insertOrThrow(Table_Antet.TABLE_NAME, null, cval);
            Log.d("SALVEAZA", "Dupa antet");
            db.setTransactionSuccessful();
            db.endTransaction();
            // s-a salvat . acum se apeleaza pt modificare
            // "opt" array cu optiunile "ids" array cu idurile "title" titlul dialogului
            show = new Bundle();
            show.putLongArray("ids", new long[]{(long) -1});
            // "iTLD" id pt tip lista denumiri "_id" un id de inregistrare pe care s-a apelat dilogul
            show.putInt("iTLD", 0);
            // "tipdialog" int ce reprezinta idul tipului de dialog "which" pozitia pe care s-a dat click
            show.putInt("which", 0);
            // se mai adauga "idantet" cu idul documentului din antet
            show.putLong("_id", _idClient); // idul clientului
        }
        crsc.close();
        db.close();
        colectie.close();
        if (show != null)
            // se apeleaza aratadocument din ListaDenumiriActivity
            arata_document(show);
    }

    private void actiune_GEN_AVIZ_STOC_0(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        Log.d("GEN0", "Gen aviz 0");
        int _idClient = -1;
        Bundle show = null;
        ColectieAgentHelper colectie = new ColectieAgentHelper(this);
        // se sterg eventuale avize anterioare
        SQLiteDatabase db = colectie.getWritableDatabase();
        db.beginTransaction();
        db.delete(Table_Antet.TABLE_NAME, Table_Antet._ID + "=-2", null);
        db.delete(Table_Pozitii.TABLE_NAME, Table_Pozitii.COL_ID_ANTET + "=-2", null);
        db.setTransactionSuccessful();
        db.endTransaction();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // se transmite numarul de zile de pastrare a documenteleor pe pozitia corespunzatpaer. daca este -1 nu se mai tine seama de data
        // la clcul
        Log.d("PRO&", "1");

        Cursor crsc = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_GEN_AVIZ_STOC_0,
                (settings.getBoolean("key_ecran5_fara_descarcare", false) ? -1 : 1)
                , 0, false);
        Log.d("PRO&", "2");
        // se salveaza antetul
        Long nIdAntet = (long) -2;
        // salvare pozitii
        if (crsc.getCount() > 0) {
            double nValFara = 0.0;
            double nValTva = 0.0;
            db.beginTransaction();
            crsc.moveToFirst();
            while (!crsc.isAfterLast()) {
                Log.d("SALVEAZA", "Inainte de get id");
                long idPoz = colectie.getNextId(colectie.getWritableDatabase());
                Log.d("SALVEAZA", "Dupa get id:" + idPoz);
                Bundle poz = new Bundle();
                poz.putLong(Table_Pozitii._ID, idPoz);
                poz.putLong(Table_Pozitii.COL_ID_ANTET, nIdAntet);
                poz.putLong(Table_Pozitii.COL_ID_PRODUS, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_PRODUS)));
                poz.putDouble(Table_Pozitii.COL_CANTITATE, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE)));
                poz.putDouble(Table_Pozitii.COL_COTA_TVA, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_COTA_TVA)));
                if (poz.getDouble(Table_Pozitii.COL_CANTITATE) != 0) {
                    ContentValues cvalpoz = Biz.getInsertPozitii(poz);
                    cvalpoz.put(Table_Pozitii.COL_C_TIMESTAMP, "A");
                    nValFara = 0;
                    nValTva = 0;
                    Log.d("SALVEAZA", "Inainte de insert pozitie:" + cvalpoz.get("_ID"));
                    db.insertOrThrow(Table_Pozitii.TABLE_NAME, null, cvalpoz);
                    Log.d("SALVEAZA", "Dupa pozitie");
                    cvalpoz.clear();
                }
                crsc.moveToNext();
                poz.clear();
            }
            Bundle arg = new Bundle();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String nNumarDoc = Integer.toString(Biz.getNumarCrtDoc(this, "AVIZ DESC"));
            arg.putLong(Table_Antet._ID, nIdAntet);
            arg.putString(Table_Antet.COL_CORESP, "" + nNumarDoc);
            long nIdAgent = Long.valueOf(pref.getString(this.getString(R.string.key_ecran1_id_agent), "0"));
            arg.putLong(Table_Antet.COL_ID_AGENT, nIdAgent);
            arg.putLong(Table_Antet.COL_ID_DEVICE, nIdAgent);
            arg.putLong(Table_Antet.COL_ID_PART, _idClient);
            arg.putLong(Table_Antet.COL_ID_TIPDOC, Biz.TipDoc.ID_TIPDOC_AVIZDESC);
            arg.putString(Table_Antet.COL_DATA, Siruri.ttos(Siruri.getDateTime()));
            arg.putInt(Table_Antet.COL_LISTAT, 0);
            arg.putString(Table_Antet.COL_NR_DOC, "" + nNumarDoc);
            arg.putInt(Table_Antet.COL_TERM_PL, 0);
            arg.putDouble(Table_Antet.COL_VAL_FARA, nValFara);
            arg.putDouble(Table_Antet.COL_VAL_TVA, nValTva);
            arg.putString(Table_Antet.COL_NR_CHITANTA, "");
            ContentValues cval = Biz.getInsertAntet(arg);
            cval.put(Table_Antet.COL_C_TIMESTAMP, "A");
            db.insertOrThrow(Table_Antet.TABLE_NAME, null, cval);
            Log.d("SALVEAZA", "Dupa antet");
            db.setTransactionSuccessful();
            db.endTransaction();

            // s-a salvat . acum se apeleaza pt modificare
            // "opt" array cu optiunile "ids" array cu idurile "title" titlul dialogului
            show = new Bundle();
            show.putLongArray("ids", new long[]{(long) -2}); // idul de antet
            // "iTLD" id pt tip lista denumiri "_id" un id de inregistrare pe care s-a apelat dilogul
            show.putInt("iTLD", 0);
            // "tipdialog" int ce reprezinta idul tipului de dialog "which" pozitia pe care s-a dat click
            show.putInt("which", 0);
            // se mai adauga "idantet" cu idul documentului din antet
            show.putLong("_id", _idClient); // idul clientului
        }
        crsc.close();
        db.close();
        colectie.close();
        if (show != null)
            // se apeleaza aratadocument din ListaDenumiriActivity
            arata_document(show);

    }

    // nota de transfer intre productie si amanunt
    private void actiune_GEN_TRANSFER_AMANUNT(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        Log.d("GEN0", "Gen TRANSFER AMANUNT");
        int _idClient = -3;
        Bundle show = null;
        ColectieAgentHelper colectie = new ColectieAgentHelper(this);
        // se sterg eventuale avize anterioare
        SQLiteDatabase db = colectie.getWritableDatabase();
        db.beginTransaction();
        db.delete(Table_Antet.TABLE_NAME, Table_Antet._ID + "=-3", null);
        db.delete(Table_Pozitii.TABLE_NAME, Table_Pozitii.COL_ID_ANTET + "=-3", null);
        db.setTransactionSuccessful();
        db.endTransaction();
        Cursor crsc = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_TRANSFER_AMANUNT, 0, 0, false);
        // se salveaza antetul
        Long nIdAntet = (long) -3;
        // salvare pozitii
        if (crsc.getCount() > 0) {
            double nValFara = 0.0;
            double nValTva = 0.0;
            db.beginTransaction();
            crsc.moveToFirst();
            while (!crsc.isAfterLast()) {
                Log.d("SALVEAZA", "Inainte de get id");
                long idPoz = colectie.getNextId(colectie.getWritableDatabase());
                Log.d("SALVEAZA", "Dupa get id:" + idPoz);
                Bundle poz = new Bundle();
                poz.putLong(Table_Pozitii._ID, idPoz);
                poz.putLong(Table_Pozitii.COL_ID_ANTET, nIdAntet);
                poz.putLong(Table_Pozitii.COL_ID_PRODUS, crsc.getLong(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_ID_PRODUS)));
                poz.putDouble(Table_Pozitii.COL_CANTITATE, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_CANTITATE)));
                poz.putDouble(Table_Pozitii.COL_COTA_TVA, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_COTA_TVA)));
                poz.putDouble(Table_Pozitii.COL_PRET_CU, crsc.getDouble(crsc.getColumnIndexOrThrow(Table_Pozitii.COL_PRET_CU)));
                poz.putInt("cu_tva", 1);
                if (poz.getDouble(Table_Pozitii.COL_CANTITATE) != 0) {
                    ContentValues cvalpoz = Biz.getInsertPozitii(poz);
                    cvalpoz.put(Table_Pozitii.COL_C_TIMESTAMP, "A");
                    nValFara = 0;
                    nValTva = 0;
                    Log.d("SALVEAZA", "Inainte de insert pozitie:" + cvalpoz.getAsLong("_ID"));
                    Log.d("SALVEAZA", "Dupa pozitie>: " + db.insertOrThrow(Table_Pozitii.TABLE_NAME, null, cvalpoz));
                    cvalpoz.clear();
                }
                crsc.moveToNext();
                poz.clear();
            }
            Bundle arg = new Bundle();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String nNumarDoc = Integer.toString(Biz.getNumarCrtDoc(this, "AVIZ DESC"));
            arg.putLong(Table_Antet._ID, nIdAntet);
            arg.putString(Table_Antet.COL_CORESP, "" + nNumarDoc);
            long nIdAgent = Long.valueOf(pref.getString(this.getString(R.string.key_ecran1_id_agent), "0"));
            arg.putLong(Table_Antet.COL_ID_AGENT, nIdAgent);
            arg.putLong(Table_Antet.COL_ID_DEVICE, nIdAgent);
            arg.putLong(Table_Antet.COL_ID_PART, _idClient);
            arg.putLong(Table_Antet.COL_ID_TIPDOC, Biz.TipDoc.ID_TIPDOC_TRANSAM);
            arg.putString(Table_Antet.COL_DATA, Siruri.dtoc(Siruri.getDateTime()));
            arg.putInt(Table_Antet.COL_LISTAT, 0);
            arg.putString(Table_Antet.COL_NR_DOC, "" + nNumarDoc);
            arg.putInt(Table_Antet.COL_TERM_PL, 0);
            arg.putDouble(Table_Antet.COL_VAL_FARA, nValFara);
            arg.putDouble(Table_Antet.COL_VAL_TVA, nValTva);
            ContentValues cval = Biz.getInsertAntet(arg);
            cval.put(Table_Antet.COL_C_TIMESTAMP, "A");
            db.insertOrThrow(Table_Antet.TABLE_NAME, null, cval);
            Log.d("SALVEAZA", "Dupa antet");
            db.setTransactionSuccessful();
            db.endTransaction();

            // s-a salvat . acum se apeleaza pt modificare
            // "opt" array cu optiunile "ids" array cu idurile "title" titlul dialogului
            show = new Bundle();
            show.putLongArray("ids", new long[]{(long) -3}); // idul de antet
            // "iTLD" id pt tip lista denumiri "_id" un id de inregistrare pe care s-a apelat dilogul
            show.putInt("iTLD", 0);
            // "tipdialog" int ce reprezinta idul tipului de dialog "which" pozitia pe care s-a dat click
            show.putInt("which", 0);
            // se mai adauga "idantet" cu idul documentului din antet
            show.putLong("_id", _idClient); // idul clientului
        }
        crsc.close();
        db.close();
        colectie.close();
        if (show != null)
            // se apeleaza aratadocument din ListaDenumiriActivity
            arata_document(show);

    }

    // afiseaza documentul pentru relistare
    public void arata_document(Bundle arg) {
        // se extrage din arg
        // "opt" array cu optiunile "ids" array cu idurile "title" titlul dialogului
        // "iTLD" id pt tip lista denumiri "_id" un id de inregistrare pe care s-a apelat dilogul
        // "tipdialog" int ce reprezinta idul tipului de dialog "which" pozitia pe care s-a dat click
        // "actiune" reprezinta o actiune care se transmite mai departe
        // se mai adauga "idantet" cu idul documentului din antet
        long idAntet;
        try {
            long[] ids = arg.getLongArray("ids");
            int pos = arg.getInt("which");
            idAntet = ids[pos];
//			ColectieAgentHelper colectie = new ColectieAgentHelper(this);
//			colectie.getWritableDatabase().execSQL("DELETE FROM "+Table_TempContinutDocumente.TABLE_NAME);
//			colectie.close();
            // porneste activitatea asociata
            Intent intent = new Intent();
            intent.setClassName(this, "ro.prosoftsrl.documente" + "." + "DocumenteActivity");
            intent.putExtra("iTLD", iTLD);
            intent.putExtra("_id", arg.getLong("_id")); // idul clientului
            intent.putExtra("idantet", idAntet); // idul
            intent.putExtra("actiune", "m"); // transmite modificare
            startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    // activare fereastra pentru ambalaje pe client la optiunile clientului
    private void actiune_ACTUAL_AMBALAJE(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        Bundle arg = new Bundle();
        arg.putInt("iTLD", iTLD);
        arg.putLong("_id", _id); //in acest caz se trimite idul clientului
        // porneste activitatea asociata
        Intent intent = new Intent();
        intent.setClassName(this, "ro.prosoftsrl.documente" + "." + "AmbalajeActivity");
        intent.putExtra("iTLD", iTLD);
        intent.putExtra("_id", _id); // id client
        intent.putExtra("actiune", "actamb"); // se transmite actamb ca sa se faca actualizare fara
        startActivity(intent);


        arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ARATA_AMBALAJE);
        transmite_actiuni(null, arg);
    }


    // se apeleaza de la dialogul
    private void actiune_ADAUGA_DOCUMENT(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        // se apeleaza dialogul de alegere sablon , pentru initializarea continutului
        // urmeaza apelarea dialogalegesablon
        if (iTLD != Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC) {
            ColectieAgentHelper colectie = new ColectieAgentHelper(this);
            SQLiteDatabase db = colectie.getReadableDatabase();
            String sql = "SELECT * FROM " + Table_Clienti.TABLE_NAME + " WHERE _id=" + _id;
            Cursor crsClient = db.rawQuery(sql, null);
            crsClient.moveToFirst();
            boolean lBlocatV = (crsClient.getInt(crsClient.getColumnIndex(Table_Clienti.COL_BLOCAT_VANZARE)) == 1);
            crsClient.close();
            db.close();
            colectie.close();
            if (lBlocatV) {
                // daca este blocat_vanzare =1 se inchide
                Toast.makeText(getApplicationContext(), "Clientul este blocat la vanzare", Toast.LENGTH_LONG).show();
            } else {
                Bundle fb = new Bundle();
                fb.putString("text_pozitiv", "Continua");
                fb.putString("text_negativ", "Fara sablon (INCASARE)");
                fb.putString("titlu", "Preia continut din sablon");
                fb.putInt("iTLD", iTLD);
                fb.putLong("_id", _id); //in acest caz se trimite idul clientului
                fb.putInt("tipdialog", 1);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("alegesablondialog");
                if (prev != null) {
                    ft.remove(prev);
                    ft.commit();
                }
                // pentru avizele de inc desc nu se fol sablon
                DialogSablonAlege dlg = DialogSablonAlege.newinstance(fb);
                dlg.show(ft, "alegesablondialog");
            }
        } else {
            Bundle arg = new Bundle();
            arg.putInt("iTLD", iTLD);
            arg.putLong("_id", _id); //in acest caz se trimite idul clientului
            arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ADAUGA_DOCUMENT_FARA_SABLON);
            transmite_actiuni(null, arg);
        }

        // se sterg eventuale avize anterioare
        //Toast.makeText(this, selection, Toast.LENGTH_SHORT).show();
        // sterge tabela temporara pt continut
    }

    private void actiune_ISTORIE_DOCUMENTE(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        ColectieAgentHelper colectie = new ColectieAgentHelper(this);
        Cursor crs = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_ISTORIC_DOC, _id, _id, false);
        if (crs.getCount() > 0) {
            String[] opt = new String[crs.getCount()];
            long[] ids = new long[crs.getCount()];
            crs.moveToFirst();
            for (int i = 0; i < crs.getCount(); i++) {
                opt[i] = getOptiuneIstoric(
                        crs.getString(crs.getColumnIndexOrThrow(Table_Tipdoc.TABLE_NAME + "_" + Table_Tipdoc.COL_DENUMIRE)),
                        crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_DATA)),
                        crs.getString(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_NR_DOC)),
                        crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_VAL_FARA)) +
                                crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_VAL_TVA)),
                        crs.getDouble(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet.COL_INCASAT))
                );

                ids[i] = crs.getLong(crs.getColumnIndexOrThrow(Table_Antet.TABLE_NAME + "_" + Table_Antet._ID));
                crs.moveToNext();

            }
            // urmeaza apelarea dialoglistadenumiri
            Bundle fb = new Bundle();
            fb.putStringArray("opt", opt);
            fb.putLongArray("ids", ids);
            fb.putString("title", "Document         Data        Nr          Valoare   Incasat");
            fb.putInt("iTLD", 0);
            fb.putLong("_id", _id); //in acest caz se trimite idul clientului
            fb.putInt("tipdialog", ConstanteGlobale.TipDialogListaDenumiri.OPT_ISTORIE_DOCUMENTE);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("istoricdialog");
            if (prev != null) {
                ft.remove(prev);
                ft.commit();
            }
            DialogListaDenumiri dlg = DialogListaDenumiri.newInstance(fb);
            dlg.show(ft, "istoricdialog");

        } else {
            Toast.makeText(this, "Nu exista documente", Toast.LENGTH_SHORT).show();
        }
        crs.close();
        colectie.close();
    }

    private void actiune_SABLON_CERERE_MARFA(String selection, int iTLD, long _id, long idSelectie, int idTipDialog) {
        // urmeaza apelarea dialogalegesablon
        Bundle fb = new Bundle();
        fb.putString("text_pozitiv", "Continua");
        fb.putString("text_negativ", "Renunta");
        fb.putString("titlu", "Alege cerere");
        fb.putInt("iTLD", iTLD);
        fb.putLong("_id", _id); //in acest caz se trimite idul clientului
        Log.d("PRO", "La sablon _id=" + _id);
        fb.putInt("tipdialog", 0);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("alegesablondialog");
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }
        DialogSablonAlege dlg = DialogSablonAlege.newinstance(fb);
        dlg.show(ft, "alegesablondialog");
    }

    @Override
    public void transmite_actiuni(View view, final Bundle arg) {
        // primeste date de la dialog (dialogul de alegere zi si cursa pt sablon) si apeleaza macheta pt sablon
        int iAct = arg.getInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE);
        switch (iAct) {
            case ConstanteGlobale.Actiuni_la_documente.ALEGE_AGENT: {
                // se promeste idul agentului la keya _id
                long id = arg.getLong("_id");
                ColectieAgentHelper colectie = new ColectieAgentHelper(this);
                SQLiteDatabase db = colectie.getWritableDatabase();
                db.beginTransaction();
                db.execSQL(" UPDATE " + Table_Agent.TABLE_NAME + " set " + Table_Agent.COL_ACTIV + " =0 ");
                db.execSQL(" UPDATE " + Table_Agent.TABLE_NAME + " set " + Table_Agent.COL_ACTIV + " =1 " + " where " + Table_Agent._ID + "=" + id);
                db.delete(Table_Clienti.TABLE_NAME, null, null);
                String sqlCmd = " INSERT INTO " + Table_Clienti.TABLE_NAME + " ( " +
                        Table_Clienti._ID + "," +
                        Table_Clienti.COL_ORDONARE + "," +
                        Table_Clienti.COL_ID_PART + "," +
                        Table_Clienti.COL_DENUMIRE + "," +
                        Table_Clienti.COL_NR_FISc + "," +
                        Table_Clienti.COL_NR_RC + "," +
                        Table_Clienti.COL_JUDET + "," +
                        Table_Clienti.COL_LOC + "," +
                        Table_Clienti.COL_ADRESA + "," +
                        Table_Clienti.COL_TEL1 + "," +
                        Table_Clienti.COL_TEL2 + "," +
                        Table_Clienti.COL_CONTACT + "," +
                        Table_Clienti.COL_BANCA + "," +
                        Table_Clienti.COL_CONT + "," +
                        Table_Clienti.COL_ID_ZONA +
                        " ) " +
                        " SELECT " +
                        Table_Partener.TABLE_NAME + "." + Table_Partener._ID + "," +
                        Table_Client_Agent.COL_ORDONARE + "," +
                        Table_Partener.COL_ID_PART + "," +
                        Table_Partener.COL_DENUMIRE + "," +
                        Table_Partener.COL_NR_FISc + "," +
                        Table_Partener.COL_NR_RC + "," +
                        Table_Partener.COL_JUDET + "," +
                        Table_Partener.COL_LOC + "," +
                        Table_Partener.COL_ADRESA + "," +
                        Table_Partener.COL_TEL1 + "," +
                        Table_Partener.COL_TEL2 + "," +
                        Table_Partener.COL_CONTACT + "," +
                        Table_Partener.COL_BANCA + "," +
                        Table_Partener.COL_CONT + "," +
                        Table_Partener.COL_ID_ZONA +
                        " from " + Table_Partener.TABLE_NAME + " inner join " + Table_Client_Agent.TABLE_NAME + " on " +
                        Table_Partener.TABLE_NAME + "." + Table_Partener._ID + " = " + Table_Client_Agent.TABLE_NAME + "." + Table_Client_Agent.COL_ID_CLIENT +
                        " where " + Table_Partener.TABLE_NAME + "." + Table_Partener.COL_BLOCAT + "=0" +
                        " and " + Table_Client_Agent.TABLE_NAME + "." + Table_Client_Agent.COL_ID_AGENT + " = " + id;
                Log.d("PRO", "Sir clienti: " + sqlCmd);
                db.execSQL(sqlCmd);
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
                colectie.close();
                this.finish();
            }
            break;
            case ConstanteGlobale.Actiuni_la_documente.ALEGE_SABLON: {
                long nIdAntet = 0;
                long iAgent = 0;

                // se cauta sablon .Daca exista se initializeaza tempcontinut, daca nu ,se goleste
                ColectieAgentHelper colectie = new ColectieAgentHelper(this);
                final SQLiteDatabase db = colectie.getWritableDatabase();
                db.beginTransaction();
                db.execSQL("DELETE FROM " + Table_TempContinutDocumente.TABLE_NAME);
                db.setTransactionSuccessful();
                db.endTransaction();
                // daca este varianta de comenzi online se cauta sablonul in server
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean lComenziOnline = settings.getBoolean(getString(R.string.key_ecran5_comenzi_online), false);
                if (!lComenziOnline) {
                    iAgent = Integer.valueOf(settings.getString(getApplicationContext().getString(R.string.key_ecran1_id_agent), "0"));
                } else {
                    // se extrage idul agentului curent
                    Cursor crs = db.rawQuery("SELECT " + Table_Agent._ID + " from " + Table_Agent.TABLE_NAME + " where " + Table_Agent.COL_ACTIV + "=1", null);
                    if (crs.getCount() > 0) {
                        crs.moveToFirst();
                        iAgent = crs.getInt(crs.getColumnIndex(Table_Agent._ID));
                    }
                    crs.close();
                }
                final long iCodAgent = iAgent;

                if (!lComenziOnline) {
                    // sablon din local
                    Cursor crs = db.rawQuery(
                            Biz.getSqlCautaSablon(
                                    arg.getLong("_id"),
                                    iAgent,
                                    arg.getLong("id_ruta"),
                                    arg.getInt("id_cursa")),
                            null);
                    if (crs != null && crs.getCount() > 0) {
                        crs.moveToFirst();
                        nIdAntet = crs.getLong(crs.getColumnIndexOrThrow(Table_Sablon_Antet._ID));
                        Cursor crstemp = Biz.getCursorListaDenumiri(
                                colectie,
                                Biz.TipListaDenumiri.TLD_LINIE_SABLON,
                                nIdAntet,
                                arg.getLong("_id"),
                                true);
                        Log.d("STARTSABLON", "Row: " + crstemp.getCount());
                        ContentValues cval = new ContentValues();
                        if (crstemp.getCount() > 0) {
                            crstemp.moveToFirst();
                            db.beginTransaction();
                            db.delete(Table_Mesaje.TABLE_NAME, Table_Mesaje.COL_ID_MESAJ + "=" + ConstanteGlobale.Mesaje.SUCCES_PREIA_SABLON, null);
                            while (!crstemp.isAfterLast()) {
                                cval.put(Table_TempContinutDocumente._ID, crstemp.getLong(crstemp.getColumnIndex(Table_Produse._ID)));
                                cval.put(Table_TempContinutDocumente.COL_CANTITATE, crstemp.getDouble(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_CANTITATE)));
                                cval.put(Table_TempContinutDocumente.COL_DIFERENTE, crstemp.getDouble(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_DIFERENTE)));
                                //cval.put(Table_TempContinutDocumente.COL_DIFERENTE, crstemp.getDouble(crstemp.getColumnIndex("DIFERENTE")));
                                cval.put(Table_TempContinutDocumente.COL_ID_PRODUS, crstemp.getLong(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_ID_PRODUS)));
                                cval.put(Table_TempContinutDocumente.COL_S_TIMESTAMP, crstemp.getLong(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_S_TIMESTAMP)));
                                db.insert(Table_TempContinutDocumente.TABLE_NAME, null, cval);
                                cval.clear();
                                crstemp.moveToNext();
                            }
                            db.setTransactionSuccessful();
                            db.endTransaction();
                        }
                        cval.clear();
                        cval.put(Table_Mesaje.COL_ID_MESAJ, ConstanteGlobale.Mesaje.SUCCES_PREIA_SABLON);
                        db.beginTransaction();
                        db.insert(Table_Mesaje.TABLE_NAME, null, cval);
                        db.setTransactionSuccessful();
                        db.endTransaction();
                        crstemp.close();
                    }
                    crs.close();

                } else {
                    // sablon din server
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            db.beginTransaction();
                            db.delete(Table_Mesaje.TABLE_NAME, Table_Mesaje.COL_ID_MESAJ + "=" + ConstanteGlobale.Mesaje.SUCCES_PREIA_SABLON, null);
                            db.setTransactionSuccessful();
                            db.endTransaction();
                            String sqlCmd = " SELECT " +
                                    "SABLON_ANTET.cod_int as SABLON_ANTET_cod_int ," +
                                    "SABLON_POZITII.cod_int ," +
                                    "SABLON_POZITII.id_antet ," +
                                    "SABLON_POZITII.id_produs ," +
                                    "SABLON_POZITII.cantitate ," +
                                    "SABLON_POZITII.diferente ," +
                                    "SABLON_POZITII.cod_int as " + Table_TempContinutDocumente.COL_S_TIMESTAMP +
                                    " from SABLON_ANTET inner join SABLON_POZITII on	SABLON_ANTET.cod_int=SABLON_POZITII.id_antet " +
                                    " WHERE " +
                                    "SABLON_ANTET.id_part= " + arg.getLong("_id") + " and " +
                                    "SABLON_ANTET.id_agent= " + iCodAgent + " and " +
                                    "SABLON_ANTET.id_ruta= " + (arg.getLong("id_ruta") + 1) + " and " +
                                    "SABLON_ANTET.id_cursa=" + arg.getInt("id_cursa");
                            MySQLDBadapter sqldb = new MySQLDBadapter(getApplicationContext());
                            try {
                                sqldb.open();
                                ResultSet res = sqldb.query(sqlCmd);
                                Log.d("PRO", "extrage sablon : " + sqlCmd);
                                ContentValues cval = new ContentValues();
                                db.beginTransaction();
                                while (res.next()) {
                                    cval.put(Table_TempContinutDocumente._ID, res.getLong("cod_int"));
                                    cval.put(Table_TempContinutDocumente.COL_CANTITATE, res.getDouble("cantitate"));
                                    cval.put(Table_TempContinutDocumente.COL_DIFERENTE, res.getDouble("diferente"));
                                    cval.put(Table_TempContinutDocumente.COL_ID_PRODUS, res.getLong("id_produs"));
                                    cval.put(Table_TempContinutDocumente.COL_S_TIMESTAMP, res.getLong(Table_TempContinutDocumente.COL_S_TIMESTAMP));
                                    db.insert(Table_TempContinutDocumente.TABLE_NAME, null, cval);
                                    cval.clear();
                                }
                                cval.put(Table_Mesaje.COL_ID_MESAJ, ConstanteGlobale.Mesaje.SUCCES_PREIA_SABLON);
                                db.insert(Table_Mesaje.TABLE_NAME, null, cval);
                                db.setTransactionSuccessful();
                                db.endTransaction();
                                res.close();
                                sqldb.close();
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    Thread t = new Thread(runnable);
                    t.start();
                    Log.d("PRO", "ininte de executa");
                    while (t.isAlive()) {
                        // Log.d("PRO","executa");
                    }
                    Log.d("PRO", "dupa executa");

                }
                // se verifica existenta mesajului de succes pentru preluare sablon
                Cursor crs = db.rawQuery("SELECT * FROM " + Table_Mesaje.TABLE_NAME + " WHERE " + Table_Mesaje.COL_ID_MESAJ + " = " +
                        ConstanteGlobale.Mesaje.SUCCES_PREIA_SABLON, null);
                boolean lSucces = (crs.getCount() > 0);
                crs.close();
                db.close();
                colectie.close();
                if (lSucces) {
                    Intent intent = new Intent();
                    intent.setClassName(this, "ro.prosoftsrl.documente" + "." + "DocumenteActivity");
                    intent.putExtra("actiune", nIdAntet == 0 ? "a" : "m");
                    intent.putExtra("_id", arg.getLong("_id")); // id_client
                    // trebuie rezolvata modificarea , cautarea sablonului
                    intent.putExtra("idantet", nIdAntet);
                    if (lComenziOnline) {
                        // daca se ia direct din server , la id_ruta din dialog se adauga 1
                        intent.putExtra("id_ruta", arg.getLong("id_ruta") + 1);
                    } else {
                        intent.putExtra("id_ruta", arg.getLong("id_ruta"));
                    }
                    intent.putExtra("id_cursa", arg.getInt("id_cursa"));
                    intent.putExtra("id_agent", iCodAgent);

                    intent.putExtra("iTLD", Biz.TipListaDenumiri.TLD_SABLON_CERERE);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Nu se poate prelua sablonul . Verificati conexiunea la baza de date", Toast.LENGTH_LONG).show();
                }
            }
            break;
            case ConstanteGlobale.Actiuni_la_documente.ADAUGA_DOCUMENT_DIN_SABLON: {
                // se primeste un sablon si se initializeaza continututl
                long nIdAntet = 0;
                boolean lAreSablon = false;
                boolean lCursaFolosita = false;
                boolean lFaraSablon = false;
                int iAgent = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getApplicationContext().getString(R.string.key_ecran1_id_agent), "0"));
                // se cauta sablon .Daca exista se initializeaza tempcontinut, daca nu ,se goleste
                ColectieAgentHelper colectie = new ColectieAgentHelper(this);
                SQLiteDatabase db = colectie.getWritableDatabase();
                SQLiteDatabase dbread = colectie.getReadableDatabase();
                db.beginTransaction();
                db.execSQL("DELETE FROM " + Table_TempContinutDocumente.TABLE_NAME);
                db.setTransactionSuccessful();
                db.endTransaction();
                // daca este cursa 0 sau 3  se verifica sa nu mai fi fost folosita pe data curenta
                if (arg.getInt("id_cursa") == 0 || arg.getInt("id_cursa") == 3) {
                    // se cauta in tabela de blocari

                    String scmd = "SELECT _ID FROM " + Table_Bloc_Cursa.TABLE_NAME +
                            " WHERE " +
                            Table_Bloc_Cursa.COL_ID_CURSA + "=" + arg.getInt("id_cursa") + " AND " +
                            Table_Bloc_Cursa.COL_ID_PART + "=" + arg.getLong("_id") + " AND " +
                            Table_Bloc_Cursa.COL_ID_RUTA + "=" + arg.getLong("id_ruta") + " AND " +
                            Table_Bloc_Cursa.COL_DATA + "=" + "'" + Siruri.dtos(Siruri.getDateTime()) + "'";
                    Cursor crsbloc = dbread.rawQuery(scmd, null);
                    if (crsbloc.getCount() > 0) {
                        // cursa a mai fost folosita si se nu poate merge in continuare
                        lCursaFolosita = true;
                    }
                    crsbloc.close();
                }
                if (!lCursaFolosita) {
                    Cursor crs = dbread.rawQuery(
                            Biz.getSqlCautaSablon(
                                    arg.getLong("_id"),
                                    iAgent,
                                    arg.getLong("id_ruta"),
                                    arg.getInt("id_cursa")),
                            null);
                    if (crs != null && crs.getCount() > 0) {
                        lAreSablon = true;
                        crs.moveToFirst();
                        nIdAntet = crs.getLong(crs.getColumnIndexOrThrow(Table_Sablon_Antet._ID));
                        Cursor crstemp = Biz.getCursorListaDenumiri(
                                colectie,
                                Biz.TipListaDenumiri.TLD_LINIE_SABLON,
                                nIdAntet,
                                arg.getLong("_id"),
                                true);
                        Log.d("STARTSABLON", "Row: " + crstemp.getCount());

                        if (crstemp.getCount() > 0) {
                            crstemp.moveToFirst();
                            ContentValues cval = new ContentValues();
                            db.beginTransaction();
                            while (!crstemp.isAfterLast()) {
                                //cval.put(Table_TempContinutDocumente._ID,colectie.getNextId(db));
                                Log.d("ALEGSAB", "arecant:" + arg.getBoolean("faracantitati"));
                                if (!arg.getBoolean("faracatitati")) {
                                    Log.d("ALEGSAB", "arecant");
                                    cval.put(Table_TempContinutDocumente.COL_CANTITATE, crstemp.getDouble(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_CANTITATE)));
                                    cval.put(Table_TempContinutDocumente.COL_DIFERENTE, crstemp.getDouble(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_DIFERENTE)));
                                }
                                cval.put(Table_TempContinutDocumente.COL_ID_PRODUS, crstemp.getLong(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_ID_PRODUS)));
                                cval.put(Table_TempContinutDocumente.COL_S_TIMESTAMP, crstemp.getLong(crstemp.getColumnIndex(Table_Sablon_Pozitii.COL_S_TIMESTAMP)));
                                db.insert(Table_TempContinutDocumente.TABLE_NAME, null, cval);
                                cval.clear();
                                crstemp.moveToNext();
                            }
                            db.setTransactionSuccessful();
                            db.endTransaction();
                        }
                        crstemp.close();

                    }
                    crs.close();
                    db.close();
                    dbread.close();
                    colectie.close();
                }
                if (!lCursaFolosita) {
                    if (lAreSablon) {
                    } else {
                        Toast.makeText(getApplicationContext(), "Nu exista sablon predefinit pentru cursa aleasa", Toast.LENGTH_SHORT).show();
                        lFaraSablon = true;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Cursa a mai fost folosita astazi si s-a blocat", Toast.LENGTH_SHORT).show();
                    lFaraSablon = true;
                }
                if (!lFaraSablon) {
                    Intent intent = new Intent();
                    intent.setClassName(this, "ro.prosoftsrl.documente" + "." + "DocumenteActivity");
                    intent.putExtra("iTLD", arg.getInt("iTLD"));
                    intent.putExtra("_id", arg.getLong("_id"));
                    intent.putExtra("id_cursa", arg.getInt("id_cursa"));
                    intent.putExtra("id_ruta", arg.getLong("id_ruta"));

                    startActivity(intent);
                } else {
                    arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ADAUGA_DOCUMENT_FARA_SABLON);
                    transmite_actiuni(null, arg);
                }

            }
            break;
            case ConstanteGlobale.Actiuni_la_documente.ADAUGA_DOCUMENT_FARA_SABLON: {

                ColectieAgentHelper colectie = new ColectieAgentHelper(this);
                colectie.getWritableDatabase().execSQL("DELETE FROM " + Table_TempContinutDocumente.TABLE_NAME);
                colectie.close();
                // porneste activitatea asociata
                Intent intent = new Intent();
                intent.setClassName(this, "ro.prosoftsrl.documente" + "." + "DocumenteActivity");
                intent.putExtra("iTLD", arg.getInt("iTLD"));
                intent.putExtra("_id", arg.getLong("_id"));
                intent.putExtra("id_cursa", -1);
                intent.putExtra("id_ruta", (long) 0);
                startActivity(intent);

            }
            break;

            default:
                break;
        }
    }

    // se primesc coduri de la ferestrele pornite . de exemplu se primeste 1 de la clientiactivity pt a face refresh la lista
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.d("PROS", "Am primit");
            // se transmite comanda de refresh lista la fragmentul listadenumirifragment
            ListaDenumiriFragment frg = (ListaDenumiriFragment) getSupportFragmentManager().findFragmentById(R.id.lista_denumiri_fragment);
            Bundle arg = new Bundle();
            frg.transmite_actiuni_la_fragment(null, arg);
        }
    }
}
