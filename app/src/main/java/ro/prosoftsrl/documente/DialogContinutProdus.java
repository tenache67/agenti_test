package ro.prosoftsrl.documente;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenti.ActivityReceiveActionsInterface;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.Siruri;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;


public class DialogContinutProdus extends DialogFragment {
	int mNum; // tipul dialogului
	ActivityReceiveActionsInterface actiontrans ; // transmite la DocumenteActivity
	Context context ;
	
//	public interface DialogContinutProdusTransmite {
//		public void transmiteDlg (Bundle arg);
//	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.context=activity;
		if (activity instanceof ActivityReceiveActionsInterface)
			actiontrans=(ActivityReceiveActionsInterface) activity;
		else
			throw new ClassCastException(activity.toString()+" trebuie implementata interfata de la ActivityReceiveActionsInterface" );
	}
	
	static DialogContinutProdus newinstance (Bundle fb) {
		DialogContinutProdus f = new DialogContinutProdus();
		f.setArguments(fb);
		return f;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.mNum=getArguments().getInt("tipdialog",0);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater= getActivity().getLayoutInflater();
//        String versiune=PreferenceManager.getDefaultSharedPreferences(context)
//                .getString(getString(R.string.key_ecran5_varianta), "betty");
// daca este activata modificarea de pret apare activa si caseta cu modificarea pretului in care ste preluat pretul curent nu cel
        // de la discount
		switch (mNum) {
            case 0: // tip dialog pentru cantitatea de la continut apelat din lista de produse
            {
                View view = inflater.inflate(R.layout.dialog_produs_document, null);
                builder.setView(view)
                        .setPositiveButton("Accepta", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                AlertDialog dlg = (AlertDialog) dialog;
                                EditText edt = (EditText) dlg.findViewById(R.id.txtDlgProdContinutCant);
                                edt.requestFocus();
                                // 30.05.2017 - am pus butonul pentru bonus invizibil ca sa nu se mai poata da
                                // vezi si mai jos ca este fortat argumentul pt este bonus ca sa fie 0
                                CheckBox chk = (CheckBox) dlg.findViewById(R.id.chkDlgProdContinut);
                                // deja in argumente s-a primit id_produs
                                Bundle arg = getArguments();
                                Log.d("INDIALOG", edt.toString() + " " + edt.getText().toString());
                                arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE,
                                        ConstanteGlobale.Actiuni_la_documente.SET_CANTITATE_LA_DOCUMENT);
                                Double nCant = (double) 0;
                                try {
                                    nCant = Double.parseDouble(edt.getText().toString());
                                } catch (NumberFormatException e) {
                                    // TODO Auto-generated catch block
                                }
                                arg.putDouble("cantitate", nCant);
                                EditText pret = (EditText)  dlg.findViewById(R.id.txtDlgProdContinutPret);
                                Double nPret=Siruri.getDoubleDinString(pret.getText().toString());
                                arg.putDouble("pret_cu",nPret  );
                                // 30.05.2017 - am pus butonul pentru bonus invizibil ca sa nu se mai poata da
                                // am fortat ca argumentul pt bonus sa fie 0 in permanenta
                                // daca activez linia de mai jos bonusul este inactiv
                                arg.putInt("este_bonus", chk.isChecked() ? 1 : 0);
                                // daca activez linia de mai jos bonusul nu este activ
                                //arg.putInt("este_bonus", 0); // trebuia sa fie linia de mai sus

                                RadioGroup rdg= (RadioGroup) dlg.findViewById(R.id.rdbGrdlgProd);
                                long nIdFA=4;
                                switch (rdg.getCheckedRadioButtonId()) {
                                    case R.id.rdbDlgProdFA1:
                                        nIdFA=1;
                                        break;
                                    case R.id.rdbDlgProdFA2:
                                        nIdFA=2;
                                        break;
                                    case R.id.rdbDlgProdFA3:
                                        nIdFA=3;
                                        break;
                                }
                                arg.putLong("forma_ambalare_selectata", nIdFA);
                                // se transmite mai departe
                                actiontrans.transmite_actiuni(null, arg); // la DocumenteActivty
                            }
                        })
                        .setNegativeButton("Renunta", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        });
                // se cauta argumentul pentru activarea bonusului
                // bonusul se activeaza numai la apelarea din lista de produse
                Boolean lActBonus = getArguments().getBoolean("bonus", false);
                CheckBox chk = (CheckBox) view.findViewById(R.id.chkDlgProdContinut);
                chk.setEnabled(lActBonus);
                chk.setVisibility(lActBonus ? View.VISIBLE : View.GONE);
                // pentru pret
                EditText pret =(EditText) view.findViewById(R.id.txtDlgProdContinutPret) ;
                pret.setText(( Double.toString(getArguments().getDouble("pret_cu"))));
                LinearLayout layPret =(LinearLayout) view.findViewById(R.id.layDlgModPret);
                if (!getArguments().getBoolean("modifica pret"))
                    layPret.setVisibility(View.GONE);

                // se verifica daca are alte forme
                RadioGroup rdg= (RadioGroup) view.findViewById(R.id.rdbGrdlgProd);
                if (getArguments().getBoolean("forme_ambalare", false)) {
                    rdg.setVisibility(View.VISIBLE);
                    if (getArguments().getLong(ColectieAgentHelper.Table_TempContinutDocumente.COL_ID_FA1,0)==1) {
                        rdg.check(R.id.rdbDlgProdFA1);
                    } else if (getArguments().getLong(ColectieAgentHelper.Table_TempContinutDocumente.COL_ID_FA1,0)==2) {
                        rdg.check(R.id.rdbDlgProdFA2);
                    } else if (getArguments().getLong(ColectieAgentHelper.Table_TempContinutDocumente.COL_ID_FA1,0)==3) {
                        rdg.check(R.id.rdbDlgProdFA3);
                    }
                    EditText cant =(EditText) view.findViewById(R.id.txtDlgProdContinutPret) ;
                    cant.requestFocus();

                }

            }

            break;
            case 1: // tip dialog pentru cantitate in linia de produs din document sau sablon
                View view = inflater.inflate(R.layout.dialog_produs_document, null);
                builder.setView(view)
                        .setPositiveButton("Accepta", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                AlertDialog dlg = (AlertDialog) dialog;
                                EditText edtdif = (EditText) dlg.findViewById(R.id.txtDlgprodDiferente);
                                EditText edt = (EditText) dlg.findViewById(R.id.txtDlgProdContinutCant);
                                EditText pret=(EditText) dlg.findViewById(R.id.txtDlgProdContinutPret);
                                edt.requestFocus();

                                Bundle arg = getArguments();
                                Log.d("INLINIE", edt.toString() + " " + edt.getText().toString());
                                arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.SET_CANTITATE_IN_LINIE);
                                Double nCant = Siruri.getDoubleDinString(edt.getText().toString());
                                Double nDif = Siruri.getDoubleDinString(edtdif.getText().toString());
                                Double nPret=Siruri.getDoubleDinString(pret.getText().toString());
                                if (edt.getText().toString().equals(""))
                                    arg.putBoolean("faracant", true);
                                if (edtdif.getText().toString().equals("")) {
                                    arg.putBoolean("faradif", true);
                                }
                                if (pret.getText().toString().equals("")) {
                                    arg.putBoolean("farapret", true);
                                }

                                arg.putDouble("cantitate", nCant);
                                arg.putDouble("pret_cu",nPret  );

                                arg.putDouble("diferente", nDif);
                                CheckBox chk = (CheckBox) dlg.findViewById(R.id.chkDlgProdContinut);
                                arg.putInt("este_bonus", chk.isChecked() ? 1 : 0);
                                RadioGroup rdg= (RadioGroup) dlg.findViewById(R.id.rdbGrdlgProd);
                                long nIdFA=4; // implicit
                                switch (rdg.getCheckedRadioButtonId()) {
                                    case R.id.rdbDlgProdFA1:
                                        nIdFA=1;
                                        break;
                                    case R.id.rdbDlgProdFA2:
                                        nIdFA=2;
                                        break;
                                    case R.id.rdbDlgProdFA3:
                                        nIdFA=3;
                                        break;
                                }
                                arg.putLong("forma_ambalare_selectata", nIdFA);
                                // se transmite mai departe
                                actiontrans.transmite_actiuni(null, arg); // la DocumenteActivty
                            }
                        })
                        .setNegativeButton("Renunta", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        });
                LinearLayout lay =(LinearLayout) view.findViewById(R.id.layDlgProdDiferente);
                EditText edtdif = (EditText) view.findViewById(R.id.txtDlgprodDiferente);
                TextView txtdif = (TextView) view.findViewById(R.id.lblDlgProdDiferente);
                if (getArguments().getBoolean("diferente", false)) {
                    lay.setVisibility(View.VISIBLE);
                    edtdif.setVisibility(View.VISIBLE);
                    txtdif.setVisibility(View.VISIBLE);
                } else {
                    lay.setVisibility(View.GONE);
                    edtdif.setVisibility(View.GONE);
                    txtdif.setVisibility(View.GONE);
                }
                // se cauta argumentul pentru activarea bonusului
                // la apelarea din pozitii bonusul nu este activ
                // in arg se primeste valoarea pt bonus din camp
                CheckBox chk = (CheckBox) view.findViewById(R.id.chkDlgProdContinut);
                chk.setChecked(getArguments().getInt(ColectieAgentHelper.Table_TempContinutDocumente.COL_C_ESTE_BONUS,0)==1);
                chk.setEnabled(false);
                chk.setVisibility(View.VISIBLE);
                // se verifica daca are alte forme si se activeaza daca este cazul forma primita in arg
                RadioGroup rdg= (RadioGroup) view.findViewById(R.id.rdbGrdlgProd);
                if (getArguments().getBoolean("forme_ambalare", false)) {
                    rdg.setVisibility(View.VISIBLE);
                    if (getArguments().getLong(ColectieAgentHelper.Table_TempContinutDocumente.COL_ID_FA1,0)==1) {
                        rdg.check(R.id.rdbDlgProdFA1);
                    } else if (getArguments().getLong(ColectieAgentHelper.Table_TempContinutDocumente.COL_ID_FA1,0)==2) {
                        rdg.check(R.id.rdbDlgProdFA2);
                        } else if (getArguments().getLong(ColectieAgentHelper.Table_TempContinutDocumente.COL_ID_FA1,0)==3) {
                            rdg.check(R.id.rdbDlgProdFA3);
                    }
                }
                //pentru preturi
                EditText pret =(EditText) view.findViewById(R.id.txtDlgProdContinutPret) ;
                pret.setText(( Double.toString(getArguments().getDouble("pret_cu"))));
                LinearLayout layPret =(LinearLayout) view.findViewById(R.id.layDlgModPret);
                if (!getArguments().getBoolean("modifica pret"))
                    layPret.setVisibility(View.GONE);
                EditText cant =(EditText) view.findViewById(R.id.txtDlgProdContinutPret) ;
                cant.requestFocus();

                break;
		default:

			break;
		}

		return builder.create();
	}
	
}
