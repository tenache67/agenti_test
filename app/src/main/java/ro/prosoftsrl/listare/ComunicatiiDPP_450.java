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
import java.util.ArrayList;
import java.util.List;
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
public  class ComunicatiiDPP_450 {
    Context context ;
    final int iInalt=10;
    final int iRowHeight=30;
    final int iCurrLeftAbsolut=1;
    final int iLeftPageRegion=0;
    final int iWidthPageregion=832;
    List<Integer> stareLaPrint = new ArrayList<Integer>();

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

    // pentru teste
    ComunicatiiDPP_450(Context context , boolean l) {
        this.context=context;
        this.stare=2;

    }

    ComunicatiiDPP_450(Context context ) {
        this.context=context;
        act=(FragmentActivity) context ;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        this.sAdapter=settings.getString(context.getString(R.string.key_ecran3_bluetadapter),"");
        Log.d("PRO& "+"CommDPP450",sAdapter);
        toast("Conectare la imprimanta");
        Log.d("PRO& "+"CommDPP450","1.1");
        establishBluetoothConnection(sAdapter);
        Log.d("PRO& "+"CommDPP450", "1.2");
        int nStare=getStare();
       while (nStare <= 1) {
            // asteapta conectarea
            nStare=getStare();
        }
        if ( nStare==3 ) {
            toast("Verificati imprimanta ! Nu se poate realiza conexiunea cu imprimanta !");
        }
        Log.d("PRO& "+"CommDPP450", "1.3");
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
                Log.d("PRO& DOJOB","1.8 START");
                arg.putString("titlu", "Va rog asteptati ... ");
                arg.putString("text_pozitiv", "DA");
                arg.putString("text_negativ", "NU");
                arg.putInt("actiune_pozitiv", ConstanteGlobale.Actiuni_la_documente.INCHIDE_DOCUMENT);
                final DialogGeneralDaNu dialog = DialogGeneralDaNu.newinstance(arg);
                dialog.show(ft, "dialoglistare");
                Log.d("PRO& DOJOB", "dupa afisare dialog:" + act.getTitle());

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
                        Log.d("PRO& DOJOB", "1.9 START THREAD ");
                        try {
                            // SE VA FACE OVERRIDE LA RUN
                            job.run();
                            Log.d("PRO& DOJOB", "1.11 FINAL OK THREAD");
                        }
                        catch (Exception e) {
                            Log.d("PRO& DOJOB", "Eroare in doJob "+e.getMessage() );
                        }

                        finally {
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
            Log.d("PRO& INIPRN", "Cu protocol");
        } else {
            Log.d("PRO& INIPRN","Fara protocol");
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
            Log.d("PRO& INIPRN","1 Fara protocol");
        }
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            mPrinterInfo = mPrinter.getInformation(); // functie din clasa printer
        } catch ( Exception e ) {
            Log.d("PRO& INIPRN","eroare getprinterinformation");
            Log.d( "PRO INIPRN", e.getMessage());
        }
        Log.d("PRO& INIPRN","21");
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
        Log.d("PRO& BTCON","1.4");
        doJob(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = null;
                Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                Log.d("PRO& BTCONN", "1.1-1");
                if (pairedDevices.size() > 0) {
                    Log.d("PRO& BTCONN", "1.12");
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
                Log.d("PRO& BTCONN","1.5");
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                InputStream in = null;
                OutputStream out = null;
                adapter.cancelDiscovery();
                try {
                     mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                    boolean ltermin=false;
                    String err ="";
                    int steps=0;
                    Log.d("PRO& BTCONN","1.6");

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
                Log.d("PRO& BTCONN","1.7");

                try {
                    initPrinter(in, out);
                    stare=2;
                    Log.d("PRO& BTCONN","1.8. dupa initializare printer");
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
        if (mBluetoothSocket != null) {
            BluetoothSocket s = mBluetoothSocket;
            try {
                mBluetoothSocket.getInputStream().close();;
                mBluetoothSocket.getOutputStream().close();
                mBluetoothSocket.close();
            } catch ( java.io.IOException e ) {
                Log.d("PRO&", "Erorre Close Bt socket "+e.getMessage());
                e.printStackTrace();
            } finally {
                mBluetoothSocket=null;
            }
        }
    }

    private synchronized void closePrinterConnection() {
        if (mPrinter != null) {
            try {
                mProtocolAdapter.release();
                mPrinter.flush();
            } catch (java.io.IOException e) {
                e.getMessage();
            }
        }

        if (mProtocolAdapter != null) {
//            mProtocolAdapter.release();
        }
    }

    public synchronized void closeActiveConnection() {
        closePrinterConnection();
        closeBlutoothConnection();
    }

    // determina sirul de conformitate ce se trece in josul facturii
    private String setConformitate () {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String sCodFiscal = settings.getString(context.getString(R.string.key_ecran4_cf), "");
        String sConf;
        if (sCodFiscal.toLowerCase().contains("3120044")) {
            sConf="Declaram pe proprie raspundere ca produsele de panificatie livrate conform facturii sunt in conformitate cu legislatia sanitar veterinara si pentru siguranta alimentului in vigoare";
        } else {
            sConf="";
        }
        return sConf;
    }

    // determina bitmap pentru stampila pe baza de cod fiscal
    private void setStampila ( ) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true ;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String sCodFiscal = settings.getString(context.getString(R.string.key_ecran4_cf), "");
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
        } else {
            bitmap  = BitmapFactory.decodeResource(context.getResources(), R.drawable.stamp_goala);

        }

        imagewidth = bitmap.getWidth();
        imageheight = bitmap.getHeight();
        imageargb = new int[imagewidth * imageheight];
        bitmap.getPixels(imageargb, 0, imagewidth, 0, 0, imagewidth, imageheight);
        bitmap.recycle();
    }
    // chitanta
    public void printChitanta ( final Cursor crs ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&", "Nu suporta mod pagina");
                    return;
                }
                int nNextIndex=0;
                stareLaPrint.add(1);
                nNextIndex=stareLaPrint.size()-1;
                try {
                    mPrinter.reset();
                    mPrinter.selectPageMode();
                    pt_printChitanta(crs,0);
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(150);
                    Log.d("PRO&", "Inainte de flush chitanta: " );
                    mPrinter.flush();
                    Log.d("PRO&", "Dupa flush: " );
                    mPrinter.reset();
                    Log.d("PRO&", "Dupa reset chitanta: ");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&", "Eroare imprimare chitanta: " + e.getMessage());
                } finally {
                    stareLaPrint.set(nNextIndex,0);
                    Log.d("PRO&","Index la finally in chitanta :"+nNextIndex);

                }

            }
        }, 0);
        Log.d("PRO&", "dupa runable 1");

        stare=iOldStare;
    }

    // listeaza aviz client
