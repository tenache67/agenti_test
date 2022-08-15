/**
 *
 */
package ro.prosoftsrl.agenti;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

/**
 * @author Traian
 *
 */
public class ListaDenumiriFragment extends ListFragment implements FragmentComunicatorInterface, FragmentReceiveActionsInterface {
    SimpleCursorAdapter adapter;
    Cursor crs;
    ColectieAgentHelper colectie;
    Context context;
    int iTLD = 0;
    ActivityComunicatorInterface actCom;
    ActivityReceiveActionsInterface actRec;
    Bundle fb = new Bundle();

    @Override
    public int primeste_iTLD() {
        // TODO Auto-generated method stub
        return this.iTLD;
    }


    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        this.context = (Context) activity;
        actCom = (ActivityComunicatorInterface) this.context;
        actRec = (ActivityReceiveActionsInterface) this.context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        this.iTLD = actCom.transmite_iTLD();
        Log.d("FRAGMENT", "" + this.iTLD);
//		// optiuni pt meniu actiuni
//		this.fb.putStringArray("opt",Biz.getActiuniListaDenumiri(iTLD, 0));
//		// idurile actiunilor, pt selectie
//		//this.fb.putLongArray("ids", new long[] {1});
//		this.fb.putLongArray("ids", Biz.getIdsActiuni(iTLD));
        this.fb.putString("title", "Actiuni");
        this.fb.putInt("iTLD", this.iTLD);
        // se trece tipul dialogului in functie de lista de denumiri din care se apeleaza

        switch (this.iTLD) {
            case Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC:
                this.fb.putInt("tipdialog", ConstanteGlobale.TipDialogListaDenumiri.OPT_INC_DESC);
                break;
            case Biz.TipListaDenumiri.TLD_CLIENTI:
                this.fb.putInt("tipdialog", ConstanteGlobale.TipDialogListaDenumiri.OPT_LISTA_CLIENTI);
                break;
            case Biz.TipListaDenumiri.TLD_PRODUSE:
                Log.d("PRO", "Opt produse");
                this.fb.putInt("tipdialog", ConstanteGlobale.TipDialogListaDenumiri.OPT_LISTA_PRODUSE);
                break;
            default:
                this.fb.putInt("tipdialog", ConstanteGlobale.TipDialogListaDenumiri.OPT_LISTA_CLIENTI);
                break;
        }

        colectie = new ColectieAgentHelper(getActivity());
        adapter = new SimpleCursorAdapter(getActivity(),
                Biz.getIdRowListaDenumiri(this.iTLD, 0),
                Biz.getCursorListaDenumiri(colectie, this.iTLD, 0, 0, Biz.pretCuTva(context)),
                Biz.getArrayColoaneListaDenumiri(this.iTLD, 0),
                Biz.getArrayIdViewListaDenumiri(this.iTLD, 0),
                1);
        //ListView listview= getListView();
        //listview.setClickable(true);
        setListAdapter(adapter);
        ///Pt a afisa bara de cautare
        if (iTLD != Biz.TipListaDenumiri.TLD_PRODUSE && iTLD != Biz.TipListaDenumiri.TLD_CLIENTI) {
            EditText etCautare = ListaDenumiriActivity.etCautare;
            etCautare.setVisibility(View.INVISIBLE);
            Log.e("huh", String.valueOf(iTLD));
        }
    }

    // se apeleaza la click din lista de denumiri
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Cursor item = (Cursor) l.getItemAtPosition(position);
        long nIdMaster = item.getLong(item.getColumnIndexOrThrow("_id"));
        Log.d("ONCLICK", " " + nIdMaster);
        crs = Biz.getCursorListaDenumiri(colectie, this.iTLD, nIdMaster, 0, Biz.pretCuTva(context));
        if (crs.getCount() > 0) {
            Log.d("ONCLICK", "Row: " + crs.getCount());
            Log.d("PRO", "adapter:" + adapter.getCount());
            adapter.changeCursor(crs);
            adapter.notifyDataSetChanged();
            Log.d("PRO", "adapter:" + adapter.getCount());
        } else {
            Log.d("PRO", "transmite id=" + nIdMaster);
            // transmite idul randului la dialog
            this.fb.putLong("_id", nIdMaster);

            // optiuni pt meniu actiuni
            this.fb.putStringArray("opt", Biz.getActiuniListaDenumiri(iTLD, nIdMaster, context));
            // idurile actiunilor, pt selectie
            //this.fb.putLongArray("ids", new long[] {1});
            this.fb.putLongArray("ids", Biz.getIdsActiuni(iTLD, nIdMaster, context));


            switch (iTLD) {
                case Biz.TipListaDenumiri.TLD_AGENTI:
                    Bundle arg = new Bundle();
                    arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ALEGE_AGENT);
                    arg.putLong("_id", nIdMaster);
                    actRec.transmite_actiuni(null, arg);
                    break;
                default:
                    // dialogul care afiseaza optiunile care vin din click pe o denumire din listadenumiri
                    arataDialog(0);
                    break;
            }
            crs.close();
        }
    }

    public void arataDialog(int id) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("listdialog");
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }
        DialogListaDenumiri dlg = DialogListaDenumiri.newInstance(this.fb);
        dlg.show(ft, "listdialog");
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (crs != null) {
            crs.close();
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.lista_denumiri, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //optiunile din bara de meniu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.action_back_lista_denumiri: {
                crs = Biz.getCursorListaDenumiri(colectie, this.iTLD, 0, 0, Biz.pretCuTva(context));
                adapter.changeCursor(crs);
            }
            break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public int primeste_id() {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public void transmite_actiuni_la_fragment(View view, Bundle arg) {
        // primeste comanda de refresh lista de la listadenumiriactivity
        ListView listview = getListView();

        int nPos = listview.getFirstVisiblePosition();
        colectie = new ColectieAgentHelper(getActivity());
        adapter = new SimpleCursorAdapter(getActivity(),
                Biz.getIdRowListaDenumiri(this.iTLD, 0),
                Biz.getCursorListaDenumiri(colectie, this.iTLD, 0, 0, Biz.pretCuTva(context)),
                Biz.getArrayColoaneListaDenumiri(this.iTLD, 0),
                Biz.getArrayIdViewListaDenumiri(this.iTLD, 0),
                1);
        //ListView listview= getListView();
        //listview.setClickable(true);
        setListAdapter(adapter);
        try {
            listview.setSelection(nPos);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

	

