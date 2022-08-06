package ro.prosoftsrl.listare;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.datecs.api.printer.Printer;
import com.datecs.api.printer.PrinterInformation;
import com.datecs.api.printer.ProtocolAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import ro.prosoftsrl.agenthelper.ColectieAgentHelper;
import ro.prosoftsrl.agenti.Biz;
import ro.prosoftsrl.agenti.ConstanteGlobale;
import ro.prosoftsrl.agenti.DialogGeneralDaNu;
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.ConvertNumar;
import ro.prosoftsrl.diverse.Siruri;

/**
 * Created by Traian on 14.02.2015.
 */
public class ComunicatiiDPP_350 {
    Context context ;
    final int iInalt=10;
    final int iRowHeight=30;
    final int iCurrLeftAbsolut=1;
    final int iLeftPageRegion=0;
    final int iWidthPageregion=576;

    private final ProtocolAdapter.ChannelListener mChannelListener = new ProtocolAdapter.ChannelListener() {

        @Override
        public void onReadEncryptedCard() {
            toast("");
        }

        @Override
        public void onReadCard() {

        }

        @Override
        public void onReadBarcode() {

        }

        @Override
        public void onPaperReady(boolean state) {
            Log.d("PRO&","onPaperready : "+state);
            if (state) {
                toast("Hartie pregatita");
            } else {
                toast("Imprimanta nu are hartie");
            }
        }
        @Override
        public void onOverHeated(boolean state) {
            Log.d("PRO&","onOverHeated : "+state);
            if (state) {
                toast("Imprimanta supraincalzita");
            }
        }

        @Override
        public void onLowBattery(boolean state) {
            if (state) {
                toast("Acumulator descarcat ");
            }
        }
    };
    // Member variables
    private Printer mPrinter;
    private ProtocolAdapter mProtocolAdapter;
    private PrinterInformation mPrinterInfo;
    private BluetoothSocket mBluetoothSocket;
    private Socket mPrinterSocket;
    private boolean mRestart;
    private FragmentActivity act ;
    private String sAdapter;
    // starea in care se gaseste obiectul 0 - initial 1 - in curs de conectare 2 - fara activitate si conectat
    //  3 - fara activitate si neconectat 4 - in curs de listare
    private int stare =0 ;
    private int[] imageargb ;
    private int imagewidth ;
    private int imageheight;

    ComunicatiiDPP_350(Context context) {
        this.context=context;
        act=(FragmentActivity) context ;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        this.sAdapter=settings.getString(context.getString(R.string.key_ecran3_bluetadapter),"");
        Log.d("PRO&",sAdapter);
        toast("Conectare la imprimanta");
        Log.d("PRO&","1.1");
        establishBluetoothConnection(sAdapter);
        Log.d("PRO&", "1.2");
        while (getStare() <= 1) {
            // asteapta conectarea
        }
        if ( getStare()==3 ) {
            toast("Verificati imprimanta ! Nu se poate realiza conexiunea cu imprimanta !");
        }
        Log.d("PRO&", "1.3");
    }

    private void toast(final String text) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!act.isFinishing()) {
                    Toast.makeText(act, text, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void dialog(final int iconResId, final String title, final String msg) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setIcon(iconResId);
                builder.setTitle(title);
                builder.setMessage(msg);
                AlertDialog dlg = builder.create();
                dlg.show();
            }
        });
    }

    private void error(final String text, boolean resetConnection) {
        if (resetConnection) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    private void doJob(final Runnable job, final int resId) {
        // Start the job from main thread

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Progress dialog available due job execution
                FragmentTransaction ft =act.getSupportFragmentManager().beginTransaction();
                Fragment prev = act.getSupportFragmentManager().findFragmentByTag("dialoglistare");
                if (prev !=null) {
                    ft.remove(prev);
                    ft.commit();
                }
                Bundle arg = new Bundle();
                Log.d("PRO&","1.8");
                arg.putString("titlu", "Va rog asteptati ... ");
                arg.putString("text_pozitiv", "DA");
                arg.putString("text_negativ", "NU");
                arg.putInt("actiune_pozitiv", ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT);
                final DialogGeneralDaNu dialog = DialogGeneralDaNu.newinstance(arg);
                dialog.show(ft, "dialoglistare");
                Log.d("PRO&", "dupa afisare dialog:" + act.getTitle());

/*
                final ProgressDialog dialog =  new ProgressDialog(act);
                dialog.setTitle("Va rog asteptati");
                //dialog.setMessage(getString(resId));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
*/

                Thread t = new Thread(new Runnable() {
                    @Override

                    public void run() {
                        Log.d("PRO&", "1.9");
                        try {
                            Log.d("PRO&", "1.10");
                            job.run();
                            Log.d("PRO&", "1.11");

                        } finally {
                            // dialog.dismiss();
                        }
                    }
                });
                t.start();
            }
        });
    }

    protected void initPrinter(InputStream inputStream, OutputStream outputStream) throws IOException {
        mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);

        if (mProtocolAdapter.isProtocolEnabled()) {
            final ProtocolAdapter.Channel channel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            channel.setListener(mChannelListener);
            // Create new event pulling thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            channel.pullEvent();
                        } catch (IOException e) {
                            e.printStackTrace();
                            error(e.getMessage(), mRestart);
                            break;
                        }
                    }
                }
            }).start();
            mPrinter = new Printer(channel.getInputStream(), channel.getOutputStream());
            Log.d("PRO&", "Cu protocol");
        } else {
            Log.d("PRO&","Fara protocol");
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
            Log.d("PRO&","1 Fara protocol");
        }

        mPrinterInfo = mPrinter.getInformation();
        Log.d("PRO&","21");