//     public void printAvizIncarcare ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol ) {

    public void printAviz_client ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol ) {
        int iOldStare=getStare();
        stare=4;
//aici voi trece pt decl de conformitate pt betty
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null) {
                    Log.d("PRO& COM", "mprinterinfo = null");
                    return ;
                }
                else if (!mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&", "Nu suporta mod pagina");
                    return;
                }
                int nNextIndex=0;
                try {
                    stareLaPrint.add(1);
                    nNextIndex=stareLaPrint.size()-1;
                    crs.moveToFirst();
                    Log.d("PRO&", "Inainte de compunere reset listare: ");
                    mPrinter.reset();
                    Log.d("PRO&", "Inainte de compunere dupa reset listare: " );
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    setStampila();
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                    int iCurrLeft = iCurrLeftAbsolut;
                    int iCurrTop = iInalt;
                    int iTopPageRegion = 0;
                    int iHeightPageRegion = 90;
                    // deplasamentele pentru liniile ce delimiteaza coloanele din factura
                    // mm*8


                    int iLiniiCaseta = 0;
                    // descriere antet factura
                    int iLinieDen = 5 * 8;
                    int iLinieUM = iLinieDen + 45 * 8; // era 34*8
                    int iLiniePRED = iLinieUM + 5 * 8;
                    int iLinieCant = iLiniePRED + 10 * 8;
                    int iLiniePU = iLinieCant + 12 * 8;
                    int iLinieFara = iLiniePU + 12 * 8;
                    int iLinieTva = iLinieFara + 14 * 8;


                    if (lAntet) {

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
                    } // end lAntet
                    // caseta articole
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    int nRows = 0;
                    int nPozArt =0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast() && nPozArt < nRand) {
                        crs.moveToNext();
                        nPozArt=nPozArt+1 ;
                    }
                    nPozArt=0;
                    nRows = 0;
                    while (!crs.isAfterLast() && nPozArt < nLinii) {
                        nRows = nRows + 1;
                        nPozArt=nPozArt+1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length() > 32) {
                            nRows = nRows + 1;
                        }
                        // se ia in considerare si codul suplimentar
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 3) {
                            nRows = nRows + 1;
                        }

                        crs.moveToNext();
                    }
                    // se tine seama ca la urma vin 2 randuri pentru val marfa si val reducere
                    if (lSubsol) {
                        nRows = nRows + 2;
                    }
                    if (nRows < 4) nRows = 4;
                    Log.d("Pro",lAntet+" "+lSubsol+" Randuri nec av client: "+nRows);
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
                    while (!crs.isAfterLast() && k < nRand) {
                        crs.moveToNext();
                        k = k + 1;
                    }
                    k = 0;
                    double nCotaTva = 0;
                    double nValFara = 0, nValRed = 0, nValTva = 0, nTvaRed = 0;
                    String sDen = "";
                    while (!crs.isAfterLast() && k < nLinii) {
                        Log.d("Pro","Parcurg linii av client:"+k+" din "+nLinii);
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen = crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k + (nRand) + 1) + "", 3) + " {br}");
                        mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padR(sDen, 32) + "{br}");
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

                        if (sDen.length() > 32) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " + Siruri.padR(sDen.substring(32), 32) + "{br}");
                        }
                        // se ia in considerare si codul suplimentar
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 3) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " +
                                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim()+ "{br}");
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
                    if (lSubsol) {
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
                        Log.d("PRO& AVIZC", "Inainte de subsol");

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
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "de primire {br}");

                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iLiniePU + 2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total cu TVA" + "{br}");
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

                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{w}" +
                                Siruri.str(
                                        crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA)) +
                                                crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                        , 7, Biz.ConstCalcul.ZEC_VAL_CU)
                                + "{br}");

                        iCurrLeft = 210;
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
                    }
