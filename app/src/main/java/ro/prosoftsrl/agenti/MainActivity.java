
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
	public String cTitlu="Agenti" ;
	Boolean lComenziOnline=false;
	private String cCodSincro ="";
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

		SharedPreferences.Editor edt=
				settings.edit()
						.putBoolean(getString(R.string.key_ecran5_comenzi_online),lComenziOnline) ;
//				.remove(getString(R.string.key_ecran5_comenzi_online))
//				.remove(getString(R.string.key_ecran5_zile_date));
		edt.commit();

		cTitlu=cTitlu+(lComenziOnline ? " Comenzi " : "" );
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            setTitle(cTitlu+" v:"+pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            setTitle(cTitlu);
        }

		try {
			iIdDevice=Integer.valueOf(settings.getString(getString(R.string.key_ecran1_id_agent), "0"));
		} catch (Exception e ) {
			iIdDevice=0 ;
		}

		if (lComenziOnline) {
			if (iIdDevice<=0) {
				iIdDevice = 1;
				cCodSincro=settings.getString(getString(R.string.key_ecran1_codagent), "");
				edt = settings.edit().putString(getString(R.string.key_ecran1_id_agent), (Integer.toString(iIdDevice)));
				edt.commit();
//				edt = settings.edit().putString(getString(R.string.key_ecran1_codagent), ("037"));
//				edt.commit();
				edt=settings.edit().putString(getString(R.string.key_ecran3_ipserver), ("192.168.2.99:8833"));
				edt.commit();
				edt=settings.edit().putString(getString(R.string.key_ecran3_instanta_sql), ("sqlexpress"));
				edt.commit();
				edt=settings.edit().putString(getString(R.string.key_ecran3_nume_db), ("colectie_ppc"));
				edt.commit();
				edt=settings.edit().putString(getString(R.string.key_ecran3_user), ("sa"));
				edt.commit();
				edt=settings.edit().putString(getString(R.string.key_ecran3_parola), ("prosoftsrl"));
				edt.commit();

			}

//			if (iIdDevice <=0) {
//				// se porneste sincronizarea pt a aduce tabela de agenti dac nu exista deja inreg in tabela
//				Intent intent = new Intent();
//				intent.setClassName(this,"ro.prosoftsrl.agenti.SincroActivity");
//				intent.putExtra("pozitie", 0);
//				intent.putExtra("idTipLista", 0);
//				intent.putExtra("lNumaiAgenti",true) ;
//				startActivity(intent);
//
//			}
		} else {
			if (iIdDevice > 0) {
				int nZile = -1;
				try {
					nZile = Integer.valueOf(settings.getString(getString(R.string.key_ecran5_zile_date), "1"));
				} catch (Exception e) {
				}
				stergedate(nZile);
			}
		}
		// lComenziOnline=settings.getBoolean(getString(R.string.key_ecran5_comenzi_online), false);
		// lComenziOnline=false;
		Log.d("PRO","Ante optiuni"+Siruri.dtos(Siruri.getDateTime()));
		final ListView listview = (ListView) findViewById(R.id.listView_start);
		final ListViewOption[] optStart ; //=new ListViewOption[7];

		if (lComenziOnline) {

			optStart =new ListViewOption[5];
			optStart[0]=new ListViewOption("Alege agent / traseu","",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AGENTI);
			optStart[1]=new ListViewOption("Actualizare precomenzi","Sabloane pentru precomenzi",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_CLIENTI_COMENZI_ONLINE);
			optStart[2]=new ListViewOption("Lista articole","Informatii articole, stocuri, miscari ",R.drawable.ic_start_articole,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_PRODUSE);
//			optStart[2]=new ListViewOption("","",R.drawable.ic_start_avize_masina,"ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AVIZ_INC_DESC);
//			optStart[3]=new ListViewOption("Listari","Diverse informatii ( vanzari , stocuri..) ",R.drawable.ic_start_cpanel,"ro.prosoftsrl.rapoarte.RapoarteActivity");
//			optStart[4]=new ListViewOption("Sincronizare","Sincronizarea datelor locale cu cele din server", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SincroActivity");
			optStart[3]=new ListViewOption("Panou de control","Parametri de functionare", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SetariActivity");
			optStart[4]=new ListViewOption("Utile","Comenzi pentru administrarea bazelor de date ",R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.CPanelActivity");
//			optStart[6]=new ListViewOption("","",0,"");
//			optStart[6]=new ListViewOption("","",0,"");
		} else {
//			optStart[0]=new ListViewOption("Panou de control","Parametri de functionare", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SetariActivity");
//			optStart[1]=new ListViewOption("Sincronizare","Sincronizarea datelor locale cu cele din server", R.drawable.ic_start_cpanel,"ro.prosoftsrl.agenti.SincroActivity");
//			optStart[2]=new ListViewOption("Traseu 1","",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AGENTI);
//			optStart[3]=new ListViewOption("Traseu 8","",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AGENTI);
//			optStart[4]=new ListViewOption("Traseu 17","",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AGENTI);
//			optStart[5]=new ListViewOption("Traseu 20","",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AGENTI);
//			optStart[6]=new ListViewOption("Traseu 23","",R.drawable.ic_start_lista_clienti,"ro.prosoftsrl.agenti.ListaDenumiriActivity",Biz.TipListaDenumiri.TLD_AGENTI);
			optStart =new ListViewOption[7];
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
					// daca este var de comenzi si iddevice <>0 inseamna ca se incearca schimbarea
					// traseului fara ca sa se fi dat sincronizare inainte. traseu se poate schimba numai daca
					// nu este deja altul activ
//					if (lComenziOnline && position==0)
//						Toast.makeText(getApplicationContext(),
//								"Atentie ! Traseul se poate schimba numai dupa ce se da Sincronizare ",
//								Toast.LENGTH_LONG).show();
//					else
						startActivity(position,item);
				else if (lComenziOnline && position==0 ) {
					startActivity(position,item);
				}
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
		boolean lAreAg = true;
//		// se stabileste denumirea traseului activ in conditiile in care este varianta cu comenzi online
		if (lComenziOnline) {
			ColectieAgentHelper colectie=new ColectieAgentHelper(getApplicationContext());
			SQLiteDatabase db=colectie.getWritableDatabase();
			Cursor crs=db.rawQuery("select "+Table_Agent.COL_DENUMIRE+","+
					Table_Agent._ID+","+
					Table_Agent.COL_ACTIV+
					" from "+Table_Agent.TABLE_NAME , null);
			if ( crs !=null && crs.getCount() > 0) {
				lAreAg = true; // exista inreg in tabela de ag
				// verifica daca idDevice este si cel activat in agenti
				db.beginTransaction();
				db.execSQL(" UPDATE " + Table_Agent.TABLE_NAME + " set " + Table_Agent.COL_ACTIV + " =0 ");
				db.execSQL(" UPDATE " + Table_Agent.TABLE_NAME + " set " + Table_Agent.COL_ACTIV + " =1 " + " where " + Table_Agent._ID + "=" + iIdDevice);
				db.setTransactionSuccessful();
				db.endTransaction();
				crs.close();
			} else {
				lAreAg=false;
			}
			if (iIdDevice>0 && lAreAg) {
				// se scrie in lista de agenti numele traseului activ
				crs = db.rawQuery("select " + Table_Agent.COL_DENUMIRE + " from " + Table_Agent.TABLE_NAME +
							" where " + Table_Agent.COL_ACTIV + "=1"+
							" and "+Table_Agent._ID+"="+iIdDevice
							, null);
						try {
								crs.moveToFirst();
								final ListView listview = (ListView) findViewById(R.id.listView_start);
								final StableArrayAdapter arr = (StableArrayAdapter) listview.getAdapter();
								ListViewOption opt = (ListViewOption) arr.getItem(0);
								opt.sDescriere = crs.getString(crs.getColumnIndex(Table_Agent.COL_DENUMIRE));
								arr.notifyDataSetChanged();
						} catch (Exception e) {
							e.getMessage();
						}

				crs.close();
			}
			db.close();
			colectie.close();
			// in caz ca nu s-au gasit ag se preia tabela din server
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			cCodSincro=settings.getString(getString(R.string.key_ecran1_codagent), "");

			if (!lAreAg && !cCodSincro.equals("")) {
				Intent intent = new Intent();
				intent.setClassName(this,"ro.prosoftsrl.agenti.SincroActivity");
				intent.putExtra("pozitie", 0);
				intent.putExtra("idTipLista", 0);
				intent.putExtra("lNumaiAgenti",true) ;
				intent.putExtra("lAuto",true) ;
				intent.putExtra("lFinish",true) ;
				startActivity(intent);

			}
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
				//if(crs.getColumnIndex("maxdata")>=0)
				@SuppressLint("Range") String maxData=crs.getString(crs.getColumnIndex("maxdata"));
				Log.d("STERG","maxdata"+maxData);
				if (maxData!=null && !maxData.equals("")) {
					// se compara data maxima din tabela cu data din sistem
					int nDataAnt =Siruri.getNrAnLunaZi(Siruri.cTod(maxData));
					int nDataCrt=Siruri.getNrAnLunaZi(crt);
					if (nDataCrt>=nDataAnt) {
						if (nDataCrt - nDataAnt <= nZile) {
							crt.add(Calendar.DAY_OF_YEAR, -nZile);
							String sdata = Siruri.dtos(crt, "-");
							Log.d("STERG", "limita" + sdata);
							db.beginTransaction();
							db.delete(Table_Antet.TABLE_NAME, "substr(data,1,10)<'" + sdata + "'", null);
							db.delete(Table_Pozitii.TABLE_NAME, " id_antet not in (select _id from antet)", null);
							db.setTransactionSuccessful();
							db.endTransaction();
						} else {
							Toast.makeText(this, "Ultimul document este din " + maxData + " Verifica data in aparat", Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(this, "Ultimul document este din " + maxData + " Verifica data in aparat", Toast.LENGTH_LONG).show();
					}
				}
			}
			crs.close();
			db.close();
		}
	}

}