//        act.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ((ImageView) act.findViewById(R.id.icon)).setImageResource(R.drawable.icon);
//                ((TextView) act.findViewById(R.id.name)).setText(mPrinterInfo.getName());
//            }
 //       });
    }

    private void establishBluetoothConnectionnew(final String address) {
        stare=1;
        Log.d("PRO&","1.4");
        doJob(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = null;
                Log.d("PRO&", "3.33");
            }
        }, 0);
    }


    private void establishBluetoothConnection(final String address) {
        stare=1;
        Log.d("PRO&","1.4");
        doJob(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = null;
                Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                Log.d("PRO&", "1.1-1");
                if (pairedDevices.size() > 0) {
                    Log.d("PRO&", "1.12");
                    for (BluetoothDevice mdevice : pairedDevices) {
                        // MP300 is the name of the bluetooth printer device
                        Log.d("PRO&", "1.13-- "+mdevice.getName()+" = "+address);
                        if (mdevice.getName().equals(address)) {
                            Log.d("PRO&", "1.14");
                            device = mdevice;
                            break;
                        }
                    }
                }
                Log.d("PRO&","1.5");
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                InputStream in = null;
                OutputStream out = null;
                adapter.cancelDiscovery();
                try {
                     mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                    boolean ltermin=false;
                    String err ="";
                    int steps=0;
                    Log.d("PRO&","1.6");

                    while (!ltermin) {
                        try {
                            mBluetoothSocket.connect();
                            //beginListenForData();
                        } catch (IOException e ) {
                            e.printStackTrace();
                            err=e.getMessage();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            err=context.getResources().getString(R.string.err_bluet_nupoatefiaccesat)+" "+sAdapter+" "+e.getMessage();

                        } catch (Exception e) {
                            e.printStackTrace();
                            err=context.getResources().getString(R.string.err_bluet_nupoatefiaccesat)+" "+sAdapter+" "+e.getMessage();
                        }
                        if (err.equals("")  ) {
                            ltermin=true;
                        } else {
                            if (steps==5) ltermin=true; else err="";
                        }
                        steps=steps+1;
                    }
                    in = mBluetoothSocket.getInputStream();
                    out = mBluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&", "Eroare de comunicare. " + e.getMessage());
                }
                Log.d("PRO&","1.7");

                try {
                    initPrinter(in, out);
                    stare=2;
                } catch (IOException e) {
                    stare=3;
                    e.printStackTrace();
                    Log.d("PRO&","Eroare la initializare . " +  e.getMessage());
                    return;
                }
            Looper.loop();}
        }, 0);
    }

    private synchronized void closeBlutoothConnection() {
        // Close Bluetooth connection
        BluetoothSocket s = mBluetoothSocket;
        mBluetoothSocket = null;
        if (s != null) {
            Log.d("PRO&", "Close Blutooth socket");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closePrinterConnection() {
        if (mPrinter != null) {
            mPrinter.release();
        }

        if (mProtocolAdapter != null) {
            mProtocolAdapter.release();
        }
    }

    public synchronized void closeActiveConnection() {
        closePrinterConnection();
        closeBlutoothConnection();
    }

    // determina sirul de conformitate ce se trece in josul facturii
    private String setConformitate (String cClient, String cNr_doc, String cData) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String sCodFiscal = settings.getString(context.getString(R.string.key_ecran4_cf), "");
        String sConf;
        if (sCodFiscal.toLowerCase().contains("3120044")) {
            sConf="Declaram pe proprie raspundere ca produsele de panificatie livrate conform facturii sunt in conformitate cu legislatia sanitar veterinara si pentru siguranta alimentului in vigoare";
        } else {
            if (sCodFiscal.toLowerCase().contains("15460988")) {
                sConf = "CERTIFICAT DE CALITATE - DECLARATIE DE CONFORMITATE                  Subscrisa , S.C. ACS PRODUCT S.R.L. , cu sediul in BRAILA, Soseaua Baldovinesti nr.40, CIF: RO15460988, declara pe proprie raspundere ca produsele livrate catre " +
                        cClient.trim() + " cu factura nr " + cNr_doc + " din data de " + cData + " nu pun in pericol viata si sanatatea, nu produc un impact negativ asupra mediului si sunt in conformitate cu legislatia sanitara si pentru siguranta alimentelor in vigoare si cu standardul de firma nr. 1 / 2003 .Produsele corespund scopului pentru care au fost realizate si indeplinesc parametrii de calitate specificati in standardul de firma in vigoare. Prin prezenta garantam calitatea acestor produse pentru perioada termenului de valabilitate, cu conditia respectarii prescriptiilor de transport, depozitare si consum specificate pe amblajul produselor.";
            } else {
                if (sCodFiscal.toLowerCase().contains("34466138")) {
                    sConf = "CERTIFICAT DE CALITATE - DECLARATIE DE CONFORMITATE                  Subscrisa , OUR BREAD S.R.L. , cu sediul in BRAILA, Aleea Culturii nr 6-8 Viziru bl.8,sc.7, ap.129 , CIF:RO34466138, declara pe proprie raspundere ca produsele livrate catre " +
                            cClient.trim() + " cu factura nr " + cNr_doc + " din data de " + cData + " nu pun in pericol viata si sanatatea, nu produc un impact negativ asupra mediului si sunt in conformitate cu legislatia sanitara si pentru siguranta alimentelor in vigoare. Prin prezenta garantam calitatea acestor produse pentru perioada termenului de valabilitate, cu conditia respectarii prescriptiilor de transport, depozitare si consum specificate pe amblajul produselor.";
                } else {
                    sConf = "";
                }
            }
        }
        return sConf;
    }

    // determina bitmap pentru stampila pe baza de cod fiscal
    private void setStampila ( ) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true ;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String sCodFiscal = settings.getString(context.getString(R.string.key_ecran4_cf), "");
        String sCodAg=settings.getString(context.getString(R.string.key_ecran1_id_agent ), "");
        Bitmap bitmap;
        if (sCodFiscal.toLowerCase().contains("33984123")) {
            // semrompack
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_semrompack);
        } else if (sCodFiscal.toLowerCase().contains("3120044")) {
            // betty
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_bettycom);
        } else if (sCodFiscal.toLowerCase().contains("13804023")){
            // eurotehnic
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_eurot);
        } else if (sCodFiscal.toLowerCase().contains("15460988")){
            // acs
            if (sCodAg.equals("2")) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_acs_2);
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_acs);
            }
        } else if (sCodFiscal.toLowerCase().contains("34466138")){
            // ourbread
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_ourbred);
        }
        else {
            bitmap  = BitmapFactory.decodeResource(context.getResources(), R.drawable.stamp_goala);

        }

        imagewidth = bitmap.getWidth();
        imageheight = bitmap.getHeight();
        imageargb = new int[imagewidth * imageheight];
        bitmap.getPixels(imageargb, 0, imagewidth, 0, 0, imagewidth, imageheight);
        bitmap.recycle();
    }
    // chitanta
    public void printChitanta ( final Cursor crs  ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&", "Nu suporta mod pagina");
                    return;
                }
                setStampila();
                final int width = imagewidth;
                final int height = imageheight;
                final int[] argb = imageargb;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                try {
                    Log.d("PRO&", "Inainte de compunere listare: " + mPrinter.getStatus());
                    mPrinter.reset();
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    int iCurrLeft = iCurrLeftAbsolut;
                    int iCurrTop = iInalt;
                    int iTopPageRegion = 0;
                    int iHeightPageRegion = 90;
                    int iLiniiCaseta = 0;
                    // descriere antet chitanta
                    crs.moveToFirst();
                    iHeightPageRegion = iInalt + 3 * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    mPrinter.setPageXY(0, iCurrTop);
                    mPrinter.printTaggedText("{reset}{center}{b}{w}{u}CHITANTA {br}");
                    iCurrTop = iCurrTop + iRowHeight + 3;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Serie: {/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_serie_facturi), "").trim()
                            +"{br}"); // aici a fost
                    iCurrLeft = iCurrLeft + 250-90;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Numar: {/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_CHITANTA)).trim() + "{br}");
                    iCurrLeft = iCurrLeft + 250-40;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA)))).trim()
                            + "{br}");

                    // caseta furnizor
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iLiniiCaseta=6;
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2),"").equals("")) {
                        iLiniiCaseta=iLiniiCaseta+2;
                    }
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").length()>53)
                        iLiniiCaseta=iLiniiCaseta+1;
                    iHeightPageRegion=iInalt+iLiniiCaseta*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}"+Siruri.padR("FURNIZOR:",16)+"{/u}{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nume_firma),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("C.U.I.:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cf),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Nr.reg.Com:",11)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nrrc),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Adresa:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_adresa),"")
                            + "{br}");
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").length()>53)
                        iCurrTop=iCurrTop+iRowHeight;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Judet:",16)+"{/i}" +
//                            settings.getString(context.getString(R.string.key_ecran4_judet),"")
//                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_banca1),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("IBAN:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cont1),"")
                            + "{br}");
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2),"").equals(""))
                    {
                        iCurrTop=iCurrTop+iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",8)+"{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_banca2),"")
                                + "{br}");
                        iCurrTop=iCurrTop+iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("IBAN:",8)+"{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cont2),"")
                                + "{br}");
                    }
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Capital social:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cap_social),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Tel/fax:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_telfax),"")
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);
                    Log.d("PRO&", "Dupa caseta furniz: " + mPrinter.getStatus());
                    // caseta client
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    iHeightPageRegion = iInalt + 10 * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}" + "Am primit de la::  " + "{/u}{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_DENUMIRE))
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "C.U.I.: " + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_FISc)).trim() +
                            "{b}{i} Nr. ORC/an: " +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC)).trim()
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Adresa: " + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_JUDET))
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Suma de : " + "{/i}{/b}" +
                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA)) +
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                    , 9, Biz.ConstCalcul.ZEC_VAL_FARA) +
                            "{b}{i} Adica: {/i}{/b}" +
                            ConvertNumar.convert(
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA)) +
                                            crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))) +
                            "{br}");
                    //pozitionare stampila
                    mPrinter.setPageXY(iWidthPageregion - width - 20, iCurrTop);
                    mPrinter.printImage(argb, width, height, Printer.ALIGN_RIGHT, true);
                    iCurrTop = iCurrTop + 3 * iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Reprezentand : " + "{/i}{/b}" +
                            " CV. Fact. " +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(150);
                    Log.d("PRO&", "Inainte de flush: " + mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&", "Dupa flush: " + mPrinter.getStatus());
                    mPrinter.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
                }

            }
        }, 0);
        stare=iOldStare;
    }

    // listeaza aviz client
    public void printAviz_client ( final Cursor crs ) {
        int iOldStare=getStare();
        stare=4;
//aici voi trece pt decl de conformitate pt betty
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&", "Nu suporta mod pagina");
                    return;
                }
                setStampila();
                final int width = imagewidth;
                final int height = imageheight;
                final int[] argb = imageargb;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                try {
                    crs.moveToFirst();
                    Log.d("PRO&", "Inainte de compunere reset listare: " + mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&", "Inainte de compunere dupa reset listare: " + mPrinter.getStatus());
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime

                    int iCurrLeft = iCurrLeftAbsolut;
                    int iCurrTop = iInalt;
                    int iTopPageRegion = 0;
                    int iHeightPageRegion = 90;
                    // deplasamentele pentru liniile ce delimiteaza coloanele din factura
                    // mm*8
                    int iLiniiCaseta = 0;
                    // descriere antet factura
                    iHeightPageRegion = iInalt + 3 * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    mPrinter.setPageXY(0, iCurrTop);
                    mPrinter.printTaggedText("{reset}{center}{b}{w}{u}AVIZ DE INSOTIRE AL MARFII {br}");
                    iCurrTop = iCurrTop + iRowHeight + 3;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Serie: {/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_serie_facturi), "")
                            + "{br}");
                    iCurrLeft = iCurrLeft + 230;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Numar: {/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
                    iCurrLeft = iCurrLeft + 250;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight - 3;
                    iCurrLeft = iCurrLeftAbsolut;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Cota TVA: {/i}" +
                            Siruri.str(Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_COTA_TVA)), 0), 4, 0)
                            + "{br}");

                    // caseta furnizor

                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    iLiniiCaseta = 6;
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2), "").equals("")) {
                        iLiniiCaseta = iLiniiCaseta + 2;
                    }
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").length() > 53)
                        iLiniiCaseta = iLiniiCaseta + 1;

                    iHeightPageRegion = iInalt + iLiniiCaseta * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}" + Siruri.padR("FURNIZOR:", 16) + "{/u}{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nume_firma), "")
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("C.U.I.:", 16) + "{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cf), "")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Nr.reg.Com:", 16) + "{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nrrc), "")
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Adresa:", 16) + "{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_adresa), "")
                            + "{br}");
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").length() > 53)
                        iCurrTop = iCurrTop + iRowHeight;