//                    iCurrTop=2;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{i}{s}"+"Software pentru agenti de vanzari realizat de PROSOFT SRL . Tel. 0722 236256"+"{br}");
                    // se verifica starea impr relativ la celelalte sarcini
                    Log.d("PRO& LISTAV", "Inainte de putPause: ");
                    putPause(nNextIndex,false); // adaugat nou 05.12.2020
                    Log.d("PRO& LISTAV", "Dupa putPause: ");
                    mPrinter.printPage();

                    mPrinter.selectStandardMode();
                    if (lSubsol) {
                        mPrinter.feedPaper(150);
                    }
                    Log.d("PRO&", "Inainte de flush factura: ");
                    mPrinter.flush();
                    Log.d("PRO&", "Dupa flush factura: " );
                    mPrinter.reset(); // am anulat resetul 07.12.2020
                    Log.d("PRO&", "Dupa reset factura: " );
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
                } finally {
                    stareLaPrint.set(nNextIndex,0);
                }

            }
        }, 0);
        stare=iOldStare;
    }


    // listeaza documente
    public void printFactura ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol ){
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
            printFactura_cuRed(crs,nRand,nLinii,lAntet,lSubsol );
            Log.d("PRO&", "Cu red");
        } else {
            printFactura_faraRed(crs,nRand,nLinii,lAntet,lSubsol );
            Log.d("PRO&","Fara red");
        }
    }

    public void printFactura_cuRed ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol  ) {
        int iOldStare=getStare();
        stare=4;

        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&","Nu suporta mod pagina");
                    return;
                }
                int nNextIndex=0;
                stareLaPrint.add(1);
                nNextIndex=stareLaPrint.size()-1;
                // Log.d("PRO FCURED","Verificare stare dupa set stare "+getStareLaPrint());
                try {
                    Log.d("PRO&","Inainte de compunere reset listare: ");
                    mPrinter.reset();
                    Log.d("PRO&","Inainte de compunere dupa reset listare: ");
                    mPrinter.selectPageMode();

                    setStampila();
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    crs.moveToFirst();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime

                    int iCurrLeft=iCurrLeftAbsolut;
                    int iCurrTop=iInalt;
                    int iTopPageRegion=0;
                    int iHeightPageRegion=90;
                    int iLinieDen = 5 * 8;
                    int iLinieUM = iLinieDen + 34 * 8;
                    int iLiniePRED = iLinieUM + 5 * 8;
                    int iLinieCant = iLiniePRED + 10 * 8;
                    int iLiniePU = iLinieCant + 12 * 8;
                    int iLinieFara = iLiniePU + 12 * 8;
                    int iLinieTva = iLinieFara + 14 * 8;
                    int k=0;
                    // deplasamentele pentru liniile ce delimiteaza coloanele din factura
                    // mm*8
                    int iLiniiCaseta=0;
                    // descriere antet factura
                    if (lAntet ) {
                        iHeightPageRegion = iInalt + 3 * iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);
                        mPrinter.setPageXY(0, iCurrTop);
                        mPrinter.printTaggedText("{reset}{center}{b}{w}{u}FACTURA FISCALA {br}");
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

                        iCurrTop = iCurrTop + iRowHeight;
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
                        mPrinter.drawPageRectangle(iLinieUM - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLiniePRED - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieFara - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieTva - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);
                    }
                    Log.d("PRO&", "Inainte de caseta articole");

                    // caseta articole
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    int nRows=0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast() && nRows < nRand) {
                        crs.moveToNext();
                        nRows = nRows + 1;
                    }
                    nRows = 0;
                    k=0;
                    while (!crs.isAfterLast() && k < nLinii) {
                        nRows=nRows+1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length()>24) {
                            nRows=nRows+1;
                        }
                        // se ia in considerare si codul suplimentar
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM1)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM2)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM3)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM4)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        k=k+1;
                        crs.moveToNext();
                    }
                    // se tine seama ca la urma vin 2 randuri pentru val marfa si val reducere
                    if (lSubsol) {
                        nRows=nRows+2;
                        if (nRows<4) nRows=4;
                    }
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
                    k=0;
                    double nCotaTva=0;
                    double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
                    String sDen="";
                    while (!crs.isAfterLast() && k < nRand) {
                        crs.moveToNext();
                        k = k + 1;
                    }
                    k = 0;
                    while (!crs.isAfterLast() && k < nLinii) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen=crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k+(nRand) + 1) + "", 3) + " {br}");
                        mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padR(sDen, 22) + "{br}");
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
                        // se ia in considerare si codul suplimentar
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " +
                                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim()+ "{br}");
                        }

                        iCurrTop=iCurrTop+iRowHeight;
                        crs.moveToNext();
                        k=k+1;
                    }
                    // pentru reducere
                    if (lSubsol) {
                        crs.moveToFirst();
                        while (!crs.isAfterLast()) {
                            nValFara = nValFara + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                            nValTva = nValTva + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                            nValRed = nValRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                            nTvaRed = nTvaRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                            crs.moveToNext();
                        }
                        crs.moveToFirst();
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
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(nTvaRed, 6, Biz.ConstCalcul.ZEC_VAL_TVA)
                                + "{br}");
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
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                        , 8, Biz.ConstCalcul.ZEC_VAL_TVA) + "{br}");
                        Log.d("PRO","Control 1");
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
                        Log.d("PRO","Control 2");

                        mPrinter.setPageXY(iLiniePU + 1, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "de {br}");
                        mPrinter.setPageXY(iLinieFara + 2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total de plata" + "{br}");
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
                        Log.d("PRO","Control 3");

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
                        // sectiune pentru declaratia de conformitate
                        Log.d("PRO","Control 4");

                        String sConf = setConformitate();
                        if (sConf != "") {
                            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                            iHeightPageRegion = iInalt + 2 * iRowHeight;
                            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                    iWidthPageregion, iHeightPageRegion,
                                    Printer.PAGE_LEFT);
                            mPrinter.drawPageFrame(0, 0,
                                    iWidthPageregion - 1, iHeightPageRegion - 1
                                    , Printer.FILL_BLACK, 3);

                            iCurrLeft = iCurrLeftAbsolut;
                            iCurrTop = 8;
                            mPrinter.setPageXY(iCurrLeft, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}{i}{s}" + sConf + "{br}");
                        }
                        // sectiune de reclama
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iHeightPageRegion = iInalt + iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        iCurrLeft = iCurrLeftAbsolut;
                        //iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        Log.d("PRO","Control 5");

                    }

