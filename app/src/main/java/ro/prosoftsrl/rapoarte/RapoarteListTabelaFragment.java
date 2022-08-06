package ro.prosoftsrl.rapoarte;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenti.R;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RapoarteListTabelaFragment extends Fragment {
	Context context ; 
@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		context=activity;
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view=inflater.inflate(R.layout.fragment_rapoarte_list_tablea, container,false);
		Bundle arg = getArguments();
		String sqlSir=arg.getString("sqlsir");
		ColectieAgentHelper colectie= new ColectieAgentHelper(context);
		Cursor crs=colectie.getReadableDatabase().rawQuery(sqlSir, null);
		crs.moveToFirst();
		// se verifica existenta denumirilor de coloane
		String[] vCap=null;
		if(arg.containsKey("caption")) {
			vCap=arg.getStringArray("caption");
		}
		TableLayout tbl =(TableLayout) view.findViewById(R.id.tblFrgListTabela);
		tbl.removeAllViews();
		// linia de caption
		TableRow row = new TableRow(context);
		for(int i =0 ; i<crs.getColumnCount(); i++) {
			final TextView columnView= new TextView(context);
			if(vCap==null || vCap.length<i) {
				columnView.setText(crs.getColumnName(i));
			} else {
				columnView.setText(vCap[i]);
			}
			columnView.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
			columnView.setTextColor(getResources().getColor(android.R.color.black));
			columnView.setPadding(4, 4, 4, 4);
			row.addView(columnView);
		}
		tbl.addView(row);
		
		for (int i = 0; i < crs.getCount(); i++) {
			row = new TableRow(context);
			for (int j = 0; j < crs.getColumnCount(); j++) {
				final TextView columnView =new TextView(context);
				columnView.setText(crs.getString(j).toString());
				columnView.setTextAppearance(context, android.R.style.TextAppearance_Large);
				columnView.setPadding(2, 2, 2, 2);
				if (Math.round(j/2)*2==j) {
					columnView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
					columnView.setTextColor(getResources().getColor(android.R.color.background_light));
				}
				else {
					columnView.setBackgroundColor(getResources().getColor(android.R.color.background_light));
					columnView.setTextColor(getResources().getColor(android.R.color.background_dark));
				}
				row.addView(columnView);
			}
			tbl.addView(row);
			crs.moveToNext();
		}
		crs.close();
		colectie.close();
		return view;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