//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Judet:",16)+"{/i}" +
//                            settings.getString(context.getString(R.string.key_ecran4_judet),"")
//                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Banca:", 16) + "{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_banca1), "")
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("IBAN:", 16) + "{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cont1), "")
                            + "{br}");
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2), "").equals("")) {
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Banca:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_banca2), "")
                                + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("IBAN:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cont2), "")
                                + "{br}");

                    }
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Capital social:", 16) + "{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cap_social), "")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Tel/fax:", 16) + "{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_telfax), "")
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    // caseta client
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    iLiniiCaseta = 5;
                    if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA)).trim().length() > 53)
                        iLiniiCaseta = iLiniiCaseta + 1;
                    iHeightPageRegion = iInalt + iLiniiCaseta * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}" + Siruri.padR("CUMPARATOR:", 16) + "{/u}{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_DENUMIRE))
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("C.U.I.:", 16) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_FISc))
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Nr.reg.Com:", 16) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC))
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Adresa:", 16) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA))
                            + "{br}");
                    if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA)).trim().length() > 53)
                        iCurrTop = iCurrTop + iRowHeight;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Judet:",16)+"{/i}" +
//                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_JUDET))
//                        + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Banca:", 16) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_BANCA))
                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("IBAN:", 16) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_CONT))
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    // caseta antet articole
                    Log.d("PRO&", "Inainte de antet articole");
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    iHeightPageRegion = iInalt + 2 * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;
                    int iLinieDen = 5 * 8;
                    int iLinieUM = iLinieDen + 24 * 8;
                    int iLiniePRED = iLinieUM + 4 * 8;
                    int iLinieCant = iLiniePRED + 5 * 8;
                    int iLiniePU = iLinieCant + 10 * 8;
                    int iLinieFara = iLiniePU + 10 * 8;
                    int iLinieTva = iLinieFara + 10 * 8;

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Nr {br}");
                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR(" Denumire articol", 22) + "{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "UM" + "{br}");
                    mPrinter.setPageXY(iLiniePRED, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "%RED" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "  Cant" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Pret unit" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Valoarea" + "{br}");
//                    mPrinter.setPageXY(iLinieTva, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Valoarea" + "{br}");

                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
//                    mPrinter.setPageXY(iLinieTva, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "   TVA" + "{br}");

                    mPrinter.drawPageRectangle(iLinieDen - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePRED - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    Log.d("PRO&", "Inainte de caseta articole");
                    // caseta articole
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    int nRows = 0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast()) {
                        nRows = nRows + 1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length() > 24) {
                            nRows = nRows + 1;
                        }
                        crs.moveToNext();
                    }
                    // se tine seama ca la urma vin 2 randuri pentru val marfa si val reducere
                    nRows = nRows + 2;
                    if (nRows < 4) nRows = 4;
                    iHeightPageRegion = iInalt + nRows * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;
                    mPrinter.drawPageRectangle(iLinieDen - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePRED - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    crs.moveToFirst();
                    int k = 0;
                    double nCotaTva = 0;
                    double nValFara = 0, nValRed = 0, nValTva = 0, nTvaRed = 0;
                    String sDen = "";
                    while (!crs.isAfterLast()) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen = crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k + 1) + "", 3) + " {br}");
                        mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padR(sDen, 24) + "{br}");
                        mPrinter.setPageXY(iLinieUM, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + " bc" + "{br}");
                        mPrinter.setPageXY(iLiniePRED, iCurrTop);
                        Log.d("PRO&", "Ininte de afis proc red");
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_PROC_RED)), 6, 3)
                                + "{br}");
                        Log.d("PRO&", "Dupa afis proc red");

                        mPrinter.setPageXY(iLinieCant, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)), 7, 2)
                                + "{br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_PRET_FARA)), 8, Biz.ConstCalcul.ZEC_PRET_FARA)
                                + "{br}");
                        mPrinter.setPageXY(iLinieFara, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA)), 8, Biz.ConstCalcul.ZEC_VAL_FARA)
                                + "{br}");
//                        mPrinter.setPageXY(iLinieTva, iCurrTop);
//                        mPrinter.printTaggedText("{reset}{left}"+
//                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA)),6,Biz.ConstCalcul.ZEC_VAL_TVA)
//                               +"{br}");

                        if (sDen.length() > 29) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " + Siruri.padR(sDen.substring(29), 29) + "{br}");
                        }
                        nValFara = nValFara + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                        nValTva = nValTva + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                        nValRed = nValRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                        nTvaRed = nTvaRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                        iCurrTop = iCurrTop + iRowHeight;
                        crs.moveToNext();
                        k = k + 1;
                    }
                    // pentru reducere
                    mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "         Total marfa:" + "{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "---" + "{br}");
                    mPrinter.setPageXY(iLiniePRED, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-----" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-------" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "--------" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(nValFara, 8, Biz.ConstCalcul.ZEC_VAL_FARA)
                            + "{br}");
//                    mPrinter.setPageXY(iLinieTva, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}" +
//                            Siruri.str(nValTva, 6, Biz.ConstCalcul.ZEC_VAL_TVA)
//                            + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "     Discount acordat:" + "{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "---" + "{br}");
                    mPrinter.setPageXY(iLiniePRED, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-----" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-------" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "--------" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(nValRed, 8, Biz.ConstCalcul.ZEC_VAL_FARA)
                            + "{br}");
                    //                  mPrinter.setPageXY(iLinieTva, iCurrTop);
                    //                  mPrinter.printTaggedText("{reset}{left}" +
                    //                          Siruri.str(nTvaRed, 6, Biz.ConstCalcul.ZEC_VAL_TVA)
                    //                          + "{br}");
                    Log.d("PRO&", "Inainte de subsol");

                    // descriere subsol ( 8 randuri)
                    crs.moveToFirst();
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    iHeightPageRegion = iInalt + 8 * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnatura si{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    // totaluri fara si tva
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR(" Total:", 10) + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA))
                                    , 9, Biz.ConstCalcul.ZEC_VAL_FARA) + "{br}");