//                    pt_printFactCuRed(crs,nRand,nLinii,lAntet,lSubsol,0);
//                    iCurrTop=2;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{i}{s}"+"Software pentru agenti de vanzari realizat de PROSOFT SRL . Tel. 0722 236256"+"{br}");

                    Log.d("PRO&","Inainte printpage fact cu red: ");
                    putPause(nNextIndex,false); // adaugat nou 05.12.2020
                    mPrinter.printPage();
                    Log.d("PRO&","Dupa printpage fact cu red: ");
                    mPrinter.selectStandardMode();
                    if (lSubsol) {
                        mPrinter.feedPaper(150);
                    }
                    Log.d("PRO&","Inainte de flush factura: ");
                    mPrinter.flush();
                    Log.d("PRO&","Dupa flush factura cu red: ");
                    mPrinter.reset();
                    Log.d("PRO&","Dupa reset factura: ");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare imprimare list factura cu red: ");
                } finally {
                    stareLaPrint.set(nNextIndex,0);
                }

            }
        }, 0);
        stare=iOldStare;
    }


    public void printFactura_faraRed ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol ) {
        int iOldStare=getStare();
        stare=4;
        doJob(new Runnable() {
            @Override
            public void run() {
                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    Log.d("PRO&","Nu suporta mod pagina");
                    return;
                }
                int nNextIndex=0;
                try {
                    setStampila();
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    stareLaPrint.add(1);
                    nNextIndex=stareLaPrint.size()-1;
                    crs.moveToFirst();
                    Log.d("PRO&","Inainte de compunere reset listare fact fara red : "+mPrinter.getStatus());
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
                    int iLinieDen = 5 * 8;
                    int iLinieUM = iLinieDen + 44 * 8;
                    int iLinieCant = iLinieUM + 5 * 8;
                    int iLiniePU = iLinieCant + 12 * 8;
                    int iLinieFara = iLiniePU + 12 * 8;
                    int iLinieTva = iLinieFara + 14 * 8;
                    int k=0;
                    // descriere antet factura
                    if (lAntet ) {
                        iHeightPageRegion = iInalt + 3 * iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);
                        mPrinter.setPageXY(0, iCurrTop);
                        mPrinter.printTaggedText("{reset}{center}{b}{w}{u}FACTURA FISCALA {br}");
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
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iHeightPageRegion = iInalt + 2 * iRowHeight;
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
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Valoarea" + "{br}");


//                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +
//                            "Nr " +
//                            Siruri.padR(" Denumire articol", 29) +
//                            Siruri.padR("UM", 3) +
//                            Siruri.padR("Cant.", 6) +
//                            Siruri.padR("Pret unit", 10) +
//                            Siruri.padR("Valoarea", 9) +
//                            Siruri.padR("Valoarea", 9) +
//                            "{/i}{br}");
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");
                        mPrinter.setPageXY(iLiniePU, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
                        mPrinter.setPageXY(iLinieFara, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "fara TVA" + "{br}");
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "   TVA" + "{br}");

//                    mPrinter.printTaggedText("{reset}{left}{b}{i}" +
//                            Siruri.padR("cr", 32) +
//                            Siruri.padR("", 3) +
//                            Siruri.padR("", 6) +
//                            Siruri.padR("fara TVA", 10) +
//                            Siruri.padR("fara TVA", 9) +
//                            Siruri.padR("   TVA", 9) +
//                            "{/i}{br}");
                        mPrinter.drawPageRectangle(iLinieDen - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieUM - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieFara - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageRectangle(iLinieTva - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
                        mPrinter.drawPageFrame(0, 0,
                                iWidthPageregion - 1, iHeightPageRegion - 1
                                , Printer.FILL_BLACK, 3);
                        // caseta articole
                    }
                    iTopPageRegion=iTopPageRegion+iHeightPageRegion;
                    int nRows=0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast() && nRows < nRand) {
                        crs.moveToNext();
                        nRows = nRows + 1;
                    }
                    nRows = 0;
                    k=0;
                    while (!crs.isAfterLast() && k < nLinii) {
                        nRows=nRows+1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length()>29) {
                            nRows=nRows+1;
                        }
                        // se ia in considerare si codul suplimentar
                        int suplimini ;
                            suplimini = crs.getColumnIndex
                                    (ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+
                                            ColectieAgentHelper.Table_Produse.COL_SUPLIM2 ) ;
                            Log.d("DPP","Poz"+crs.getPosition()+" "+ suplimini);

                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM1)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM2)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM3)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM4)).trim().isEmpty()){
                            nRows = nRows + 1;
                        }
                        Log.d("PRO","Randuri numarate:"+nRows);
                        k=k+1;
                        crs.moveToNext();
                    }
                    if (nRows<2 && lSubsol) nRows=2;
                    Log.d("PRO","Randuri pe pag:"+nRows);
                    iHeightPageRegion=iInalt+(nRows)*iRowHeight;
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
                    mPrinter.drawPageRectangle(iLinieTva-2,0,1,iHeightPageRegion,Printer.FILL_BLACK);
                    mPrinter.drawPageFrame(0, 0,
                            iWidthPageregion - 1, iHeightPageRegion - 1
                            , Printer.FILL_BLACK, 3);
                    crs.moveToFirst();
                    k=0;
                    while (!crs.isAfterLast() && k < nRand) {
                        crs.moveToNext();
                        k = k + 1;
                    }
                    k = 0;
                    double nCotaTva=0;
                    double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
                    String sDen="";
                    Log.d("Pro","Incepe cu "+crs.getPosition());
                    while (!crs.isAfterLast() && k < nLinii) {
                        Log.d("Pro list f fara","poz: "+crs.getPosition());
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen=crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k+(nRand) + 1) + "", 3) + " {br}");
                        mPrinter.setPageXY(iLinieDen+8, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +Siruri.padR(sDen,29)+"{br}");
                        mPrinter.setPageXY(iLinieUM, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +" bc" +"{br}");
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


