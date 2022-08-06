
package ro.prosoftsrl.agenti;

import java.util.Calendar;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Agent;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Antet;
import ro.prosoftsrl.agenthelper.ColectieAgentHelper.Table_Pozitii;
import ro.prosoftsrl.diverse.Siruri;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends  FragmentActivity  {
	private int iIdDevice=0;
	Boolean lComenziOnline=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_listview);
//		ColectieAgentHelper colectie = new ColectieAgentHelper(getApplicationContext());
//		SQLiteDatabase db = colectie.getWritableDatabase();
//		ContentValues cvalues = new ContentValues() ;
//		cvalues.put(Antet._ID, 5);
//		long newRowId =db.insert(Antet.TABLE_NAME, null, cvalues);
//		Toast.makeText(getApplicationContext(), "Rezultat inser: "+newRowId,Toast.LENGTH_LONG ).show();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//		Editor edt= 
//				settings.edit()
//				.remove(getString(R.string.key_ecran5_comenzi_online))
//				.remove(getString(R.string.key_ecran5_zile_date));
//		edt.commit();

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            setTitle("Agenti v:"+pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            setTitle("Agenti");
        }
		try {
			iIdDevice=Integer.valueOf(settings.getString(getString(R.string.key_ecran1_id_agent), "0"));
		} catch (Exception e ) {
			iIdDevice=0 ;
		}
		if (iIdDevice>0) {
			int nZile=-1 ;
			try {
				nZile = Integer.valueOf(settings.getString(getString(R.string.key_ecran5_zile_date), "1"));
			}
			catch (Exception e ) {
			}
			stergedate(nZile);
		}
		lComenziOnline=settings.getBoolean(getString(R.string.key_ecran5_comenzi_online), false);
		Log.d("PRO","Ante optiuni"+Siruri.dtos(Siruri.getDateTime()));
		final ListView listview = (ListView) findViewById(R.id.listView_start);
		final ListViewOption[] optStart =new ListViewOption[7];
		if (lComenziOnline) {
			optStart[0]=new ListViewOption("Alege agent / traseu","",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AGENTI);
			optStart[1]=new ListViewOption("Actualizare precomenzi","Sabloane pentru precomenzi",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE);
			optStart[2]=new ListViewOption("Lista articole","Informatii articole, stocuri, miscari ",R.drawable.ic_start_articole,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_PRODUSE);
//			optStart[2]=new ListViewOption("","",R.drawable.ic_start_avize_masina,"ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC);
			optStart[3]=new ListViewOption("Listari","Diverse informatii ( vanzari , stocuri..) ",R.drawable.ic_start_cpanel,"ro.prosoftsrl.rapoarte.RapoarteActivity");
			optStart[4]=new ListViewOption("Sincronizare","Sincronizarea datelor locale cu cele din server", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SincroActivity");
			optStart[5]=new ListViewOption("Panou de control","Parametri de functionare", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SetariActivity");
			optStart[6]=new ListViewOption("Utile","Comenzi pentru administrarea bazelor de date ",R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.CPanelActivity");
//			optStart[6]=new ListViewOption("","",0,"");
		} else {
			optStart[0]=new ListViewOption("Lista clienti","Facturi, comnenzi, chitante, solduri, diverse informatii",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_CLIENTI);
			optStart[1]=new ListViewOption("Lista articole","Informatii articole, stocuri, miscari ",R.drawable.ic_start_articole,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_PRODUSE);
			optStart[2]=new ListViewOption("Incarcare/Desc. auto","Avize incarcare, descarcare masina",R.drawable.ic_start_avize_masina,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC);
			optStart[3]=new ListViewOption("Listari","Diverse informatii ( vanzari , stocuri..) ",R.drawable.ic_start_cpanel,"ro.prosoftsrl.rapoarte.RapoarteActivity");
			optStart[4]=new ListViewOption("Sincronizare","Sincronizarea datelor locale cu cele din server", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SincroActivity");
			optStart[5]=new ListViewOption("Panou de control","Parametri de functionare", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SetariActivity");
			optStart[6]=new ListViewOption("Utile","Comenzi pentru administrarea bazelor de date ",R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.CPanelActivity");						
		}
		
		final StableArrayAdapter adapter =new StableArrayAdapter(this, R.layout.row_listview_start,optStart);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick (AdapterView<?> parent ,final View view , int position, long id) {
				final ListViewOption item= (ListViewOption) parent.getItemAtPosition(position);
				//Toast.makeText(getApplicationContext(), "Item: "+item,Toast.LENGTH_LONG ).show();
				//list.remove(item);
				//adapter.notifyDataSetChanged();
				if (iIdDevice!=0)
					startActivity(position,item);
				else if (item.sActivity.equals("ro.prosoftsrl.agenti.SetariActivity"))
					startActivity(position,item);
				else
					Toast.makeText(getApplicationContext(), "Atentie ! Nu este completat Identificatorul agentului. Accesati: Panou de control / Date identificare agent/ Identificator agent ", 
								Toast.LENGTH_LONG).show();
				
			}
		});
	}
	@SuppressLint("Range")
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		// se stabileste denumirea traseului activ in conditiile in care este varianta cu comenzi online
		if (lComenziOnline) {
			ColectieAgentHelper colectie=new ColectieAgentHelper(getApplicationContext());
			SQLiteDatabase db=colectie.getWritableDatabase();
			Cursor crs=db.rawQuery("select "+Table_Agent.COL_DENUMIRE+" from "+Table_Agent.TABLE_NAME +" where "+Table_Agent.COL_ACTIV+"=1", null);
			if (crs.getCount()>0) {
				crs.moveToFirst();
				final ListView listview = (ListView) findViewById(R.id.listView_start);
				final StableArrayAdapter arr = (StableArrayAdapter) listview.getAdapter();
				ListViewOption opt = (ListViewOption) arr.getItem(0);
				opt.sDescriere=crs.getString(crs.getColumnIndex(Table_Agent.COL_DENUMIRE));
				arr.notifyDataSetChanged();
			}
			crs.close();
			db.close();
			colectie.close();
		}
	}
	public void startActivity (int position, ListViewOption item) {
		// sActivity - numele clasei pentru activitate si este dat in vectorul de initializare pt optiuni
		if (!item.sActivity.equals("")) {
			Intent intent = new Intent();
			if(item.sActivity.lastIndexOf(".")<0)
				intent.setClassName(this,this.getPackageName()+"."+item.sActivity);
			else
				// daca contine punct atunci se da cu tot cu package
				intent.setClassName(this,item.sActivity);
			
			intent.putExtra("pozitie", position);
			intent.putExtra("idTipLista", item.iILD);
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private class StableArrayAdapter extends  ArrayAdapter <ListViewOption> {
		private final Context context ;
		ListViewOption[] objects;
		//		HashMap <String,Integer> mIdMap = new HashMap <String,Integer> ();
		public StableArrayAdapter(Context context, int textViewResourceId, ListViewOption[] objects) {
			super(context,textViewResourceId,objects) ;
			this.context=context;
			this.objects=objects;
				
			}
		
		
		public View getView(int position, View convertView , ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView =inflater.inflate(R.layout.row_listview_start,parent,false);
			TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
			TextView textView2= (TextView) rowView.findViewById(R.id.secondLine);
			ImageView imageView= (ImageView) rowView.findViewById(R.id.icon);
			textView.setText(objects[position].toString());
			textView2.setText(objects[position].toDescriere());
			imageView.setImageResource(objects[position].toImgid());
			return rowView;
		}
				
	}


	// elimina istoric documente
	private void stergedate(int nZile) {
		if (nZile>0) {
			Log.d("STERG_ZILE","zile:"+nZile);
			ColectieAgentHelper colectie=new ColectieAgentHelper(getApplicationContext());
			SQLiteDatabase db=colectie.getWritableDatabase();
			Calendar crt = Calendar.getInstance();
			Cursor crs=db.rawQuery("select max(substr(antet.data,1,10)) as maxdata from antet", null);
			if (crs.getCount() > 0) {
				crs.moveToFirst();
				String maxData=crs.getString(crs.getColumnIndex("maxdata"));
				Log.d("STERG","maxdata"+maxData);
				if (maxData!=null && !maxData.equals("")) {
					// se compara data maxima din tabela cu data din sistem
					if (Siruri.getNrAnLunaZi(Siruri.cTod(maxData))<=Siruri.getNrAnLunaZi(crt)) {
						crt.add(Calendar.DAY_OF_YEAR, -nZile);
						String sdata=Siruri.dtos(crt,"-");
						Log.d("STERG","limita"+sdata);
						db.beginTransaction();
						db.delete(Table_Antet.TABLE_NAME,"substr(data,1,10)<'"+sdata+"'" ,null);
						db.delete(Table_Pozitii.TABLE_NAME," id_antet not in (select _id from antet)" ,null);
						db.setTransactionSuccessful();
						db.endTransaction();
					} else {
						Toast.makeText(this, "Ultimul document este din "+maxData+" Verifica data in aparat", Toast.LENGTH_LONG).show();
					}
				}
			}
			crs.close();
			db.close();
		}
	}

}