//                    mPrinter.setPageXY(iLinieTva, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}"+
//                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
//                                    ,8,Biz.ConstCalcul.ZEC_VAL_TVA)+"{br}");
                    mPrinter.drawPageRectangle(iLiniePU, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva, 0, 1, iCurrTop + iRowHeight, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU, iCurrTop + iRowHeight, iWidthPageregion - iLiniePU, 1, Printer.FILL_BLACK);
                    iCurrLeft = 210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Delegat:{br}");
                    mPrinter.drawPageRectangle(200, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "stampila{br}");
                    iCurrLeft = 210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + settings.getString(context.getString(R.string.key_ecran1_numeagent), "") + "{br}");
                    iCurrLeft = 428;
                    mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnat.{br}");
                    iCurrLeft = iCurrLeftAbsolut;
                    //iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(210, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "BI/CI:{/i}{/b} " +
                            settings.getString(context.getString(R.string.key_ecran1_biagent), "") +
                            "{br}");
                    iCurrLeft = 428;
                    mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "de {br}");
                    mPrinter.setPageXY(iLinieFara + 2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total cu TVA" + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    iCurrLeft = 210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    try {
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Auto: " +
                                settings.getString(context.getString(R.string.key_ecran1_auto), "")
                                + "{br}");
                    } catch (IOException e) {
                        Log.d("PRO&", "Eroare:" + e.getMessage());
                        e.printStackTrace();
                    }
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "primire{br}");

                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{w}" +
                            Siruri.str(
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA)) +
                                            crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                    , 7, Biz.ConstCalcul.ZEC_VAL_CU)
                            + "{br}");

                    iCurrLeft = 210;
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Intocmit de:" + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            settings.getString(context.getString(R.string.key_ecran1_numeagent), "") + "{br}");
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}CNP: " +
                            settings.getString(context.getString(R.string.key_ecran1_cnpagent), "") + "{br}");
                    // sectiune de reclama
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    iHeightPageRegion = iInalt + iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
//                    iCurrTop=2;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{i}{s}"+"Software pentru agenti de vanzari realizat de PROSOFT SRL . Tel. 0722 236256"+"{br}");
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(150);
                    Log.d("PRO&", "Inainte de flush factura: " + mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&", "Dupa flush factura: " + mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&", "Dupa reset factura: " + mPrinter.getStatus());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
                }

            }
        }, 0);
        stare=iOldStare;
    }


    // listeaza documente
    public void printFactura ( final Cursor crs ) {
        boolean lRed=false;
        crs.moveToFirst();
        while (! crs.isAfterLast()) {
            Log.d("PRO&","Proc red="+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_PROC_RED)));
            if (crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_PROC_RED))!=0) {
                lRed=true;
            }
            crs.moveToNext();
        }
        if (lRed) {
            //printFactura_cuRed(crs);
            // pentru dpp350 se listeaza numai in forma de factura fara discount
            printFactura_faraRed(crs);
            Log.d("PRO&", "Cu red");
        } else {
            printFactura_faraRed(crs);
            Log.d("PRO&","Fara red");
        }
    }

    public void printFactura_cuRed ( final Cursor crs ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&","Nu suporta mod pagina");
                    return;
                }
                setStampila();
                final int width = imagewidth;
                final int height = imageheight;
                final int[] argb = imageargb;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                try {
                    crs.moveToFirst();
                    Log.d("PRO&","Inainte de compunere reset listare: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Inainte de compunere dupa reset listare: "+mPrinter.getStatus());
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime

                    int iCurrLeft=iCurrLeftAbsolut;
                    int iCurrTop=iInalt;
                    int iTopPageRegion=0;
                    int iHeightPageRegion=90;
                    // deplasamentele pentru liniile ce delimiteaza coloanele din factura
                    // mm*8
                    int iLiniiCaseta=0;
                    // descriere antet factura
                    iHeightPageRegion=iInalt+3*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    mPrinter.setPageXY(0, iCurrTop);
                    mPrinter.printTaggedText("{reset}{center}{b}{w}{u}FACTURA FISCALA {br}");
                    iCurrTop=iCurrTop+iRowHeight+3;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Serie: {/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_serie_facturi), "")
                            + "{br}");
                    iCurrLeft=iCurrLeft+230;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Numar: {/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
                    iCurrLeft=iCurrLeft+250;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight-3 ;
                    iCurrLeft=iCurrLeftAbsolut;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Cota TVA: {/i}" +
                            Siruri.str(Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_COTA_TVA)), 0),4,0)
                            + "{br}");

                    // caseta furnizor

                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iLiniiCaseta=6;
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2),"").equals("")) {
                        iLiniiCaseta=iLiniiCaseta+2;
                    }
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").length()>53)
                        iLiniiCaseta=iLiniiCaseta+1;

                    iHeightPageRegion=iInalt+iLiniiCaseta*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}"+Siruri.padR("FURNIZOR:",16)+"{/u}{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nume_firma),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("C.U.I.:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cf),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Nr.reg.Com:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nrrc),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Adresa:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_adresa),"")
                            + "{br}");
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").length()>53)
                        iCurrTop=iCurrTop+iRowHeight;

//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Judet:",16)+"{/i}" +
//                            settings.getString(context.getString(R.string.key_ecran4_judet),"")
//                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_banca1),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("IBAN:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cont1),"")
                            + "{br}");
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2),"").equals(""))
                    {
                        iCurrTop=iCurrTop+iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",16)+"{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_banca2),"")
                                + "{br}");
                        iCurrTop=iCurrTop+iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("IBAN:",16)+"{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cont2),"")
                                + "{br}");

                    }
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Capital social:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cap_social),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Tel/fax:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_telfax),"")
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);

                    // caseta client
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iLiniiCaseta=5;
                    if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA)).trim().length()>53)
                        iLiniiCaseta=iLiniiCaseta+1;
                    iHeightPageRegion=iInalt+iLiniiCaseta*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}"+Siruri.padR("CUMPARATOR:",16)+"{/u}{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_DENUMIRE))
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("C.U.I.:",16)+"{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_FISc))
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Nr.reg.Com:",16)+"{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC))
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Adresa:",16)+"{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA))
                            + "{br}");
                    if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA)).trim().length()>53)
                        iCurrTop=iCurrTop+iRowHeight;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Judet:",16)+"{/i}" +
