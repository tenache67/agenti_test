package ro.prosoftsrl.rapoarte;

import java.util.Calendar;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Clienti;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Produse;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.FragmentReceiveActionsInterface;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.Siruri;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

public class RapoarteFragment extends Fragment implements FragmentReceiveActionsInterface {
    Context context;
    ActivityReceiveActionsInterface rec;
    String sData1 ;
    String sData2 ;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        context = activity;
        rec = (ActivityReceiveActionsInterface) getActivity();
        Log.d("ATACHRAP", activity.getClass().getName());
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        final View view = inflater.inflate(R.layout.fragment_rapoarte, container, false);
        // initializare valori pt butoanele de data
        sData1 = Siruri.dtos(Calendar.getInstance(), "-");
        sData2 = Siruri.dtos(Calendar.getInstance(), "-");
        final Button btnData1 = (Button) view.findViewById(R.id.btnFrgRapDatePick1);
        btnData1.setText("De la data :" + Siruri.dtoc(Siruri.getDateTime()));
        btnData1.setTag(sData1);
        btnData1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle arg = new Bundle();
                arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ARATA_DIALOG_GENERAL);
                arg.putInt(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_TIP, ConstanteGlobale.TipDialogGeneral.DIALOG_CERE_DATA);
                arg.putInt(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_ID_EXPEDITOR, 1);
                arg.putString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_INI1, btnData1.getTag().toString());
                rec.transmite_actiuni(null, arg);
            }
        });
        final Button btnData2 = (Button) view.findViewById(R.id.btnFrgRapDatePick2);
        btnData2.setText("   la data :" + Siruri.dtoc(Calendar.getInstance()));
        btnData2.setTag(sData2);
        btnData2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle arg = new Bundle();
                arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ARATA_DIALOG_GENERAL);
                arg.putInt(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_TIP, ConstanteGlobale.TipDialogGeneral.DIALOG_CERE_DATA);
                arg.putInt(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_ID_EXPEDITOR, 2);
                arg.putString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_INI1, btnData2.getTag().toString());
                rec.transmite_actiuni(null, arg);
            }
        });


        Button btnTotComenzi = (Button) view.findViewById(R.id.btnFrgListVanz);
        btnTotComenzi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listComenzi();
            }
        });
        // livrare produse / zi
        Button btnTotGrupe = (Button) view.findViewById(R.id.btnFrgListProduse);
        btnTotGrupe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                listVanzareGrupe();
            }
        });
        // desfasurator clienti
        Button btndesfasClienti = (Button) view.findViewById(R.id.btnFrgListdesfasuratorClienti);
        btndesfasClienti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                listDesfasuratorClienti();
            }
        });


        Button btnTotIncas = (Button) view.findViewById(R.id.btnFrgListIncas);
        btnTotIncas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                listIncasari();
            }
        });

        Button btnStoc = (Button) view.findViewById(R.id.btnFrgListStoc);
        btnStoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                listStoc();
            }
        });

        Button btnSoldIni = (Button) view.findViewById(R.id.btnFrgListSoldPornire);
        btnSoldIni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                listSoldPornire();
            }
        });

        Button btnSoldCrt = (Button) view.findViewById(R.id.btnFrgListSoldCurent);
        btnSoldCrt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                listSoldCurent();
            }
        });

        return view;
    }

    // total produse vandute
    private void listComenzi() {
        String[] vCap = {"Denumire", "Cant", "Valoare", "Cod"};
        // se preia cursorul cu vanzarile
        String sqlSir = "SELECT " +
                Table_Produse.TABLE_NAME + "." + Table_Produse.COL_DENUMIRE + "," +
                " round(sum(" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE + "),3) as cant,  " +
                " round(sum(" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE + "*" +
                Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_PRET_CU + "),3) as val , " +
                Table_Produse.TABLE_NAME + "." + Table_Produse.COL_ID_MASTER + " as cod " +
                " FROM " + Table_Antet.TABLE_NAME +
                " join " + Table_Pozitii.TABLE_NAME + " on " + Table_Antet.TABLE_NAME + "." + Table_Antet._ID + "=" +
                Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_ANTET +
                " join " + Table_Produse.TABLE_NAME + " on " + Table_Produse.TABLE_NAME + "." + Table_Produse._ID + " = " +
                Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                " WHERE (" +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_COMANDA + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_BONFISC + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_FACTURA + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_AVIZCLIENT +
                ") AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                " GROUP BY " + Table_Produse.TABLE_NAME + "." + Table_Produse._ID +
                " UNION ALL " +
                " SELECT " +
                "'    -------  '||" + Table_Produse.TABLE_NAME + "." + Table_Produse.COL_DENUMIRE + "," +
                "00000.000 as cant," +
                "000000.00 as val, " +
                Table_Produse.TABLE_NAME + "." + Table_Produse._ID + " as cod " +
                " from " + Table_Produse.TABLE_NAME +
                " where " + Table_Produse.TABLE_NAME + "." + Table_Produse.COL_ID_MASTER + "=0 " +
                " UNION ALL  " +
                " SELECT " +
                "'            TOTAL:' as denumire ," +
                " round(sum(" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE + "),3) as cant,  " +
                " round(sum(" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE + "*" +
                Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_PRET_CU + "),3) as val , " +
                " 0 as cod " +
                " FROM " + Table_Antet.TABLE_NAME +
                " join " + Table_Pozitii.TABLE_NAME + " on " + Table_Antet.TABLE_NAME + "." + Table_Antet._ID + "=" +
                Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_ANTET +
                " join " + Table_Produse.TABLE_NAME + " on " + Table_Produse.TABLE_NAME + "." + Table_Produse._ID + " = " +
                Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                " WHERE (" +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_COMANDA + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_BONFISC + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_FACTURA + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_AVIZCLIENT +
                ") AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                " GROUP BY 1 " +
                " ORDER BY 4,1";
        Log.d("COMENZI", sqlSir);

//		" ORDER BY " +Table_Produse.TABLE_NAME+"."+Table_Produse.COL_ORDONARE+","+
//    	Table_Produse.TABLE_NAME+"."+Table_Produse.COL_DENUMIRE+


        Bundle arg = new Bundle();
        arg.putString("sqlsir", sqlSir);
        arg.putStringArray("caption", vCap);
        arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.RAPOARTE_LIST_TABELA);
        rec.transmite_actiuni(null, arg); // la documenteactivity
    }

    private void listIncasari() {
        String sqlSir = "SELECT " +
                Table_Clienti.TABLE_NAME + "." + Table_Clienti.COL_DENUMIRE + "," +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_NR_DOC + "," +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_INCASAT +
                " FROM " + Table_Antet.TABLE_NAME +
                " JOIN " + Table_Clienti.TABLE_NAME + " on " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_PART + " = " +
                Table_Clienti.TABLE_NAME + "." + Table_Clienti._ID +
                " WHERE " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_INCASAT + " <> 0 " +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'";
        CheckBox chk = (CheckBox) getView().findViewById(R.id.chkFrgRapCuTotIncas);
        if (!chk.isChecked())
            sqlSir = sqlSir + " AND ( " +
                    Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_FACTURA + " OR " +
                    Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_BONFISC + " )";

        sqlSir = sqlSir + " UNION ALL  " +
                " SELECT " +
                "'             TOTAL:' as denumire ," +
                "'   ' as " + Table_Antet.COL_NR_DOC + "," +
                "sum(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_INCASAT + ") as " + Table_Antet.COL_INCASAT +
                " FROM " + Table_Antet.TABLE_NAME +
                " JOIN " + Table_Clienti.TABLE_NAME + " on " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_PART + " = " +
                Table_Clienti.TABLE_NAME + "." + Table_Clienti._ID +
                " WHERE " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_INCASAT + " <> 0  " +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'";
        if (!chk.isChecked())
            sqlSir = sqlSir + " AND ( " +
                    Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_FACTURA + " OR " +
                    Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_BONFISC + " )";
        sqlSir = sqlSir +
                " GROUP BY 1,2 " +
                " ORDER BY 1";

        Bundle arg = new Bundle();
        arg.putString("sqlsir", sqlSir);
        arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.RAPOARTE_LIST_TABELA);
        rec.transmite_actiuni(null, arg); // la documenteactivity

    }

    private void listStoc() {
        String sqlSir = "SELECT " +
                Table_Produse.TABLE_NAME + "." + Table_Produse.COL_DENUMIRE + "," +
                " round(sum(case " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZINC + " then " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_COMANDA + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_BONFISC + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_FACTURA + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZDESC + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZCLIENT + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " end " +
                "),0) as cantitate  " +
                " FROM " + Table_Antet.TABLE_NAME +
                " JOIN " + Table_Pozitii.TABLE_NAME + " ON " +
                Table_Antet.TABLE_NAME + "." + Table_Antet._ID + " = " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_ANTET +
                " JOIN " + Table_Produse.TABLE_NAME + " ON " +
                Table_Produse.TABLE_NAME + "." + Table_Produse._ID + " = " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                " WHERE " + Table_Antet.TABLE_NAME + "." + Table_Antet._ID + " > 0" +
                " AND " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_BLOCAT + "=0 " +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                " GROUP BY " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                " HAVING " +
                " round(sum(case " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZINC + " then " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_COMANDA + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_FACTURA + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_BONFISC + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZDESC + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZCLIENT + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " end " +
                "),0) <>0  " +

                " UNION ALL  " + " SELECT " +
                " '        Total: '" + Table_Produse.COL_DENUMIRE + "," +
                " round(sum(case " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZINC + " then " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_COMANDA + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_FACTURA + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_BONFISC + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZDESC + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " when " + Biz.TipDoc.ID_TIPDOC_AVIZCLIENT + " then -" + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_CANTITATE +
                " end " +
                "),0) as cantitate  " +
                " FROM " + Table_Antet.TABLE_NAME +
                " JOIN " + Table_Pozitii.TABLE_NAME + " ON " +
                Table_Antet.TABLE_NAME + "." + Table_Antet._ID + " = " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_ANTET +
                " JOIN " + Table_Produse.TABLE_NAME + " ON " +
                Table_Produse.TABLE_NAME + "." + Table_Produse._ID + " = " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                " WHERE " + Table_Antet.TABLE_NAME + "." + Table_Antet._ID + " > 0" +
                " AND " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_BLOCAT + "=0 " +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                " GROUP BY 1 " +
                " ORDER BY 1 ";
        Bundle arg = new Bundle();
        arg.putString("sqlsir", sqlSir);
        arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.RAPOARTE_LIST_TABELA);
        rec.transmite_actiuni(null, arg); // la documenteactivity

    }

    private void listVanzareGrupe() {
        String[] vCap = {"Denumire", "Cant", "Valoare"};
        // se preia cursorul cu vanzarile
        String sqlSir = "SELECT " +
                "grupa.denumire ,grupa.cantitate,grupa.valoare " +
                " from " +
                " (select gr._id,gr.denumire,sum(pozitii.cantitate) as cantitate,sum(pozitii.cantitate*pozitii.pret_cu) as valoare " +
                " from pozitii " +
                " inner join produse on pozitii.id_produs=produse._id " +
                " inner join antet on antet._id=pozitii.id_antet " +
                " inner join (select produse._id,produse.denumire from produse where id_master=0 ) gr on produse.id_master=gr._id " +
                " WHERE (" +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_COMANDA + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_BONFISC + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_FACTURA + " or " +
                Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_TIPDOC + "=" + Biz.TipDoc.ID_TIPDOC_AVIZCLIENT +
                ") AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                " group by gr._id " +
                " ) grupa";

        Log.d("COMENZI", sqlSir);

        Bundle arg = new Bundle();
        arg.putString("sqlsir", sqlSir);
        arg.putStringArray("caption", vCap);
        arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.RAPOARTE_LIST_TABELA);
        rec.transmite_actiuni(null, arg); // la documenteactivity
    }

    private void listDesfasuratorClienti() {
        String[] vCap = {"Client", "Nr doc", "Produs", "Cantitate"};
        String sqlSir =
                " select distinct " +
                        Table_Clienti.TABLE_NAME + "." + Table_Clienti.COL_DENUMIRE + " as den_client , " +
                        Table_Antet.TABLE_NAME + "." + Table_Antet.COL_NR_DOC +
                        " FROM " + Table_Antet.TABLE_NAME +
                        " JOIN " + Table_Pozitii.TABLE_NAME + " ON " +
                        Table_Antet.TABLE_NAME + "." + Table_Antet._ID + " = " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_ANTET +
                        " JOIN " + Table_Produse.TABLE_NAME + " ON " +
                        Table_Produse.TABLE_NAME + "." + Table_Produse._ID + " = " + Table_Pozitii.TABLE_NAME + "." + Table_Pozitii.COL_ID_PRODUS +
                        " JOIN " + Table_Clienti.TABLE_NAME + " ON " +
                        Table_Clienti.TABLE_NAME + "." + Table_Clienti._ID + " = " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_ID_PART +
                        " WHERE " + Table_Antet.TABLE_NAME + "." + Table_Antet._ID + " > 0" +
                        " AND " + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_BLOCAT + "=0 " +
                        " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)>='" + sData1 + "'" +
                        " AND " + "SUBSTR(" + Table_Antet.TABLE_NAME + "." + Table_Antet.COL_DATA + ",1,10)<='" + sData2 + "'" +
                        " ORDER BY 1 ";

        Bundle arg = new Bundle();
        arg.putString("sqlsir", sqlSir);
        arg.putStringArray("caption", vCap);
        arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.RAPOARTE_LIST_TABELA);
        rec.transmite_actiuni(null, arg); // la documenteactivity
    }

    private void listSoldPornire() {
        String sqlSir = "";
    }

    private void listSoldCurent() {

    }

    private void setDataLaButon(int iIdResource, String sData, String sMesaj) {
        Button btn = (Button) getView().findViewById(iIdResource);
        btn.setTag(sData);
        btn.setText(sMesaj + "" + Siruri.dtoc(Siruri.cTod(sData)));
    }

    @Override
    public void transmite_actiuni_la_fragment(View view, Bundle arg) {
        // in arg se primenste un id expeditor de genul 1,2 , 3, ... care defineste de fapt o actiune
        switch (arg.getInt(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_ID_EXPEDITOR)) {
            case 1: { // setare data la butonul "de la data"
                setDataLaButon(R.id.btnFrgRapDatePick1,
                        arg.getString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_RETUR1),
                        "De la data: ");
                sData1=arg.getString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_RETUR1);
            }
            break;
            case 2: { // setare data la butonul "de la data"
                setDataLaButon(R.id.btnFrgRapDatePick2,
                        arg.getString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_RETUR1),
                        "la data: ");
                sData2=arg.getString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_RETUR2);
            }
            break;

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("PRO","save");
        outState.putString("data1",sData1);
        outState.putString("data2",sData2);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PRO","create");
        if (savedInstanceState!=null) {
            Log.d("PRO","create1");
            sData1=savedInstanceState.getString("data1");
            sData2=savedInstanceState.getString("data2");
        }
    }
}