//                        mPrinter.printTaggedText("{reset}{left}" +
//                                Siruri.padL((k+1)+"",3)+
//                                Siruri.padR(sDen,29) +
//                                Siruri.padR("bc",3) +
//                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)),6,2)+
//                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_PRET_FARA)),10,Biz.ConstCalcul.ZEC_PRET_FARA)+
//                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA)),9,Biz.ConstCalcul.ZEC_VAL_FARA)+
//                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA)),9,Biz.ConstCalcul.ZEC_VAL_TVA)+
//                                "{br}");
                            if (sDen.length()>29) {
                                iCurrTop=iCurrTop+iRowHeight;
                                mPrinter.setPageXY(iLinieDen, iCurrTop);
                                mPrinter.printTaggedText("{reset}{left}"+"   "+Siruri.padR(sDen.substring(29),29)+"{br}") ;
                            }
                        // se ia in considerare si codul suplimentar
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " +
                                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim()+ "{br}");
                        }
                        // linii suplimentare la produs . o linie poate contine C:= sau P:= ( cantitate sau pret)
                        for (int i=1;i<=4;i++) {
                            String sLin=crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+
                                    "suplim"+i)).trim();
                            if (!sLin.isEmpty()){
                                iCurrTop = iCurrTop + iRowHeight;
                                sDen=Siruri.getSuplimPart(sLin,"T"); // text
                                if (!sDen.isEmpty()) {
                                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                                    mPrinter.printTaggedText("{reset}{left}{i}" + Siruri.padR(sDen, 29) + "{/i}{br}");
                                }
                                sDen=Siruri.getSuplimPart(sLin,"C"); // cant
                                if (!sDen.isEmpty()) {
                                    mPrinter.setPageXY(iLinieCant, iCurrTop);
                                    mPrinter.printTaggedText("{reset}{left}{i}" + sDen + "{/i}{br}");
                                }
                                sDen=Siruri.getSuplimPart(sLin,"P"); // pret
                                if (!sDen.isEmpty()) {
                                    mPrinter.setPageXY(iLiniePU, iCurrTop);
                                    mPrinter.printTaggedText("{reset}{left}{i}" + sDen + "{/i}{br}");
                                }
                                nRows = nRows + 1;
                            }

                        }
                        crs.moveToNext();
                        iCurrTop = iCurrTop + iRowHeight;
                        k=k+1;

                    }
                    // descriere subsol ( 8 randuri)
                    if (lSubsol) {
                        crs.moveToFirst();
                        while (!crs.isAfterLast()) {
                            nValFara = nValFara + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                            nValTva = nValTva + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                            nValRed = nValRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                            nTvaRed = nTvaRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                            crs.moveToNext();
                        }
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
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                                        , 8, Biz.ConstCalcul.ZEC_VAL_TVA) + "{br}");
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
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total de plata" + "{br}");
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
                        // sectiune pt declaratia de conformitate
                        String sConf = setConformitate();
                        if (sConf != "") {
                            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                            iHeightPageRegion = iInalt + 2 * iRowHeight;
                            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                    iWidthPageregion, iHeightPageRegion,
                                    Printer.PAGE_LEFT);
                            mPrinter.drawPageFrame(0, 0,
                                    iWidthPageregion - 1, iHeightPageRegion - 1
                                    , Printer.FILL_BLACK, 3);

                            iCurrLeft = iCurrLeftAbsolut;
                            iCurrTop = 8;
                            mPrinter.setPageXY(iCurrLeft, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}{i}{s}" + sConf + "{br}");
                        }
                        // sectiune de reclama
                        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                        iHeightPageRegion = iInalt + iRowHeight;
                        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                                iWidthPageregion, iHeightPageRegion,
                                Printer.PAGE_LEFT);
                        iCurrLeft = iCurrLeftAbsolut;
                    }