//                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_JUDET))
//                        + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",16)+"{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_BANCA))
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("IBAN:", 16) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_CONT))
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);

                    // caseta antet articole
                    Log.d("PRO&","Inainte de antet articole");
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+2*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    int iLinieDen=5*8;
                    int iLinieUM=iLinieDen+34*8;
                    int iLiniePRED=iLinieUM+5*8;
                    int iLinieCant=iLiniePRED+10*8;
                    int iLiniePU=iLinieCant+12*8;
                    int iLinieFara=iLiniePU+12*8;
                    int iLinieTva=iLinieFara+14*8;

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Nr {br}");
                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR(" Denumire articol", 22) + "{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "UM" + "{br}");
                    mPrinter.setPageXY(iLiniePRED, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "%RED" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "  Cant" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Pret unit" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Valoarea" + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Valoarea" + "{br}");

                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "   TVA" + "{br}");

                    mPrinter.drawPageRectangle(iLinieDen - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePRED-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    Log.d("PRO&", "Inainte de caseta articole");
                    // caseta articole
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    int nRows=0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast()) {
                        nRows=nRows+1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length()>24) {
                            nRows=nRows+1;
                        }
                        crs.moveToNext();
                    }
                    // se tine seama ca la urma vin 2 randuri pentru val marfa si val reducere
                    nRows=nRows+2;
                    if (nRows<4) nRows=4;
                    iHeightPageRegion=iInalt+nRows*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.drawPageRectangle(iLinieDen-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePRED-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    crs.moveToFirst();
                    int k=0;
                    double nCotaTva=0;
                    double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
                    String sDen="";
                    while (!crs.isAfterLast()) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen=crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k + 1) + "", 3) + " {br}");
                        mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padR(sDen, 24) + "{br}");
                        mPrinter.setPageXY(iLinieUM, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + " bc" + "{br}");
                        mPrinter.setPageXY(iLiniePRED, iCurrTop);
                        Log.d("PRO&", "Ininte de afis proc red");
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_PROC_RED)), 6, 3)
                                + "{br}");
                        Log.d("PRO&", "Dupa afis proc red");

                        mPrinter.setPageXY(iLinieCant, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)),7,2)
                                +"{br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_PRET_FARA)),8,Biz.ConstCalcul.ZEC_PRET_FARA)
                                +"{br}");
                        mPrinter.setPageXY(iLinieFara, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA)),8,Biz.ConstCalcul.ZEC_VAL_FARA)
                                +"{br}");
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}"+
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA)),6,Biz.ConstCalcul.ZEC_VAL_TVA)
                                +"{br}");

                        if (sDen.length()>29) {
                            iCurrTop=iCurrTop+iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}"+"   "+Siruri.padR(sDen.substring(29),29)+"{br}") ;
                        }
                        nValFara=nValFara+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                        nValTva=nValTva+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                        nValRed=nValRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                        nTvaRed=nTvaRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                        iCurrTop=iCurrTop+iRowHeight;
                        crs.moveToNext();
                        k=k+1;
                    }
                    // pentru reducere
                    mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "         Total marfa:" + "{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "---" + "{br}");
                    mPrinter.setPageXY(iLiniePRED, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-----" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-------" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "--------" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(nValFara, 8, Biz.ConstCalcul.ZEC_VAL_FARA)
                            + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(nValTva, 6, Biz.ConstCalcul.ZEC_VAL_TVA)
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "     Discount acordat:" + "{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "---" + "{br}");
                    mPrinter.setPageXY(iLiniePRED, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-----" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "-------" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "--------" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(nValRed, 8, Biz.ConstCalcul.ZEC_VAL_FARA)
                            + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(nTvaRed, 6, Biz.ConstCalcul.ZEC_VAL_TVA)
                            + "{br}");
                    Log.d("PRO&", "Inainte de subsol");

                    // descriere subsol ( 8 randuri)
                    crs.moveToFirst();
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+8*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnatura si{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    // totaluri fara si tva
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +Siruri.padR(" Total:",10) +"{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA))
                                    , 9, Biz.ConstCalcul.ZEC_VAL_FARA) + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}"+
                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                    ,8,Biz.ConstCalcul.ZEC_VAL_TVA)+"{br}");
                    mPrinter.drawPageRectangle(iLiniePU, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva,0,1,iCurrTop+iRowHeight,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU, iCurrTop + iRowHeight, iWidthPageregion-iLiniePU, 1, Printer.FILL_BLACK);
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Delegat:{br}");
                    mPrinter.drawPageRectangle(200, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "stampila{br}");
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + settings.getString(context.getString(R.string.key_ecran1_numeagent), "") + "{br}");
                    iCurrLeft=428;
                    mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnat.{br}");
                    iCurrLeft=iCurrLeftAbsolut;
                    //iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(210, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "BI/CI:{/i}{/b} " +
                            settings.getString(context.getString(R.string.key_ecran1_biagent), "") +
                            "{br}");
                    iCurrLeft=428;
                    mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "de {br}");
                    mPrinter.setPageXY(iLinieFara+2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total de plata" + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    try {
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Auto: "+
                                settings.getString(context.getString(R.string.key_ecran1_auto),"")
                                + "{br}");
                    } catch (IOException e) {
                        Log.d("PRO&","Eroare:"+e.getMessage());
                        e.printStackTrace();
                    }
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "primire{br}");

                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{w}"+
                            Siruri.str(
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_FARA))+
                                            crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                    ,7,Biz.ConstCalcul.ZEC_VAL_CU)
                            +"{br}");

                    iCurrLeft=210;
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+"Intocmit de:"+"{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}"+
                            settings.getString(context.getString(R.string.key_ecran1_numeagent),"")+"{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}CNP: "+
                            settings.getString(context.getString(R.string.key_ecran1_cnpagent),"")+"{br}");
                    // sectiune pentru declaratia de conformitate
                    String sConf=setConformitate(
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_DENUMIRE)),
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)),
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA)))));
                    if (sConf!="") {
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iHeightPageRegion = iInalt + 2*iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);

                        iCurrLeft = iCurrLeftAbsolut;
                        iCurrTop=8;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{i}{s}"+sConf+"{br}");
                    }
                    // sectiune de reclama
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
//                    iCurrTop=2;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{i}{s}"+"Software pentru agenti de vanzari realizat de PROSOFT SRL . Tel. 0722 236256"+"{br}");
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(150);
                    Log.d("PRO&","Inainte de flush factura: "+mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&","Dupa flush factura: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Dupa reset factura: "+mPrinter.getStatus());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare imprimare: "+ e.getMessage());
                }

            }
        }, 0);
        stare=iOldStare;
    }


    public void printFactura_faraRed ( final Cursor crs ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&","Nu suporta mod pagina");
                    return;
                }
                setStampila();
                final int width = imagewidth;
                final int height = imageheight;
                final int[] argb = imageargb;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                try {
                    crs.moveToFirst();
                    Log.d("PRO&","Inainte de compunere reset listare: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Inainte de compunere dupa reset listare: "+mPrinter.getStatus());
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime

                    int iCurrLeft=iCurrLeftAbsolut;
                    int iCurrTop=iInalt;
                    int iTopPageRegion=0;
                    int iHeightPageRegion=90;
                    // deplasamentele pentru liniile ce delimiteaza coloanele din factura
                    // mm*8
                    int iLiniiCaseta=0;
                    // descriere antet factura
                    iHeightPageRegion=iInalt+3*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    Log.d("PRO&", "Inainte de drawPageframe: " + mPrinter.getStatus());

                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    mPrinter.setPageXY(0, iCurrTop);
                    mPrinter.printTaggedText("{reset}{center}{b}{w}{u}FACTURA FISCALA {br}");
                    iCurrTop=iCurrTop+iRowHeight+3;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Serie: {/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_serie_facturi), "")
                            + "{br}");
                    iCurrLeft=iCurrLeft+230-80;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Numar: {/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
                    iCurrLeft=iCurrLeft+250-80;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight-3 ;
                    iCurrLeft=iCurrLeftAbsolut;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Cota TVA: {/i}" +
                            Siruri.str(Biz.round(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_COTA_TVA)), 0), 2, 0)
                            +" %"+ "{br}");

                            // caseta furnizor

                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iLiniiCaseta=6;
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2),"").equals("")) {
                        iLiniiCaseta=iLiniiCaseta+2;
                    }
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").length()>53)
                        iLiniiCaseta=iLiniiCaseta+1;

                    iHeightPageRegion=iInalt+iLiniiCaseta*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}"+Siruri.padR("FURNIZOR:",16)+"{/u}{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nume_firma),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("C.U.I.:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cf),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Nr.reg.Com:",11)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nrrc),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Adresa:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_adresa),"")
                            + "{br}");
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").length()>53)
                        iCurrTop=iCurrTop+iRowHeight;

//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Judet:",16)+"{/i}" +
//                            settings.getString(context.getString(R.string.key_ecran4_judet),"")
//                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_banca1),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("IBAN:",8)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cont1),"")
                            + "{br}");
                    if (!settings.getString(context.getString(R.string.key_ecran4_banca2),"").equals(""))
                    {
                        iCurrTop=iCurrTop+iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",8)+"{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_banca2),"")
                                + "{br}");
                        iCurrTop=iCurrTop+iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("IBAN:",8)+"{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cont2),"")
                                + "{br}");

                    }
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Capital social:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cap_social),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Tel/fax:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_telfax),"")
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);
                    Log.d("PRO&", "Dupa caseta furniz: " + mPrinter.getStatus());

                    // caseta client
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iLiniiCaseta=5;
                    if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA)).trim().length()>53)
                        iLiniiCaseta=iLiniiCaseta+1;
                    iHeightPageRegion=iInalt+iLiniiCaseta*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}"+Siruri.padR("CUMPARATOR:",16)+"{/u}{/i}" +
                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_DENUMIRE))
                        + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("C.U.I.:",8)+"{/i}" +
                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_FISc))
                        + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Nr.reg.Com:",11)+"{/i}" +
                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC))
                        + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Adresa:",8)+"{/i}" +
                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA))
                        + "{br}");
                    if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_ADRESA)).trim().length()>53)
                        iCurrTop=iCurrTop+iRowHeight;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Judet:",16)+"{/i}" +
//                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_JUDET))
//                        + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Banca:", 8) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_BANCA))
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("IBAN:", 8) + "{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_CONT))
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);
                    Log.d("PRO&", "Dupa caseta client: " + mPrinter.getStatus());

                    // caseta antet articole 3 randuri
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+3*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    int iLinieDen=3*8;
//                    int iLinieUM=iLinieDen+30*8;
                    int iLinieCant=iLinieDen+(29+4)*8;
                    int iLiniePU=iLinieCant+6*8;
                    int iLinieFara=iLiniePU+11*8;
                    int iLinieTva=iLinieFara+9*8;
                    Log.d("PRO&", "Val 1 : " + iLiniePU + ' ' + mPrinter.getStatus());

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Nr {br}");
                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR(" Denumire articol", 22) + "{br}");
//                    mPrinter.setPageXY(iLinieUM, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "UM" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Cant" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"Pret" +"{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Val." + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Val." + "{br}");
// rand 2
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "(bc)" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"fara" +"{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara" + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + " TVA" + "{br}");
// ransd 3
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +" TVA" +"{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +" TVA" +"{br}");


                    mPrinter.drawPageRectangle(iLinieDen-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
//                    mPrinter.drawPageRectangle(iLinieUM-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);

                    // caseta articole
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    int nRows=0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast()) {
                        nRows=nRows+1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length()>29) {
                            nRows=nRows+1;
                        }
                        crs.moveToNext();
                    }
                    if (nRows<2) nRows=2;
                    iHeightPageRegion=iInalt+nRows*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.drawPageRectangle(iLinieDen-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
  //                  mPrinter.drawPageRectangle(iLinieUM-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    crs.moveToFirst();
                    int k=0;
                    double nCotaTva=0;
                    double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0, nPretFara=0,nValFaraLinie=0,nValTvaLinie=0;
                    String sDen="";
                    while (!crs.isAfterLast()) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen=crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft+2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}"+Siruri.padR((k + 1) + "", 2) + " {br}");
                        mPrinter.setPageXY(iLinieDen+2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +Siruri.padR(sDen,29)+"{br}");
