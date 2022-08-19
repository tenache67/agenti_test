package ro.prosoftsrl.documente;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_TempContinutDocumente;
import ro.prosoftsrl.agenti.ActivityComunicatorInterface;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.FragmentReceiveActionsInterface;
import ro.prosoftsrl.agenti.ListaDenumiriFragment;
import ro.prosoftsrl.agenti.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ListaProdusePtContinutFragment extends ListFragment implements FragmentReceiveActionsInterface {
    Context context;
    ActivityComunicatorInterface act;
    ActivityReceiveActionsInterface rec;
    Cursor crsArticole;
    ColectieAgentHelper colectie;
    SimpleCursorAdapter adapter;
    Cursor crs;
    long nIdMaster;
    long nIdclient;
    Bundle fb;
    Boolean lPVcuTVA;
    int nTipTva;

    public EditText etCautare2;
    public List<String> arraySpinner = new ArrayList<String>();
    ArrayAdapter<String> adapterSpin;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        this.act = (ActivityComunicatorInterface) getActivity();
        this.nIdclient = this.act.transmite_id_client();
        this.rec = (ActivityReceiveActionsInterface) getActivity();
        this.context = activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        this.lPVcuTVA = Biz.pretCuTva(context);
        View view = inflater.inflate(R.layout.fragment_continut_documente, container, false);
        view.setScrollbarFadingEnabled(false);
        // se cauta in argumentele primite tipul de tva pt articole
        nTipTva = getArguments().getInt(Table_Produse.COL_COTA_TVA, 0);
        Log.d("APELLISTA", "Tip tva:" + nTipTva);

        //buton activare alt fragment
        Button btnInchide = (Button) view.findViewById(R.id.btnFrgContInchide);
        btnInchide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Bundle arg = new Bundle();
                arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.INCHIDE_LISTA_PRODUSE_CONTINUT);
                rec.transmite_actiuni(null, arg);
            }
        });
        Button btnUp = (Button) view.findViewById(R.id.btnFrgContUpLevel);
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (nIdMaster != 0) {
                    nIdMaster = 0;
                    crs = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT, nIdMaster, nIdclient, lPVcuTVA, nTipTva);
                    adapter.changeCursor(crs);
                }
            }
        });
        this.colectie = new ColectieAgentHelper(this.context);
        this.nIdMaster = 0;
        adapter = new SimpleCursorAdapter(this.context,
                Biz.getIdRowListaDenumiri(Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT, this.nIdMaster),
                Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT, this.nIdMaster, this.nIdclient, this.lPVcuTVA, nTipTva),
                Biz.getArrayColoaneListaDenumiri(Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT, this.nIdMaster),
                Biz.getArrayIdViewListaDenumiri(Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT, this.nIdMaster),
                1);
        setListAdapter(adapter);

        InitSpinnerRezultate(view);
        InitCautare(view);

        return view;
    }

    private void InitSpinnerRezultate(View v) {
        Spinner sp = (Spinner) v.findViewById(R.id.spinnerOptiuni2);
        adapterSpin = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_item, arraySpinner);


        adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapterSpin);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int z, long l) {
                int childCount = getListView().getAdapter().getCount();
                Cursor crs = (Cursor) getListView().getItemAtPosition(1);
                crs.moveToFirst();
                String cautareString = ((String) sp.getSelectedItem()).toLowerCase();
                Log.d("EU CAUT", cautareString);
                for (int i = 0; i < childCount; i++) {
                    @SuppressLint("Range") String numeBase = crs.getString(crs.getColumnIndex(Table_Produse.COL_DENUMIRE));
                    numeBase = numeBase.toLowerCase();
                    if (numeBase.equals(cautareString)) {

                        getListView().setSelection(i);
                        Log.d("GASIT", "YAY");

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    private void InitCautare(View v) {
        etCautare2 = (EditText) v.findViewById(R.id.editTextCautare2);
        etCautare2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int curLength, int afterLength) {

                adapterSpin.clear();
                Spinner sp = (Spinner) v.findViewById(R.id.spinnerOptiuni2);

                String cautareString = etCautare2.getText().toString().toLowerCase();
                //getListView()

                int childCount = getListView().getAdapter().getCount();

                if (childCount == 0) //daca nu exista produse
                    return;
                if (cautareString.length() < 3) //daca sunt mai putin de 3 litere in cautare
                    return;
                if (afterLength < curLength) //daca se da backspace nu cauta
                    return;

                Cursor crs = (Cursor) getListView().getItemAtPosition(1);

                crs.moveToFirst();
                // @SuppressLint("Range") String data = crs.getString(crs.getColumnIndex("name"));
                for (int i = 0; i < childCount; i++) {
                    @SuppressLint("Range") String numeBase = crs.getString(crs.getColumnIndex(Table_Produse.COL_DENUMIRE));
                    numeBase = numeBase.toLowerCase();
                    StringTokenizer stringTokenizer = new StringTokenizer(numeBase, " ."); //cauta fiecare cuvant al liniei

                    while (stringTokenizer.hasMoreTokens()) {
                        if (stringTokenizer.nextToken().startsWith(cautareString) && !cautareString.isEmpty()) {
                            //rl.setVisibility(View.INVISIBLE);
                            //frg.getListView().setSelection(i);

                            adapterSpin.add(numeBase);
                            sp.performClick();


                            Log.d("FOUND", "YEEES");
                            break;
                        }

                    }

                    crs.moveToNext();

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor item = (Cursor) l.getItemAtPosition(position);
        Long nIdFa1 = item.getLong(item.getColumnIndexOrThrow(Table_TempContinutDocumente.COL_ID_FA1));
        Long nIdM = item.getLong(item.getColumnIndexOrThrow("_id"));
        crs = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT, nIdM, nIdclient, lPVcuTVA, nTipTva);
        if (crs.getCount() > 0) {
            this.nIdMaster = nIdM;
            adapter.changeCursor(crs);
        } else {
            // transmite idul randului la dialog
            boolean lForme = (
                    PreferenceManager.getDefaultSharedPreferences(context).getString("key_ecran5_varianta", "").toUpperCase().equals("SOROLI") ||
                            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_ecran5_forme_ambalare", false));

            this.fb = new Bundle();
            Log.d("PRO", "1");
            this.fb.putLong(Table_TempContinutDocumente.COL_ID_FA1, nIdFa1);
            Log.d("PRO", "2");
            this.fb.putInt("tipdialog", 0);
            this.fb.putLong("id_produs", nIdM); // idul articolului
            ToggleButton tog = (ToggleButton) (getView().findViewById(R.id.togFrgDocActBonus));
            this.fb.putBoolean("bonus", tog.isChecked());
            this.fb.putBoolean("forme_ambalare", lForme);
            // modif de pret este doar pt FLORISGIN si SEMROPACK
            boolean lModPret = false;
            String sCodFiscal = PreferenceManager.getDefaultSharedPreferences(context).
                    getString("key_ecran4_cf", "");
            if (sCodFiscal.toLowerCase().contains("27670851")) {
                // florisgin
                lModPret = true;
            } else if (sCodFiscal.toLowerCase().contains("33984123")) {
                // semrompack
                lModPret = true;
            }
            this.fb.putBoolean("modifica pret", lModPret);
            int nCol = item.getColumnIndexOrThrow(Table_Produse.COL_PRET_CU);
            Double nPret = item.getDouble(nCol);
            this.fb.putDouble("pret_cu", nPret);
            arataDialog(this.fb);
        }
    }


    public void arataDialog(Bundle arg) {
        // dialogul primeste in bundle informatia despre id_produs
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("listdialog");
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }
        DialogContinutProdus dlg = DialogContinutProdus.newinstance(arg);
        dlg.show(ft, "listdialog");
        //dlg.getView().findViewById(R.id.txtDlgProdContinutCant).requestFocus();

    }

    public void inchide() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @SuppressLint("Range")
    @Override
    public void transmite_actiuni_la_fragment(View view, Bundle arg) {
        // TODO Auto-generated method stub
        // se actualizeaza tabela temporara de continut
        // se face actualizarea in fiunctie de id_produs si este_bonus
        // se face si butonul de bonus inactiv
        // _id din temp sunt preluate din pozitii . Pt pozitiile noi se genereaza iduri noi
        // vine si pretul
        Long nIdProdus = arg.getLong("id_produs", 0);
        double nCant = arg.getDouble("cantitate", 0);
        double nPret = arg.getDouble("pret_cu");
        Log.d("SETCANT", "cant:" + nCant);
        int nBonus = arg.getInt("este_bonus", 0);
        SQLiteDatabase db = colectie.getWritableDatabase();
        db.beginTransaction();
//		if (nCant==0) {
//				db.delete(Table_TempContinutDocumente.TABLE_NAME, " id_produs="+nIdProdus+" and este_bonus="+nBonus , null);
//		}
//		else {
        Cursor crstemp = db.rawQuery("select * from " + Table_TempContinutDocumente.TABLE_NAME + " WHERE " + " id_produs=" + nIdProdus + " and este_bonus=" + nBonus, null);
        ContentValues cVal = new ContentValues();
        cVal.put(Table_TempContinutDocumente.COL_ID_FA1, arg.getLong("forma_ambalare_selectata", 0));
        cVal.put(Table_TempContinutDocumente.COL_CANTITATE, nCant);
        cVal.put(Table_TempContinutDocumente.COL_PRET_CU, nPret);
        cVal.put(Table_TempContinutDocumente.COL_C_ESTE_BONUS, nBonus);
        cVal.put(Table_TempContinutDocumente.COL_ID_PRODUS, nIdProdus);
        Log.d("SETCANT", "Cant=" + nCant);
        if (crstemp.getCount() != 0) {
            crstemp.moveToFirst();
            cVal.put(Table_TempContinutDocumente._ID,
                    crstemp.getLong(crstemp.getColumnIndexOrThrow(Table_TempContinutDocumente._ID)));
            cVal.put(Table_TempContinutDocumente.COL_S_TIMESTAMP,
                    crstemp.getLong(crstemp.getColumnIndex(Table_TempContinutDocumente.COL_S_TIMESTAMP)));
            db.replace(Table_TempContinutDocumente.TABLE_NAME, null, cVal);
            Log.d("SETCANT", "Repl:" + nCant);

        } else {
            db.insert(Table_TempContinutDocumente.TABLE_NAME, null, cVal);
            Log.d("SETCANT", "Inse:" + nCant);

        }
        crstemp.close();
//		}
//			ContentValues cVal=Biz.getUpdateContinutDocument(crstemp, nIdProdus, nCant,nBonus);			
        db.setTransactionSuccessful();
        db.endTransaction();
        // se dezactiveaza butonul de bonus
        ToggleButton tog = (ToggleButton) getView().findViewById(R.id.togFrgDocActBonus);
        tog.setChecked(false);
        crs = Biz.getCursorListaDenumiri(colectie, Biz.TipListaDenumiri.TLD_PRODUSE_PT_CONTINUT, this.nIdMaster, this.nIdclient, Biz.pretCuTva(context), nTipTva);
        adapter.changeCursor(crs);
    }


}
