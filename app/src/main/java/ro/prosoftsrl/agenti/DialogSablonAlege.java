package ro.prosoftsrl.agenti;

import java.util.Calendar;

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
import android.widget.Spinner;

public class DialogSablonAlege extends DialogFragment {
	int mNum; // tipul dialogului
	ActivityReceiveActionsInterface actiontrans ; // transmite la activity
	Context context ;
	Bundle arg;
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

	static DialogSablonAlege newinstance (Bundle fb) {
		DialogSablonAlege f = new DialogSablonAlege();
		f.setArguments(fb);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.arg=getArguments();
		this.mNum=arg.getInt("tipdialog",0);
		String msgPoz=arg.getString("text_pozitiv")==null ? "Accepta" : arg.getString("text_pozitiv") ; 
		String msgNeg=arg.getString("text_negativ")==null ? "Renunta" : arg.getString("text_negativ") ;
		String msgCanc=arg.getString("text_neutru")==null ? "" : arg.getString("text_neutru") ;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(arg.getString("titlu"));
		LayoutInflater inflater= getActivity().getLayoutInflater();
		switch (mNum) {
		case 0: // tip dialog pentru alegere sablon
		{
			Log.d("INSABLON","mNum="+mNum);
			View view=inflater.inflate(R.layout.dialog_sablon_alege, null);
			builder.setView(view)
			.setPositiveButton(msgPoz, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AlertDialog dlg = (AlertDialog) dialog;
					Spinner spnZi=(Spinner) dlg.findViewById(R.id.spnZiDlgSablonAlege);
					Spinner spnCursa=(Spinner) dlg.findViewById(R.id.spnCursaDlgSablonAlege);
					arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ALEGE_SABLON);
					arg.putLong("id_ruta", (long) spnZi.getSelectedItemPosition());
					arg.putInt("id_cursa", spnCursa.getSelectedItemPosition());
					arg.putLong("_id", arg.getLong("_id"));
					// se transmite mai departe
					Log.d("INSABLON","id ruta="+(long) spnZi.getSelectedItemPosition());
					Log.d("INSABLON","id cursa="+ spnCursa.getSelectedItemPosition());
					
					actiontrans.transmite_actiuni(null, arg); // la ListaDenumiriActivty
				}
			})
			.setNegativeButton(msgNeg, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.DIALOG_GENERAL_NEGATIV);					
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
			CheckBox chk=(CheckBox) view.findViewById(R.id.chkFaraCantDlgsablonAlege);
			chk.setVisibility(View.INVISIBLE);
			// se initializaeaza spinnerul pt zi cu ziua curenta
			// duminica are valoarea 1
			int nZi=Siruri.getDateTime().get(Calendar.DAY_OF_WEEK)+1+5;
			if (nZi>6) nZi=nZi-7;
			Spinner spnZi=(Spinner) view.findViewById(R.id.spnZiDlgSablonAlege);
			spnZi.setSelection(nZi);
			
			}
			break;
		case 1: // tip dialog pentru initializare document din sablon
			Log.d("INSABLON","mNum="+mNum);
			{
			View view=inflater.inflate(R.layout.dialog_sablon_alege, null);
			builder.setView(view)
			.setPositiveButton(msgPoz, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AlertDialog dlg = (AlertDialog) dialog;
					Spinner spnZi=(Spinner) dlg.findViewById(R.id.spnZiDlgSablonAlege);
					Spinner spnCursa=(Spinner) dlg.findViewById(R.id.spnCursaDlgSablonAlege);
					CheckBox chk=(CheckBox) dlg.findViewById(R.id.chkFaraCantDlgsablonAlege);
					arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ADAUGA_DOCUMENT_DIN_SABLON);
					arg.putLong("id_ruta", (long) spnZi.getSelectedItemPosition());
					arg.putInt("id_cursa", spnCursa.getSelectedItemPosition());
					arg.putLong("_id", arg.getLong("_id"));
					arg.putBoolean("faracatitati",chk.isChecked());
					Log.d("INSABLON","fara c:"+chk.isChecked());
					
					actiontrans.transmite_actiuni(null, arg); // la ListaDenumiriActivty
				}
			})
			.setNegativeButton(msgNeg, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					arg.putInt(ConstanteGlobale.Actiuni_la_documente.ETICHETA_ACTIUNE, ConstanteGlobale.Actiuni_la_documente.ADAUGA_DOCUMENT_FARA_SABLON);
					actiontrans.transmite_actiuni(null, arg); // la ListaDenumiriActivty
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
			// se initializaeaza spinnerul pt zi cu ziua curenta
			// duminica are valoarea 1
			int nZi=Siruri.getDateTime().get(Calendar.DAY_OF_WEEK)+5;
			if (nZi>6) nZi=nZi-7;
			Spinner spnZi=(Spinner) view.findViewById(R.id.spnZiDlgSablonAlege);
			spnZi.setSelection(nZi);
			}
			break;
		default:
			break;
		}
		
		return builder.create();
	}
	
}