//                        mPrinter.setPageXY(iLinieUM, iCurrTop);
//                        mPrinter.printTaggedText("{reset}{left}" +"bc" +"{br}");
                        mPrinter.setPageXY(iLinieCant, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)), 3, 0)
                                + "{br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        if (crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_RED))!=0) {
                            // document cu reducere listat pe model fara
                            nPretFara=(
                                crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA))-
                                crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_RED)                                )
                            ) /crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_CANTITATE));
                            nPretFara=Biz.round(nPretFara,2);
                            nValFaraLinie=crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA))-
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                            nValTvaLinie=crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA))-
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                        } else {
                            nPretFara=crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_PRET_FARA));
                            nValFaraLinie=crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                            nValTvaLinie=crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                        }
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(nPretFara,7,Biz.ConstCalcul.ZEC_PRET_FARA)
                                +"{br}");
                        mPrinter.setPageXY(iLinieFara, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(nValFaraLinie,6,Biz.ConstCalcul.ZEC_VAL_FARA)
                                +"{br}");
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}"+
                                Siruri.str(nValTvaLinie,6,Biz.ConstCalcul.ZEC_VAL_TVA)
                                +"{br}");

                            if (sDen.length()>29) {
                                iCurrTop=iCurrTop+iRowHeight;
                                mPrinter.setPageXY(iLinieDen, iCurrTop);
                                mPrinter.printTaggedText("{reset}{left}"+"   "+Siruri.padR(sDen.substring(29),29)+"{br}") ;
                            }
                        nValFara=nValFara+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                        nValTva = nValTva + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                        nValRed=nValRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                        nTvaRed=nTvaRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                        iCurrTop=iCurrTop+iRowHeight;
                        crs.moveToNext();
                        k = k + 1;

                    }
                    Log.d("PRO&", "Dupa caseta articole: " + mPrinter.getStatus());

                    // descriere subsol ( 8 randuri)
                    crs.moveToFirst();
                    iTopPageRegion = iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+8*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop = iInalt;

                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnatura si{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);

                    // totaluri fara si tva
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Tot:", 4) + "{br}");
                    mPrinter.setPageXY(iLinieFara-8, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA))
                                    , 7, Biz.ConstCalcul.ZEC_VAL_FARA) + "{br}");
                    mPrinter.setPageXY(iLinieTva, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                    , 6, Biz.ConstCalcul.ZEC_VAL_TVA) + "{br}");
                    mPrinter.drawPageRectangle(iLiniePU, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
    //                mPrinter.drawPageRectangle(iLinieFara, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieTva, 0, 1, iCurrTop + iRowHeight, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU, iCurrTop + iRowHeight, iWidthPageregion - iLiniePU, 1, Printer.FILL_BLACK);
                    Log.d("PRO&", "Caseta articole 1: " + mPrinter.getStatus());

                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Delegat:{br}");
                    mPrinter.drawPageRectangle(200, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "stampila{br}");
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + settings.getString(context.getString(R.string.key_ecran1_numeagent), "") + "{br}");
                    iCurrLeft=428;
                    mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "  Total de plata {br}");
                    iCurrLeft=iCurrLeftAbsolut;
                    Log.d("PRO&", "Caseta articole 2: " + mPrinter.getStatus());

                    //iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(210, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "BI/CI:{/i}{/b} " +
                            "{br}");
                    iCurrLeft=428;
// total de plata
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{w}" +
                            Siruri.str(
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA)) +
                                            crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                    , 9, Biz.ConstCalcul.ZEC_VAL_CU)
                            + "{br}");

                    iCurrTop=iCurrTop+iRowHeight;
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}"  +
                            settings.getString(context.getString(R.string.key_ecran1_biagent), "") +
                            "{br}");


                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "  Semnatura de {br}");

                    iCurrLeft=210;
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Auto: "+ "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "      primire" + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            settings.getString(context.getString(R.string.key_ecran1_auto), "") + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}CNP: {br}");

                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" +
                            settings.getString(context.getString(R.string.key_ecran1_cnpagent), "") + "{br}");
                    Log.d("PRO&", "Dupa subsol : " + mPrinter.getStatus());

                    // sectiune pt declaratia de conformitate
                    String sConf=setConformitate(
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_DENUMIRE)),
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)),
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                    );
                    if (sConf!="") {
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iHeightPageRegion = iInalt + 11*iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);

                        iCurrLeft = iCurrLeftAbsolut;
                        iCurrTop=8;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{i}{s}"+sConf+"{br}");
                    }
                    // sectiune de reclama
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
//                    iCurrTop=2;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{i}{s}"+"Software pentru agenti de vanzari realizat de PROSOFT SRL . Tel. 0722 236256"+"{br}");
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(150);
                    Log.d("PRO&","Inainte de flush factura: "+mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&","Dupa flush factura: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Dupa reset factura: "+mPrinter.getStatus());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare imprimare: "+ e.getMessage());
                }

            }
        }, 0);
        stare=iOldStare;
    }
    //comanda
    public void printComanda ( final Cursor crs ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&","Nu suporta mod pagina");
                    return;
                }
                setStampila();
                final int width = imagewidth;
                final int height = imageheight;
                final int[] argb = imageargb;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                try {
                    crs.moveToFirst();
                    Log.d("PRO&","Inainte de compunere reset listare: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Inainte de compunere dupa reset listare: "+mPrinter.getStatus());
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime

                    int iCurrLeft=iCurrLeftAbsolut;
                    int iCurrTop=iInalt;
                    int iTopPageRegion=0;
                    int iHeightPageRegion=90;
                    // deplasamentele pentru liniile ce delimiteaza coloanele din factura
                    // mm*8
                    int iLiniiCaseta=0;
                    // descriere antet factura
                    iHeightPageRegion=iInalt+3*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    mPrinter.setPageXY(0, iCurrTop);
                    mPrinter.printTaggedText("{reset}{center}{b}{w}{u}COMANDA PRODUSE {br}");
                    iCurrTop=iCurrTop+iRowHeight+3;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                            + "{br}");
                    // caseta antet articole
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+2*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    int iLinieDen=5*8;
                    int iLinieCant=iLinieDen+64*8;
                    int iLiniePU=iLinieCant+12*8;

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Nr {br}");
                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +Siruri.padR(" Denumire articol", 32) +"{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"  Cant" +"{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"Pret" +"{br}");

                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");

                    mPrinter.drawPageRectangle(iLinieDen-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);
                    // caseta articole
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    int nRows=0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast()) {
                        nRows=nRows+1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length()>40) {
                            nRows=nRows+1;
                        }
                        crs.moveToNext();
                    }
                    if (nRows<2) nRows=2;
                    iHeightPageRegion=iInalt+nRows*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.drawPageRectangle(iLinieDen-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    crs.moveToFirst();
                    int k=0;
                    double nCotaTva=0;
                    double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
                    String sDen="";
                    while (!crs.isAfterLast()) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen=crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}"+Siruri.padL((k+1)+"",3)+" {br}");
                        mPrinter.setPageXY(iLinieDen+8, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +Siruri.padR(sDen,40)+"{br}");
                        mPrinter.setPageXY(iLinieCant, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)),7,2)
                                +"{br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_PRET_CU)),8,Biz.ConstCalcul.ZEC_PRET_CU)
                                +"{br}");
                        if (sDen.length()>40) {
                            iCurrTop=iCurrTop+iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}"+"   "+Siruri.padR(sDen.substring(40),40)+"{br}") ;
                        }
                        nValFara=nValFara+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                        nValTva=nValTva+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                        nValRed=nValRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                        nTvaRed=nTvaRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                        iCurrTop=iCurrTop+iRowHeight;
                        crs.moveToNext();
                        k=k+1;

                    }
                    // descriere subsol ( 2 randuri)
                    crs.moveToFirst();
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+2*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    // totaluri fara si tva
                    iCurrLeft=iCurrLeftAbsolut;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total " + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{w}"+
                            Siruri.str(
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_FARA))+
                                            crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                    ,7,Biz.ConstCalcul.ZEC_VAL_CU)
                            +"{br}");

                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(150);
                    Log.d("PRO&","Inainte de flush factura: "+mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&","Dupa flush factura: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Dupa reset factura: "+mPrinter.getStatus());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare imprimare: "+ e.getMessage());
                }

            }
        }, 0);
        stare=iOldStare;
    }


    // aviz incarcare
    public void printAvizIncarcare ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&","Nu suporta mod pagina");
                    return;
                }
                setStampila();
                final int width = imagewidth;
                final int height = imageheight;
                final int[] argb = imageargb;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                try {
                    mPrinter.reset();
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    int nRowTot = 0;
                    int iCurrLeft = iCurrLeftAbsolut;
                    int iCurrTop = iInalt;
                    int iTopPageRegion = 0;
                    int iHeightPageRegion = 0;
                    int iLiniicaseta = 0;

                    int iLinieDen = 5 * 8;
                    int iLinieUM = iLinieDen + 49 * 8;
                    int iLinieCant = iLinieUM + 6 * 8;
                    int iLiniePU = iLinieCant + 12 * 8;
                    int iLinieFara = iLiniePU + 14 * 8;

                    // descriere antet factura
                    if (lAntet) {
                        nRowTot = nRowTot + 3;
                        iHeightPageRegion = iInalt + 3 * iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);
                        mPrinter.setPageXY(0, iCurrTop);
                        // daca nrand <>0 atiunci este continuare
                        mPrinter.printTaggedText("{reset}{center}{b}{w}{u}AVIZ DE INSOTIRE AL MARFII {br}");
                        iCurrTop = iCurrTop + iRowHeight + 3;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}Serie: {/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_serie_facturi), "")
                                + "{br}");
                        iCurrLeft = iCurrLeft + 230;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}Numar: {/i}" +
                                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
                        iCurrLeft = iCurrLeft + 250;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                                Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                                + "{br}");

                        // caseta furnizor
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iLiniicaseta = 6;
                        nRowTot = nRowTot + iLiniicaseta;
                        if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").trim().length() > 59) {
                            iLiniicaseta = iLiniicaseta + 1;
                        }
                        iHeightPageRegion = iInalt + iLiniicaseta * iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        iCurrLeft = iCurrLeftAbsolut;
                        iCurrTop = iInalt;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}{u}" + Siruri.padR("FURNIZOR:", 16) + "{/u}{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_nume_firma), "")
                                + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("C.U.I.:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cf), "")
                                + "{br}");
                        mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Nr.reg.Com:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_nrrc), "")
                                + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Adresa:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_adresa), "")
                                + "{br}");
                        if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").trim().length() > 59) {
                            iCurrTop = iCurrTop + iRowHeight;
                        }
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Banca:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_banca1), "")
                                + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("IBAN:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cont1), "")
                                + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Capital social:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cap_social), "")
                                + "{br}");
                        mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Tel/fax:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_telfax), "")
                                + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);

                        // caseta client
                        iLiniicaseta = 3;
                        nRowTot = nRowTot + iLiniicaseta;
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").trim().length() > 59) {
                            iLiniicaseta = iLiniicaseta + 1;
                        }

                        iHeightPageRegion = iInalt + iLiniicaseta * iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        iCurrLeft = iCurrLeftAbsolut;
                        iCurrTop = iInalt;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}{u}" + Siruri.padR("CUMPARATOR:", 16) + "{/u}{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_nume_firma), "")
                                + "{br}");
                        mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}" + Siruri.padR("Gestiune: ", 16) +
                                "Distributie " +
                                "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("C.U.I.:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_cf), "")
                                + "{br}");

                        mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Nr.reg.Com:", 16) + "{/i}" +
                                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC))
                                + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Adresa:", 16) + "{/i}" +
                                settings.getString(context.getString(R.string.key_ecran4_adresa), "")
                                + "{br}");
                        if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").trim().length() > 59) {
                            iCurrTop = iCurrTop + iRowHeight;
                        }