//                    iCurrTop=2;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{i}{s}"+"Software pentru agenti de vanzari realizat de PROSOFT SRL . Tel. 0722 236256"+"{br}");
                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    if (lSubsol) {
                        mPrinter.feedPaper(150);
                    }
                    Log.d("PRO&","Inainte de flush factura: "+mPrinter.getStatus());
                    mPrinter.flush();
                    Log.d("PRO&","Dupa flush factura: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Dupa reset factura fara red: "+mPrinter.getStatus());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&","Eroare imprimare: "+ e.getMessage());
                } finally {
                    stareLaPrint.set(nNextIndex,0);
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
                int nNextIndex=0;
                try {
                    stareLaPrint.add(1);
                    nNextIndex=stareLaPrint.size()-1;
                    crs.moveToFirst();
                    Log.d("PRO&","Inainte de compunere reset listare fat cu red: "+mPrinter.getStatus());
                    mPrinter.reset();
                    Log.d("PRO&","Inainte de compunere dupa reset listare: "+mPrinter.getStatus());
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    setStampila();
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

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
                } finally {
                    stareLaPrint.set(nNextIndex,0);
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
                int nNextIndex=0;

                try {
                    stareLaPrint.add(1);
                    nNextIndex=stareLaPrint.size()-1;
                    mPrinter.reset();
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    setStampila();
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
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
                } finally {
                    stareLaPrint.set(nNextIndex,0);
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
                int nNextIndex=0;
                try {
                    stareLaPrint.add(1);
                    nNextIndex=stareLaPrint.size()-1;
                    mPrinter.reset();
                    mPrinter.selectPageMode();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    crs.moveToFirst();
                    setStampila();
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

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
                } finally {
                    stareLaPrint.set(nNextIndex,0);
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
                    mPrinter.reset();
                    mPrinter.printTaggedText(sb.toString());
                    // modificari
                    mPrinter.drawPageRectangle(10, 10, 30, 50, Printer.FILL_INVERTED);
                    mPrinter.drawPageRectangle(10, 40, 30, 50, Printer.FILL_BLACK);

                    // sfarsit
                        mPrinter.feedPaper(150);
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
    // in lista stareLaprint se adauga un element cu valoare 1 cant incepe codul de printare
    // la finalul metodei de printare acest element se face 0 .
    // se verifica daca in lista exista cvreun element cu val 1 ceea ce inseamna ca se mai fac prelucrari si nu se poate
    // trece mai departe sau inchide bluetooth. Daca intoarec 1 se mai lucreaza , daca intoarce 0 este libber

    public int getStareLaPrint () {
//        boolean lStare= stareLaPrint.contains(1);
//        if (lStare ) return  1 ; else return 0 ;
        return (stareLaPrint.contains(1) ? 1 : 0) ;
    }

    // se determina starea imprimantei doar pe un index
    public int getStareLaPrint (int nIndex, boolean lPeIndex) {
        int nStare =0 ;
        if (lPeIndex) {
            if (stareLaPrint.size()>nIndex) {
                nStare=(stareLaPrint.get(nIndex)!=0 ? 1 : 0);
            }
        } else {
            for (int i=0 ; i<stareLaPrint.size() ; i++) {
                if (nStare==0 && i!=nIndex ) {
                    nStare=getStareLaPrint(i,true);
                }
            }
        }

        return nStare;
    }



        // face poauza pana se elibereaza indexul dat , lPeIndex=true sau pana se elibereraza celalti indecsi lPeindex=false
    public void putPause (int nIndex,boolean lPeIndex ) {
        int nStare=getStareLaPrint(nIndex,lPeIndex);
        for (int j=1 ; j<=10 || (nStare!=0) ; j++)
        {
            try {
                Thread.sleep(100);
            }
            catch (Exception e) {

                Log.d("PRO$ COM PAUZAPR",e.getMessage());
            }
            nStare=getStareLaPrint(nIndex,lPeIndex);
            Log.d("PRO$ COM PAUZAPR","Pas:"+j+"  Stare imprimanta in pauza : "+nStare);
        } ;



    }

    // face pauza pana se elibereaza toate metodele care folosesc imprimanta
    public void putPause (int nTime) {
        try {
            Thread.sleep( nTime );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void putPause () {
        int nStare=getStareLaPrint();
        for (int j=1 ; j<=10 || (nStare!=0) ; j++)
        {
            try {
                Thread.sleep(100);
            }
            catch (Exception e) {

                Log.d("PRO$ COM PAUZAPR",e.getMessage());
            }
            nStare=getStareLaPrint();
            Log.d("PRO$ COM PAUZAPR","Pas:"+j+"  Stare imprimanta in pauza : "+nStare);
        } ;

    }
    public int pt_printFactCuRed  ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol,final int iCurTopPageRegion  )
        throws IOException {
        setStampila();
        final int width = imagewidth;
        final int height = imageheight;
        final int[] argb = imageargb;
       SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        crs.moveToFirst();
        // 80-pixeli= 1 cm pe latime
        // 100-pixeli = 12 mm pe inaltime

        int iCurrLeft=iCurrLeftAbsolut;
        int iCurrTop=iInalt;
        int iTopPageRegion=0;
        int iHeightPageRegion=90;
        int iLinieDen = 5 * 8;
        int iLinieUM = iLinieDen + 34 * 8;
        int iLiniePRED = iLinieUM + 5 * 8;
        int iLinieCant = iLiniePRED + 10 * 8;
        int iLiniePU = iLinieCant + 12 * 8;
        int iLinieFara = iLiniePU + 12 * 8;
        int iLinieTva = iLinieFara + 14 * 8;
        int k=0;
        // deplasamentele pentru liniile ce delimiteaza coloanele din factura
        // mm*8
        int iLiniiCaseta=0;
        // descriere antet factura
        if (lAntet ) {
            iHeightPageRegion = iInalt + 3 * iRowHeight;
            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                    iWidthPageregion, iHeightPageRegion,
                    Printer.PAGE_LEFT);
            mPrinter.drawPageFrame(0, 0,
                    iWidthPageregion - 1, iHeightPageRegion - 1
                    , Printer.FILL_BLACK, 3);
            mPrinter.setPageXY(0, iCurrTop);
            mPrinter.printTaggedText("{reset}{center}{b}{w}{u}FACTURA FISCALA {br}");
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

            iCurrTop = iCurrTop + iRowHeight;
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
            mPrinter.drawPageRectangle(iLinieUM - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLiniePRED - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLinieFara - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLinieTva - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageFrame(0, 0,
                    iWidthPageregion - 1, iHeightPageRegion - 1
                    , Printer.FILL_BLACK, 3);
        }
        Log.d("PRO&", "Inainte de caseta articole");

        // caseta articole
        iTopPageRegion=iTopPageRegion+iHeightPageRegion;
        int nRows=0;
        crs.moveToFirst();
        while (!crs.isAfterLast() && nRows < nRand) {
            crs.moveToNext();
            nRows = nRows + 1;
        }
        nRows = 0;
        k=0;
        while (!crs.isAfterLast() && k < nLinii) {
            nRows=nRows+1;
            if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length()>24) {
                nRows=nRows+1;
            }
            // se ia in considerare si codul suplimentar
            if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                nRows = nRows + 1;
            }
            if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM1)).trim().isEmpty()){
                nRows = nRows + 1;
            }
            if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM2)).trim().isEmpty()){
                nRows = nRows + 1;
            }
            if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM3)).trim().isEmpty()){
                nRows = nRows + 1;
            }
            if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ColectieAgentHelper.Table_Produse.COL_SUPLIM4)).trim().isEmpty()){
                nRows = nRows + 1;
            }
            k=k+1;
            crs.moveToNext();
        }
        // se tine seama ca la urma vin 2 randuri pentru val marfa si val reducere
        if (lSubsol) {
            nRows=nRows+2;
            if (nRows<4) nRows=4;
        }
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
        k=0;
        double nCotaTva=0;
        double nValFara=0, nValRed=0, nValTva=0 , nTvaRed=0;
        String sDen="";
        while (!crs.isAfterLast() && k < nRand) {
            crs.moveToNext();
            k = k + 1;
        }
        k = 0;
        while (!crs.isAfterLast() && k < nLinii) {
            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            sDen=crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME+"_"+ ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k+(nRand) + 1) + "", 3) + " {br}");
            mPrinter.setPageXY(iLinieDen + 8, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}" + Siruri.padR(sDen, 22) + "{br}");
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
            // se ia in considerare si codul suplimentar
            if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                iCurrTop = iCurrTop + iRowHeight;
                mPrinter.setPageXY(iLinieDen, iCurrTop);
                mPrinter.printTaggedText("{reset}{left}" + "   " +
                        crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim()+ "{br}");
            }

            iCurrTop=iCurrTop+iRowHeight;
            crs.moveToNext();
            k=k+1;
        }
        // pentru reducere
        if (lSubsol) {
            crs.moveToFirst();
            while (!crs.isAfterLast()) {
                nValFara = nValFara + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                nValTva = nValTva + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                nValRed = nValRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                nTvaRed = nTvaRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                crs.moveToNext();
            }
            crs.moveToFirst();
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
            mPrinter.setPageXY(iLinieTva, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}" +
                    Siruri.str(nTvaRed, 6, Biz.ConstCalcul.ZEC_VAL_TVA)
                    + "{br}");
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
            mPrinter.setPageXY(iLinieTva, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}" +
                    Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                            , 8, Biz.ConstCalcul.ZEC_VAL_TVA) + "{br}");
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
            mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total de plata" + "{br}");
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
            // sectiune pentru declaratia de conformitate
            String sConf = setConformitate();
            if (sConf != "") {
                iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                iHeightPageRegion = iInalt + 2 * iRowHeight;
                mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                        iWidthPageregion, iHeightPageRegion,
                        Printer.PAGE_LEFT);
                mPrinter.drawPageFrame(0, 0,
                        iWidthPageregion - 1, iHeightPageRegion - 1
                        , Printer.FILL_BLACK, 3);

                iCurrLeft = iCurrLeftAbsolut;
                iCurrTop = 8;
                mPrinter.setPageXY(iCurrLeft, iCurrTop);
                mPrinter.printTaggedText("{reset}{left}{i}{s}" + sConf + "{br}");
            }
            // sectiune de reclama
            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
            iHeightPageRegion = iInalt + iRowHeight;
            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                    iWidthPageregion, iHeightPageRegion,
                    Printer.PAGE_LEFT);
            iCurrLeft = iCurrLeftAbsolut;
            iTopPageRegion = iTopPageRegion + iHeightPageRegion;

        }
        return iTopPageRegion;
    }
    public void pt_printChitanta (Cursor crs, int iCurTopPageRegion) throws IOException {
        setStampila();
        Log.d("PRO&", "dupa set stampila chitanta");
        final int width = imagewidth;
        final int height = imageheight;
        final int[] argb = imageargb;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("PRO&", "Inainte de compunere listare chitanta: ");
        // 80-pixeli= 1 cm pe latime
        // 100-pixeli = 12 mm pe inaltime
        int iCurrLeft = iCurrLeftAbsolut;
        int iCurrTop = iInalt;
        int iTopPageRegion = iCurTopPageRegion;
        int iHeightPageRegion = 90;
        int iLiniiCaseta = 0;
        // descriere antet chitanta
        crs.moveToFirst();
        Log.d("PRO&", "chitanta 1 ");
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
                settings.getString(context.getString(R.string.key_ecran4_serie_facturi), "")
                + "{br}");
        iCurrLeft = iCurrLeft + 230;
        Log.d("PRO&", "chitanta 2 ");
        mPrinter.setPageXY(iCurrLeft, iCurrTop);
        mPrinter.printTaggedText("{reset}{left}{b}{i}Numar: {/i}" +
                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
        Log.d("PRO&", "chitanta 2 .1 " );
        iCurrLeft = iCurrLeft + 250;
        mPrinter.setPageXY(iCurrLeft, iCurrTop);
        mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                + "{br}");
        Log.d("PRO&", "chitanta 3 ");

        // caseta furnizor
        iLiniiCaseta = 6;
        iTopPageRegion = iTopPageRegion + iHeightPageRegion;
        if (!settings.getString(context.getString(R.string.key_ecran4_banca2), "").equals("")) {
            iLiniiCaseta = iLiniiCaseta + 2;
        }
        Log.d("PRO&", "chitanta 3.1 " );
        if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").length() > 53)
            iLiniiCaseta = iLiniiCaseta + 1;
        iHeightPageRegion = iInalt + iLiniiCaseta * iRowHeight;
        Log.d("PRO&", "chitanta 3.2 ");

        mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                iWidthPageregion, iHeightPageRegion,
                Printer.PAGE_LEFT);
        Log.d("PRO&", "chitanta 3.3 ");
        iCurrLeft = iCurrLeftAbsolut;
        iCurrTop = iInalt;
        mPrinter.setPageXY(iCurrLeft, iCurrTop);
        Log.d("PRO&", "chitanta 3.4 ");
        mPrinter.printTaggedText("{reset}{left}{b}{i}{u}" + Siruri.padR("FURNIZOR:", 16) + "{/u}{/i}" +
                settings.getString(context.getString(R.string.key_ecran4_nume_firma), "")
                + "{br}");
        iCurrTop = iCurrTop + iRowHeight;
        Log.d("PRO&", "chitanta 3.5 " );
        mPrinter.setPageXY(iCurrLeft, iCurrTop);
        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("C.U.I.:", 16) + "{/i}" +
                settings.getString(context.getString(R.string.key_ecran4_cf), "")
                + "{br}");
        Log.d("PRO&", "chitanta 3.6 " );
        mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Nr.reg.Com:", 16) + "{/i}" +
                settings.getString(context.getString(R.string.key_ecran4_nrrc), "")
                + "{br}");
        iCurrTop = iCurrTop + iRowHeight;
        Log.d("PRO&", "chitanta 3.7 ");
        mPrinter.setPageXY(iCurrLeft, iCurrTop);
        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Adresa:", 16) + "{/i}" +
                settings.getString(context.getString(R.string.key_ecran4_adresa), "")
                + "{br}");
        Log.d("PRO&", "chitanta 3.8 ");
        if (settings.getString(context.getString(R.string.key_ecran4_adresa), "").length() > 53) {
            Log.d("PRO&", "chitanta 3.9 ");
            iCurrTop = iCurrTop + iRowHeight;
        }
        Log.d("PRO&", "chitanta 3.10 ");

        iCurrTop = iCurrTop + iRowHeight;
        Log.d("PRO&", "chitanta 4 ");

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
        Log.d("PRO&", "chitanta 5 " );

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
        Log.d("PRO&", "chitanta 6 " );

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
                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_FISc)) +
                "{b}{i} Nr. ORC/an: " +
                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC))
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
        Log.d("PRO&", "chitanta 7 " );

        //pozitionare stampila
        mPrinter.setPageXY(iWidthPageregion - width - 20, iCurrTop);
        mPrinter.printImage(argb, width, height, Printer.ALIGN_RIGHT, true);
        iCurrTop = iCurrTop + 3 * iRowHeight;
        mPrinter.setPageXY(iCurrLeft, iCurrTop);
        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Reprezentand : " + "{/i}{/b}" +
                " CV. Fact. " +
                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
        mPrinter.drawPageFrame(0, iCurTopPageRegion,
                iWidthPageregion - 1, iHeightPageRegion - 1
                , Printer.FILL_BLACK, 3);
        Log.d("PRO&", "chitanta 8 " );


    }
}
