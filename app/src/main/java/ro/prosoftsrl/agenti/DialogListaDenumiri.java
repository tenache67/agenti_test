package ro.prosoftsrl.agenti;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DialogListaDenumiri extends DialogFragment{
	onDlgClick mCallBack;
	private String[] opt ;
	private String title;
	long[] ids ;
	int iTLD;
	long _id ;
	int idTipDialog;
	static DialogListaDenumiri newInstance(Bundle fb ) {
		DialogListaDenumiri dlg = new DialogListaDenumiri();
//		Bundle args =new Bundle();
//		args.putStringArray("opt", fb.getStringArray("opt"));
//		args.putIntArray("ids", fb.getIntArray("ids"));
//		args.putString("title", fb.getString("title"));
//		args.putInt("iTLD", fb.getInt("iTLD"));
//		args.putLong("_id", fb.getLong("_id"));
//		args.putInt("tipdialog",fb.getInt("tipdialog") );
		dlg.setArguments(fb);
		return dlg;
	}
	
	public interface onDlgClick {
		//public void onListItemSelected(String selection,int iTLD,int _id, int position, int idTipDialog);
		// key in arg :
		// "opt" array cu optiunile "ids" array cu idurile "title" titlul dialogului 
		// "iTLD" id pt tip lista denumiri "_id" un id de inregistrare pe care s-a apelat dilogul
		// "tipdialog" int ce reprezinta idul tipului de dialog "which" pozitia pe care s-a dat click
		// "actiune" reprezinta o actiune care se transmite mai departe
		public void onListItemSelected(Bundle arg);
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mCallBack=(onDlgClick) activity ;
		} catch (ClassCastException e ) {
			throw new ClassCastException(activity.toString()+" trebuie sa implementeze onDlgclick");
		}
		this.setCancelable(false);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.opt=getArguments().getStringArray("opt");
		this.ids=getArguments().getLongArray("ids");
		this.title=getArguments().getString("title");
		this.iTLD=getArguments().getInt("iTLD");
		this._id=getArguments().getLong("_id");
		this.idTipDialog=getArguments().getInt("tipdialog");
		return new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setCancelable(false)
				.setItems(opt, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						final Bundle arg = getArguments();
						arg.putInt("which", which);
						//mCallBack.onListItemSelected(opt[which],iTLD,_id, which,idTipDialog);
						// se trimite la activity care implementeaza interfata dialogului 
						// vezi cu ce se initializeaza mcallback
						mCallBack.onListItemSelected(arg);
						getDialog().dismiss();
						DialogListaDenumiri.this.dismiss();
					}
				}).create(); 
					
		}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
}