//                    iHeightPageRegion=iCurrTop+iRowHeight;
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);

                    // caseta antet articole
                    // varianta cu imprimare partiala
/*
                     mPrinter.printPage();
                     mPrinter.reset();
                     mPrinter.selectPageMode();
                     iTopPageRegion=0;
                     iHeightPageRegion=0;
                    try {
                        Thread.sleep(Math.round((4 + nRowTot / 4) * 1000)+2000);
                        //Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
*/
                    //
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    iHeightPageRegion = iInalt + 2 * iRowHeight;
                    nRowTot = 2;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Nr {br}");
                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR(" Denumire articol", 22) + "{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "UM" + "{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "  Cant" + "{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Pret unit" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Valoarea" + "{br}");

                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
                    mPrinter.drawPageRectangle(iLinieDen - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    } // if lAntet


                    // caseta articole
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    int nRows = 0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast() && nRows < nRand) {
                        crs.moveToNext();
                        nRows = nRows + 1;
                    }
                    nRows = 0;
                    while (!crs.isAfterLast() && nRows < nLinii) {
                        nRows = nRows + 1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length() > 29) {
                            nRows = nRows + 1;
                        }
                        crs.moveToNext();
                    }
                    if (nRows < 2) nRows = 2;
                    iHeightPageRegion = iInalt + nRows * iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft = iCurrLeftAbsolut;
                    iCurrTop = iInalt;
                    mPrinter.drawPageRectangle(iLinieDen - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    int k = 0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast() && k < nRand) {
                        crs.moveToNext();
                        k = k + 1;
                    }
                    k = 0;
                    double nCotaTva = 0;
                    double nValFara = 0, nValRed = 0, nValTva = 0, nTvaRed = 0;
                    String sDen = "";
                    while (!crs.isAfterLast() && k < nLinii) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen = crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k+(nRand) + 1) + "", 3) + " {br}");
                        mPrinter.setPageXY(iLinieDen, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padR(sDen, 29) + "{br}");
                        mPrinter.setPageXY(iLinieUM, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + " bc" + "{br}");
                        mPrinter.setPageXY(iLinieCant, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)), 7, 2)
                                + "{br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_PRET_FARA)), 9, Biz.ConstCalcul.ZEC_PRET_FARA)
                                + "{br}");
                        mPrinter.setPageXY(iLinieFara, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA)), 9, Biz.ConstCalcul.ZEC_VAL_FARA)
                                + "{br}");

                        if (sDen.length() > 39) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " + Siruri.padR(sDen.substring(39), 39) + "{br}");
                        }
                        nValFara = nValFara + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                        nValTva = nValTva + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                        nValRed = nValRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                        nTvaRed = nTvaRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                        iCurrTop = iCurrTop + iRowHeight;
                        crs.moveToNext();
                        k = k + 1;

                    }
                    nRowTot = nRowTot + k;
                    // varianta cu imprimare partiala
/*
                    mPrinter.printPage();
                    mPrinter.reset();
                    mPrinter.selectPageMode();
                    iTopPageRegion=0;
                    iHeightPageRegion=0;
                    try {
                        Thread.sleep(Math.round((4 + nRowTot / 4) * 1000)+2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
*/
                    //
                    // descriere subsol ( 8 randuri)
                    crs.moveToFirst();
                    if (lSubsol) {
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iHeightPageRegion = iInalt + 8 * iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        iCurrLeft = iCurrLeftAbsolut;
                        iCurrTop = iInalt;
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnatura si{br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        // totaluri fara si tva
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR(" Total:", 10) + "{br}");
                        mPrinter.setPageXY(iLinieFara, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA))
                                        , 9, Biz.ConstCalcul.ZEC_VAL_FARA) + "{br}");
                        mPrinter.drawPageRectangle(iLiniePU, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieFara, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLiniePU, iCurrTop + iRowHeight, iWidthPageregion - iLiniePU, 1, Printer.FILL_BLACK);
                        iCurrLeft = 210;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Delegat:{br}");
                        mPrinter.drawPageRectangle(200, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        iCurrLeft = iCurrLeftAbsolut;
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "stampila{br}");
                        iCurrLeft = 210;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + settings.getString(context.getString(R.string.key_ecran1_numeagent), "") + "{br}");
                        iCurrLeft = 428;
                        mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnat.{br}");
                        iCurrLeft = iCurrLeftAbsolut;
                        //iCurrTop=iCurrTop+iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(210, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "BI/CI:{/i}{/b} " +
                                settings.getString(context.getString(R.string.key_ecran1_biagent), "") +
                                "{br}");
                        iCurrLeft = 428;
                        mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "de {br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        iCurrLeft = 210;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        try {
                            mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Auto: " +
                                    settings.getString(context.getString(R.string.key_ecran1_auto), "")
                                    + "{br}");
                        } catch (IOException e) {
                            Log.d("PRO&", "Eroare:" + e.getMessage());
                            e.printStackTrace();
                        }
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "primire{br}");

//                    mPrinter.setPageXY(iLinieFara, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{w}"+
//                            Siruri.str(
//                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_FARA))+
//                                            crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
//                                    ,7,Biz.ConstCalcul.ZEC_VAL_CU)
//                            +"{br}");

                        iCurrLeft = 210;
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Intocmit de:" + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                settings.getString(context.getString(R.string.key_ecran1_numeagent), "") + "{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}CNP: " +
                                settings.getString(context.getString(R.string.key_ecran1_cnpagent), "") + "{br}");
                        // sectiune de reclama
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iHeightPageRegion = iInalt + iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                    }
                    iCurrLeft = iCurrLeftAbsolut;
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    if (lSubsol) {
                        mPrinter.feedPaper(150);
                    }
                    Log.d("PRO&","Inainte de flush factura: "+mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&","Dupa flush factura: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Dupa reset factura: "+mPrinter.getStatus());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare imprimare: "+ e.getMessage());
                }
            }
        }, 0);
        stare=iOldStare;
    }

    // aviz descarcare
    public void printAvizDescarcare ( final Cursor crs ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&","Nu suporta mod pagina");
                    return;
                }
                setStampila();
                final int width = imagewidth;
                final int height = imageheight;
                final int[] argb = imageargb;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                try {
                    mPrinter.reset();
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime

                    int iCurrLeft=iCurrLeftAbsolut;
                    int iCurrTop=iInalt;
                    int iTopPageRegion=0;
                    int iHeightPageRegion=90;
                    int iLiniicaseta=0;
                    // descriere antet factura
                    iHeightPageRegion=iInalt+3*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    mPrinter.setPageXY(0, iCurrTop);
                    mPrinter.printTaggedText("{reset}{center}{b}{w}{u}AVIZ DE INSOTIRE AL MARFII * {br}");
                    iCurrTop=iCurrTop+iRowHeight+3;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Serie: {/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_serie_facturi), "")
                            + "{br}");
                    iCurrLeft=iCurrLeft+230;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Numar: {/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
                    iCurrLeft=iCurrLeft+250;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                            Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                            + "{br}");

                    // caseta furnizor
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iLiniicaseta=6;
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").trim().length()>59) {
                        iLiniicaseta=iLiniicaseta+1;
                    }
                    iHeightPageRegion=iInalt+iLiniicaseta*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}"+Siruri.padR("FURNIZOR:",16)+"{/u}{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nume_firma),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}"+Siruri.padR("Gestiune: ",16)+
                            "Distributie "+
                            "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("C.U.I.:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cf),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Nr.reg.Com:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nrrc),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Adresa:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_adresa),"")
                            + "{br}");
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").trim().length()>59) {
                        iCurrTop=iCurrTop+iRowHeight;
                    }
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Banca:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_banca1),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("IBAN:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cont1),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Capital social:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cap_social),"")
                            + "{br}");
                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Tel/fax:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_telfax),"")
                            + "{br}");
