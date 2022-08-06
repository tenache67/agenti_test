package ro.prosoftsrl.agenti;
// la creare se pot transmite parametri pentru 
// titlu - titlul dialogului
// text_pozitiv - text pt butonul pozitiv
// text_negativ - text pt butonul negativ
// actiune_pozitiv - int pentru actiunea de la butonul pozitiv
// actiune_negativ - int pt actiunea de la butonul negativ
import ro.prosoftsrl.diverse.Siruri;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class DialogGeneralDaNu extends  DialogFragment {
	int mNum;
	Bundle arg;
	ActivityReceiveActionsInterface actiontrans ; // transmite la DocumenteActivity
	Context context ;
    View view ;
	public interface DialogContinutProdusTransmite {
		public void transmiteDlg (Bundle arg);
	}
	
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
	
	public static DialogGeneralDaNu newinstance (Bundle fb) {
		DialogGeneralDaNu f = new DialogGeneralDaNu();
		f.setArguments(fb);
		return f;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.arg=getArguments();
		this.mNum=arg.getInt("tipdialog",0);

        // tipul 0 - are numai butoanele
        // tipul 1 - are o caseta de parola

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		//builder.setTitle(arg.getString("titlu"));
		builder.setMessage(arg.getString("titlu"));
		String msgPoz=arg.getString("text_pozitiv")==null ? "Accepta" : arg.getString("text_pozitiv") ; 
		String msgNeg=arg.getString("text_negativ")==null ? "Renunta" : arg.getString("text_negativ") ;
		String msgCanc=arg.getString("text_neutru")==null ? "" : arg.getString("text_neutru") ;
        LayoutInflater inflater= getActivity().getLayoutInflater();
        // in functie de tipul dialogului se pot primi dicverse valori prin arg
        // cheile pentru valori sunt denumite : valoare_ini1 , 2 , 3 ///
        switch (this.mNum) {
            case 1: { // cere parola
                view=inflater.inflate(R.layout.dialog_cere_parola, null);
                builder.setView(view);
                arg.putString("valoare_retur1","");
            }
            break;
            case 2: { // cere data
                // se primeste valoare_ini1 cu data prestabilita
                Calendar cdata=Calendar.getInstance();
                if (arg.getString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_INI1)!=null) {
                    // data este in format yyyymmdd
                    cdata= Siruri.cTod(arg.getString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_INI1));
                }
                view=inflater.inflate(R.layout.dialog_cere_data, null);
                builder.setView(view);
                DatePicker dateView = (DatePicker) view.findViewById(R.id.dateDlgCereData);
                dateView.updateDate(cdata.get(Calendar.YEAR),cdata.get(Calendar.MONTH),cdata.get(Calendar.DAY_OF_MONTH));
                arg.putString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_RETUR1, Siruri.dtos(cdata));
            }
            break;
        }
		builder
			.setPositiveButton(msgPoz, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					// daca in arg exista deja eticheta pt actiune nu se mai trimite alta

                    switch (mNum) {
                        case 1: { // parola
                            EditText edt = (EditText) view.findViewById(R.id.edtDlgParola);
                            if (edt.getText()!=null) {
                                arg.putString("valoare_retur1",edt.getText().toString());
                            } else {
                                arg.putString("valoare_retur1","");
                            }
                        }
                        break;
                        case 2: { //data
                            DatePicker dateView = (DatePicker) view.findViewById(R.id.dateDlgCereData);
                            Calendar data= Calendar.getInstance();
                            data.set(dateView.getYear(),dateView.getMonth(),dateView.getDayOfMonth());
                            arg.putString(ConstanteGlobale.TipDialogGeneral.DIALOG_ETICHETA_VALOARE_RETUR1, Siruri.dtos(data));
                        }
                        break;

                    }

					if (arg.get("actiune_pozitiv")==null)
						arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_POZITIV);
					else 
						arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE,arg.getInt("actiune_pozitiv"));
					actiontrans.transmite_actiuni(null, arg); // la Activty host
				}
			})
			.setNegativeButton(msgNeg, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (arg.get("actiune_negativ")==null) 
						arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_NEGATIV);
					else
						arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, arg.getInt("actiune_negativ"));
					actiontrans.transmite_actiuni(null, arg); // la Activty host
				}
			});
			if (!msgCanc.equals(""))
				builder.setNeutralButton(msgCanc, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (arg.get("actiune_neutru")==null) 
							arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_NEUTRU);
						else
							arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, arg.getInt("actiune_neutru"));
						actiontrans.transmite_actiuni(null, arg); // la Activty host
					}
				});
		return builder.create();
	}
	
}