//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);

                    // caseta client
                    iLiniicaseta=3;
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").trim().length()>59) {
                        iLiniicaseta=iLiniicaseta+1;
                    }

                    iHeightPageRegion=iInalt+iLiniicaseta*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}{u}"+Siruri.padR("CUMPARATOR:",16)+"{/u}{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_nume_firma),"")
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("C.U.I.:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_cf),"")
                            + "{br}");

                    mPrinter.setPageXY(iWidthPageregion/2, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Nr.reg.Com:",16)+"{/i}" +
                            crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC))
                            + "{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+Siruri.padR("Adresa:",16)+"{/i}" +
                            settings.getString(context.getString(R.string.key_ecran4_adresa),"")
                            + "{br}");
                    if (settings.getString(context.getString(R.string.key_ecran4_adresa),"").trim().length()>59) {
                        iCurrTop=iCurrTop+iRowHeight;
                    }

//                    iHeightPageRegion=iCurrTop+iRowHeight;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);
                    // caseta antet articole
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+2*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    int iLinieDen=5*8;
                    int iLinieUM=iLinieDen+49*8;
                    int iLinieCant=iLinieUM+6*8;
                    int iLiniePU=iLinieCant+12*8;
                    int iLinieFara=iLiniePU+14*8;


                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}Nr {br}");
                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +Siruri.padR(" Denumire articol", 22) +"{br}");
                    mPrinter.setPageXY(iLinieUM, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"UM" +"{br}");
                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"  Cant" +"{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"Pret unit" +"{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"Valoarea" +"{br}");

                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"fara TVA" +"{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"fara TVA" +"{br}");
                    mPrinter.drawPageRectangle(iLinieDen-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion-1, iHeightPageRegion-1
                            , Printer.FILL_BLACK, 3);
                    // caseta articole
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    int nRows=0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast()) {
                        nRows=nRows+1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length()>29) {
                            nRows=nRows+1;
                        }
                        crs.moveToNext();
                    }
                    if (nRows<2) nRows=2;
                    iHeightPageRegion=iInalt+nRows*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.drawPageRectangle(iLinieDen-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieUM-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieCant-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU-2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    crs.moveToFirst();
                    int k=0;
                    double nCotaTva=0;
                    double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
                    String sDen="";
                    while (!crs.isAfterLast()) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen=crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}"+Siruri.padL((k+1)+"",3)+" {br}");
                        mPrinter.setPageXY(iLinieDen, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +Siruri.padR(sDen,29)+"{br}");
                        mPrinter.setPageXY(iLinieUM, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +" bc" +"{br}");
                        mPrinter.setPageXY(iLinieCant, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)),7,2)
                                +"{br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_PRET_FARA)),9,Biz.ConstCalcul.ZEC_PRET_FARA)
                                +"{br}");
                        mPrinter.setPageXY(iLinieFara, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA)),9,Biz.ConstCalcul.ZEC_VAL_FARA)
                                +"{br}");

                        if (sDen.length()>39) {
                            iCurrTop=iCurrTop+iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}"+"   "+Siruri.padR(sDen.substring(39),39)+"{br}") ;
                        }
                        nValFara=nValFara+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                        nValTva=nValTva+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                        nValRed=nValRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                        nTvaRed=nTvaRed+crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                        iCurrTop=iCurrTop+iRowHeight;
                        crs.moveToNext();
                        k=k+1;

                    }
                    // descriere subsol ( 8 randuri)
                    crs.moveToFirst();
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+8*iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iInalt;
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);

                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +"Semnatura si{br}");
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    // totaluri fara si tva
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +Siruri.padR(" Total:",10) +"{br}");
                    mPrinter.setPageXY(iLinieFara, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}"+
                            Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Antet.COL_VAL_FARA))
                                    ,9,Biz.ConstCalcul.ZEC_VAL_FARA)+"{br}");
                    mPrinter.drawPageRectangle(iLiniePU, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLinieFara, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    mPrinter.drawPageRectangle(iLiniePU, iCurrTop + iRowHeight, iWidthPageregion-iLiniePU, 1, Printer.FILL_BLACK);
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Delegat:{br}");
                    mPrinter.drawPageRectangle(200, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                    iCurrLeft=iCurrLeftAbsolut;
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "stampila{br}");
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + settings.getString(context.getString(R.string.key_ecran1_numeagent), "") + "{br}");
                    iCurrLeft=428;
                    mPrinter.setPageXY(iLiniePU+1, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Semnat.{br}");
                    iCurrLeft=iCurrLeftAbsolut;
                    //iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(210, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "BI/CI:{/i}{/b} " +
                            settings.getString(context.getString(R.string.key_ecran1_biagent), "") +
                            "{br}");
                    iCurrLeft=428;
                    mPrinter.setPageXY(iLiniePU+1, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "de {br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    iCurrLeft=210;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    try {
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Auto: "+
                                settings.getString(context.getString(R.string.key_ecran1_auto),"")
                                + "{br}");
                    } catch (IOException e) {
                        Log.d("PRO&","Eroare:"+e.getMessage());
                        e.printStackTrace();
                    }
                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}" + "primire{br}");


                    iCurrLeft=210;
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}{b}{i}"+"Intocmit de:"+"{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}"+
                            settings.getString(context.getString(R.string.key_ecran1_numeagent),"")+"{br}");
                    iCurrTop=iCurrTop+iRowHeight;
                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}CNP: "+
                            settings.getString(context.getString(R.string.key_ecran1_cnpagent),"")+"{br}");
                    // sectiune de reclama
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    iHeightPageRegion=iInalt+iRowHeight;
                    mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                            iWidthPageregion, iHeightPageRegion,
                            Printer.PAGE_LEFT);
                    iCurrLeft=iCurrLeftAbsolut;
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(150);
                    Log.d("PRO&","Inainte de flush factura: "+mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&","Dupa flush factura: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Dupa reset factura: "+mPrinter.getStatus());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare imprimare: "+ e.getMessage());
                }
            }
        }, 0);
        stare=iOldStare;
    }


    public void printText() {
        doJob(new Runnable() {
            @Override
            public void run() {
                StringBuffer sb = new StringBuffer();
                sb.append("{reset}{center}{w}{h}RECEIPT");
                sb.append("{br}");
                sb.append("{br}");
                sb.append("{reset}1. {b}First item{br}");
                sb.append("{reset}{right}{h}$0.50 A{br}");
                sb.append("{reset}2. {u}Second item{br}");
                sb.append("{reset}{right}{h}$1.00 B{br}");
                sb.append("{reset}3. {i}Third item{br}");
                sb.append("{reset}{right}{h}$1.50 C{br}");
                sb.append("{br}");
                sb.append("{reset}{right}{w}{h}TOTAL: {/w}$3.00  {br}");
                sb.append("{br}");
                sb.append("{reset}{center}{s}Thank You!{br}");

                try {
                    Log.d("PRO&", "Print Text");
                    Log.d("PRO&", "Stare: " + mPrinter.getStatus());
                    mPrinter.reset();
                    mPrinter.printTaggedText(sb.toString());
                    // modificari
                    mPrinter.drawPageRectangle(10, 10, 30, 50, Printer.FILL_INVERTED);
                    mPrinter.drawPageRectangle(10, 40, 30, 50, Printer.FILL_BLACK);

                    // sfarsit
                    mPrinter.feedPaper(110);
                    mPrinter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare de listare. " + e.getMessage());
                }
            }
        }, 0);
    }
    public int getStare () {
        return stare;
    }
}
