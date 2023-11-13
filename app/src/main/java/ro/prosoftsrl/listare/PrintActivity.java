package ro.prosoftsrl.listare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.FragmentActivity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import ro.prosoftsrl.agenti.R;
import ro.prosoftsrl.diverse.ConvertNumar;
import ro.prosoftsrl.diverse.Siruri;

public class PrintActivity extends FragmentActivity {
    Intent returnIntent = new Intent();
    private static final String LOG_TAG = "PrinterSample";
    private static final boolean DEBUG = true;

    // Request to get the bluetooth device
    private static final int REQUEST_GET_DEVICE = 0;
    // Member varibles
    private Printer mPrinter;
    private ProtocolAdapter mProtocolAdapter;
    private PrinterInformation mPrinterInfo;
    private BluetoothSocket mBluetoothSocket;
    private PrinterServer mPrinterServer;
    private Socket mPrinterSocket;
    private boolean mRestart;
    private String macadress; // adresa mac pt bt
    private int[] imageargb;
    private int imagewidth;
    private int imageheight;
    private int nRetry =1 ; // nr de incercari de conectare  se fac 3 dupa care se opreste
    final int iInalt = 10;
    int iInaltInPagina=iInalt;
    final int iRowHeight = 30;
    final int iCurrLeftAbsolut = 1;
    final int iLeftPageRegion = 0;
    final int iWidthPageregion = 832;
    final Context context = this;
//    SQLiteDatabase db ;
    // vine din apel idAntet
    long idAntet ;
//    Cursor crs ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        // adresa mac a dip bluet
        returnIntent.putExtra("IdAntet", idAntet);
        // vine din apel idAntet
        long idAntet = getIntent().getLongExtra("IdAntet", 0);

        Button btncont = (Button) findViewById(R.id.btnListeazaPrintActivity);
        btncont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    printPage();
                Log.d("PRINT", "La on click");
                setResult(Activity.RESULT_OK, returnIntent);
//                printPage();
                // se trimite inapoi idAntet
                SQLiteDatabase db = new ColectieAgentHelper(context).getWritableDatabase();
                Cursor crs = db.rawQuery(Biz.getSqlImagineDoc(idAntet), null);
                printDocument(idAntet, crs);

//                crs.close();
//                db.close();
 //               finish();
            }
        });
        // alternativa la listare
        Button btnList2 = (Button) findViewById(R.id.btnListare2PrintActivity);
        btnList2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    printPage();
                Log.d("PRINT", "La on click");
                setResult(Activity.RESULT_OK, returnIntent);
//                printPage();
                // se trimite inapoi idAntet
                SQLiteDatabase db = new ColectieAgentHelper(context).getWritableDatabase();
                Cursor crs = db.rawQuery(Biz.getSqlImagineDoc(idAntet), null);
                printDocument(idAntet, crs,5,1,true);

//                crs.close();
//                db.close();
                //               finish();
            }
        });

        Button btnreconn = (Button) findViewById(R.id.btnReconnectPrintActivity);
        btnreconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nRetry=1;
                waitForConnection();
            }
        });
        Button btnclose = (Button) findViewById(R.id.btnClosePrintActivity);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED, returnIntent);
//                printPage();
                // se trimite inapoi idAntet
                returnIntent.putExtra("IdAntet", idAntet);
                //crs.close();
                // db.close();
                finish();
            }
        });


        mRestart = true;
        macadress = getMacAdress();
        if ( !macadress.equals("")) {
            if (BluetoothAdapter.checkBluetoothAddress(macadress)) {
            }

            waitForConnection();
        } else {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            String sAdapter = settings.getString(this.getString(R.string.key_ecran3_bluetadapter), "");
            Toast.makeText(getApplicationContext(), "Verificati daca exista "+sAdapter+" la dispozitive Bluetooth imperecheate", Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            returnIntent.putExtra("IdAntet", idAntet);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // blocare buton de back

    }

    @SuppressLint("MissingPermission")
    private String getMacAdress() {
        // se cauta dresa deviceului bt
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String sAdapter = settings.getString(this.getString(R.string.key_ecran3_bluetadapter), "");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = null;
        String sMac="";
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice mdevice : pairedDevices) {
                // MP300 is the name of the bluetooth printer device
                if (mdevice.getName().equals(sAdapter)) {
                    Log.d("PRO&", "1.14");
                    device = mdevice;
                    break;
                }
            }
        }
        sMac=device!=null ? device.getAddress(): "";
        return sMac;
    }

    // The listener for all printer events
    private final ProtocolAdapter.ChannelListener mChannelListener = new ProtocolAdapter.ChannelListener() {
        @Override
        public void onReadEncryptedCard() {
            toast(getString(R.string.msg_read_encrypted_card));
        }

        @Override
        public void onReadCard() {

        }

        @Override
        public void onReadBarcode() {

        }

        @Override
        public void onPaperReady(boolean state) {
            if (state) {
                toast(getString(R.string.msg_paper_ready));
            } else {
                toast(getString(R.string.msg_no_paper));
            }
        }

        @Override
        public void onOverHeated(boolean state) {
            if (state) {
                toast(getString(R.string.msg_overheated));
            }
        }

        @Override
        public void onLowBattery(boolean state) {
            if (state) {
                toast(getString(R.string.msg_low_battery));
            }
        }
    };

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRestart = false;
        closeActiveConnection();
    }

    public synchronized void waitForConnection() {
        closeActiveConnection();
        if (nRetry<=3)  {
            establishBluetoothConnection(macadress);
            nRetry++;
        }
        // Show dialog to select a Bluetooth device.
//        startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_GET_DEVICE);

        // Start server to listen for network connection.
//        try {
//            mPrinterServer = new PrinterServer(new PrinterServerListener() {
//                @Override
//                public void onConnect(Socket socket) {
//                    if (DEBUG) Log.d(LOG_TAG, "Accept connection from " + socket.getRemoteSocketAddress().toString());
//
//                    // Close Bluetooth selection dialog
//                    finishActivity(REQUEST_GET_DEVICE);
//
//                    mPrinterSocket = socket;
//                    try {
//                        InputStream in = socket.getInputStream();
//                        OutputStream out = socket.getOutputStream();
//                        initPrinter(in, out);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        error(getString(R.string.msg_failed_to_init) + ". " + e.getMessage(), mRestart);
//                    }
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void establishBluetoothConnection(final String address) {
        // closePrinterServer();

        doJob(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = adapter.getRemoteDevice(address);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                InputStream in = null;
                OutputStream out = null;

                adapter.cancelDiscovery();

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Connect to " + device.getName());
                    mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                    mBluetoothSocket.connect();
                    in = mBluetoothSocket.getInputStream();
                    out = mBluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    error(getString(R.string.msg_failed_to_connect) + ". " + e.getMessage(), mRestart);
                    return;
                }

                try {
                    initPrinter(in, out);
                } catch (IOException e) {
                    e.printStackTrace();
                    error(getString(R.string.msg_failed_to_init) + ". " + e.getMessage(), mRestart);
                    return;
                }
            }
        }, R.string.msg_connecting);
    }

    private void doJob(final Runnable job, final int resId) {
        // Start the job from main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Progress dialog available due job execution
                final ProgressDialog dialog = new ProgressDialog(PrintActivity.this);
                dialog.setTitle(getString(R.string.title_please_wait));
                dialog.setMessage(getString(resId));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            job.run();
                        } finally {
                            dialog.dismiss();
                        }
                    }
                });
                t.start();
            }
        });
    }

    private synchronized void closeActiveConnection() {
        closePrinterConnection();
        closeBlutoothConnection();
        //closeNetworkConnection();
        //closePrinterServer();
    }

    private synchronized void closePrinterConnection() {
        if (mPrinter != null) {
            mPrinter.release();
        }

        if (mProtocolAdapter != null) {
            mProtocolAdapter.release();
        }
    }

    private synchronized void closeBlutoothConnection() {
        // Close Bluetooth connection
        BluetoothSocket s = mBluetoothSocket;
        mBluetoothSocket = null;
        if (s != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Blutooth socket");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closePrinterServer() {
        closeNetworkConnection();

        // Close network server
        PrinterServer ps = mPrinterServer;
        mPrinterServer = null;
        if (ps != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Network server");
            try {
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closeNetworkConnection() {
        // Close network connection
        Socket s = mPrinterSocket;
        mPrinterSocket = null;
        if (s != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Network socket");
            try {
                s.shutdownInput();
                s.shutdownOutput();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void error(final String text, boolean resetConnection) {
        if (resetConnection) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            });

            waitForConnection();
        }
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
        } else {
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
        }

        mPrinterInfo = mPrinter.getInformation();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                ((ImageView)findViewById(R.id.icon)).setImageResource(R.drawable.icon);
//                ((TextView)findViewById(R.id.name)).setText(mPrinterInfo.getName());
            }
        });
    }

    private void printPage() {
        doJob(new Runnable() {


            @Override
            public void run() {
//                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
//                    dialog(R.drawable.page,
//                            getString(R.string.title_warning),
//                            getString(R.string.msg_unsupport_page_mode));
//                    return;
//                }

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Print Page");
                    mPrinter.reset();
                    mPrinter.selectPageMode();

                    mPrinter.setPageRegion(0, 0, 160, 320, Printer.PAGE_LEFT);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH I{br}");
                    mPrinter.drawPageRectangle(0, 0, 160, 32, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from left to right" +
                            ", feed to bottom. Starting point in left top corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.setPageRegion(160, 0, 160, 320, Printer.PAGE_TOP);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH II{br}");
                    mPrinter.drawPageRectangle(160 - 32, 0, 32, 320, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from top to bottom" +
                            ", feed to left. Starting point in right top corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.setPageRegion(160, 320, 160, 320, Printer.PAGE_RIGHT);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH III{br}");
                    mPrinter.drawPageRectangle(0, 320 - 32, 160, 32, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from right to left" +
                            ", feed to top. Starting point in right bottom corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.setPageRegion(0, 320, 160, 320, Printer.PAGE_BOTTOM);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH IV{br}");
                    mPrinter.drawPageRectangle(0, 0, 32, 320, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from bottom to top" +
                            ", feed to right. Starting point in left bottom corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(110);
                    mPrinter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    error(getString(R.string.msg_failed_to_print_page) + ". " + e.getMessage(), mRestart);
                }
            }
        }, R.string.msg_printing_page);
    }
    private void printDocument(long idAntet, Cursor crs) {
        printDocument(idAntet,crs,15);
    }

    private void printDocument(long idAntet, Cursor crs, Integer nBloc) {
        printDocument(idAntet,crs,nBloc,0,false);
    }


    private void printDocument(long idAntet, Cursor crs, Integer nBloc,Integer nEx,boolean lAlternat) {
        int versiune = ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY;
        doJob(new Runnable() {
            @Override
            public void run() {

                if (mPrinterInfo == null || !mPrinterInfo.isPageModeSupported()) {
                    dialog(R.drawable.page,
                            getString(R.string.title_warning),
                            getString(R.string.msg_unsupport_page_mode));
                    return;
                }

                try {
                    String modelPrinter = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_ecran5_model_printer), "EPSON");
                    if (crs.getCount() > 0) {
                        crs.moveToFirst();
                        // se imparte listarea in functie de felul imprimantei : listare ascii sau cu functiile imprimantei
                        if ("SEIKO,EPSON".contains(modelPrinter)) {
                            // imprimare pe model ascii
                            //   createFisImagineAscii(nIdAntet, versiune, context, lPrint, sCondens, modelPrinter,crs);
                        }
                        if ("DPP-450".contains(modelPrinter)) {
                            Log.d("PRO&", "Ininte de listare");
                            createFisImagineDPP450(idAntet, versiune, context, crs, nBloc,nEx,lAlternat);
                        }
                        if ("DPP-350".contains(modelPrinter)) {
                            Log.d("PRO&", "Ininte de listare");
                             // createFisImagineDPP350(nIdAntet, versiune, context, crsn,Bloc,nEx,lAlternat);
                        }

                    }
                    crs.close();
                } catch (Exception e) {
                    e.printStackTrace();

                }
//                createFisImagine(idAntet, crs,
//                            ConstanteGlobale.Parametri_versiune.VERSIUNE_BETTY, context,
//                            true, ListaFormat.getSirCondensare(0),mPrinter);

            }
        }, R.string.msg_printing_page);
    }

    private void dialog(final int iconResId, final String title, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(PrintActivity.this);
                builder.setIcon(iconResId);
                builder.setTitle(title);
                builder.setMessage(msg);

                AlertDialog dlg = builder.create();
                dlg.show();
            }
        });
    }

    // proceduri pt listare
// determina sirul de conformitate ce se trece in josul facturii
    private String setConformitate() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String sCodFiscal = settings.getString(this.getString(R.string.key_ecran4_cf), "");
        String sConf;
        if (sCodFiscal.toLowerCase().contains("3120044")) {
            sConf = "Declaram pe proprie raspundere ca produsele de panificatie livrate conform facturii sunt in conformitate cu legislatia sanitar veterinara si pentru siguranta alimentului in vigoare";
        } else {
            sConf = "";
        }
        return sConf;
    }

    // determina bitmap pentru stampila pe baza de cod fiscal
    private void setStampila(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String sCodFiscal = settings.getString(context.getString(R.string.key_ecran4_cf), "");
        Bitmap bitmap;
        if (sCodFiscal.toLowerCase().contains("27670851")) {
            // florisgin
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_florisgin);
        } else if (sCodFiscal.toLowerCase().contains("33984123")) {
            // semrompack
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_semrompack);
        } else if (sCodFiscal.toLowerCase().contains("3120044")) {
            // betty
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_bettycom);
        } else if (sCodFiscal.toLowerCase().contains("13804023")) {
            // eurotehnic
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stampila_eurot);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stamp_goala);

        }
        if (bitmap!=null) {
            imagewidth = bitmap.getWidth();
            imageheight = bitmap.getHeight();
            imageargb = new int[imagewidth * imageheight];
            bitmap.getPixels(imageargb, 0, imagewidth, 0, 0, imagewidth, imageheight);
            bitmap.recycle();
        }
    }

    private void createFisImagineDPP450(Long nIdAntet, int versiune, Context context,
                                        Cursor crs, Integer nBlocDoc,Integer nEx,boolean lAlternat) {

        crs.moveToFirst();
        int nCopii = 0; // daca nEx este 0 atunci se stabileste default pt fiecare tip de doc
        int nIndex;
        int nBloc=nBlocDoc ;
        iInaltInPagina=0 ; //this.iInalt;
        //        int nPause=Math.round((4+crs.getCount()/4))*1000;
        Log.d("PRO& LISTF", "1");

        // se identifica tipul documentului
        Boolean lCuChit = (crs.getInt(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_ID_MODPL)) == 5);
        int nTipDoc = crs.getInt(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_ID_TIPDOC));
        switch (nTipDoc) {
            case Biz.TipDoc.ID_TIPDOC_COMANDA: // comanda
                nCopii=(nEx==0 ? 2 :nEx);
                iInaltInPagina= printComanda(crs,iInaltInPagina);
                break;
            case Biz.TipDoc.ID_TIPDOC_FACTURA: // factura sau chitanta
                nCopii=(nEx==0 ? 2 : nEx);
                nIndex=crs.getColumnIndex(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_LISTAT);
                if (crs.getInt(nIndex)> 0) nCopii = 1; // daca s-a listat deja se scoate un singur ex
                for (int i = 0; i <nCopii ; i++) {
                    {
                        if ( !lAlternat || true) {
                            crs.moveToFirst();
                            if (lCuChit) {
//                            iInaltInPagina=0 ;
                                printChitanta(crs, iInaltInPagina);

                                try {
                                    Thread.sleep(100);
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        crs.moveToFirst();
                        int nRec = crs.getCount();
                        // in caz ca nRec = sau mai mic cu o unitate decat nBloc nbloc se face mai mic cu 1 decat nrec
                        // nBloc=((nBloc>=nRec) && (nBloc-nRec<=1) ? nRec-1 : nBloc );
                        int nPas = 0;
//                        int nBloc = 15;
                        while (nPas * nBloc < nRec) {
                            Log.d("PRO LISTAFORMAT"," Pas:"+nPas+"  Bloc:"+nBloc);
                            if (nPas == 0) {
                                if (nRec <= nBloc) {
                                    printFactura(crs, nPas * nBloc, nBloc, true, true,iInaltInPagina);
                                    Log.d("PRO LISTAFORMAT","True, True ");
                                } else {
                                    printFactura(crs, nPas * nBloc, nBloc, true, false,iInaltInPagina);
                                    Log.d("PRO LISTAFORMAT","True, False ");
                                }
                            } else if ((nPas + 1) * nBloc >= nRec) {
                                // suntem la ultimul pas
                                printFactura(crs, nPas * nBloc, nBloc, false, true,iInaltInPagina);
                            } else {
                                printFactura(crs, nPas * nBloc, nBloc, false, false,iInaltInPagina);
                                Log.d("PRO LISTAFORMAT","False, False ");
                            }
                            try {
                                Thread.sleep(Math.round((4 + nBloc / 4) * 800)+0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            nPas = nPas + 1;
                        }

                    }
//
//                            com.printFactura(crs);
                    // asteapta terminarea listarii
                    if ( i+1<nCopii) // se face pauza doar daca mai urmeaza un exemplar
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    Log.d("PRO EXEMPLAR ",i+"");
                }
                break;
            case Biz.TipDoc.ID_TIPDOC_AVIZCLIENT: // aviz client
                nCopii=(nEx==0 ? 2 : nEx);
                nIndex=crs.getColumnIndex(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_LISTAT);
                if (crs.getInt(nIndex)> 0) nCopii = 1;
                for (int i = 0; i <nCopii ; i++) {
                    Log.d("PRO LISTAFORMAT AVIZCLI"," Exemplar:"+1);
                    {
                        int nRec = crs.getCount();
                        int nPas = 0;
  //                      int nBloc = 15;
                        while (nPas * nBloc < nRec) {
                            Log.d("PRO LISTAFORMAT AVIZCLI"," Pas:"+nPas+"  Bloc:"+nBloc);
                            if (nPas == 0) {
                                if (nRec <= nBloc) {
                                    printAviz_client(crs, nPas * nBloc,  nBloc, true, true,0);
                                    Log.d("PRO LISTAFORMAT","True, True ");
                                } else {
                                    printAviz_client(crs, nPas * nBloc,  nBloc, true, false,0);
                                    Log.d("PRO LISTAFORMAT","True, False ");
                                }
                            } else if ((nPas + 1) * nBloc >= nRec) {
                                // suntem la ultimul pas
                                printAviz_client(crs, nPas * nBloc,  nBloc, false, true,0);
                                Log.d("PRO LISTAFORMAT","False, True ");
                            } else {
                                printAviz_client(crs, nPas * nBloc,  nBloc, false, false,0);
                                Log.d("PRO LISTAFORMAT","False, False ");
                            }
                            try {
                                Thread.sleep(2000) ; //Math.round((4 + nBloc / 4) * 1000) + 1000-1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            nPas = nPas + 1;
                        }

                    }

//                            com.printFactura(crs);
                    // asteapta terminarea listarii

                }
                break;
            case Biz.TipDoc.ID_TIPDOC_AVIZDESC: // aviz descarcare
                nCopii=(nEx==0 ? 1 : nEx);
                iInaltInPagina= printAvizDescarcare(crs,iInaltInPagina);
                break;
            case Biz.TipDoc.ID_TIPDOC_AVIZINC: // aviz incarcare
                nCopii=(nEx==0 ? 1 : nEx);

            {
                int nRec = crs.getCount();
                int nPas = 0;
    //            int nBloc = 15;
                while (nPas * nBloc < nRec) {
                    if (nPas == 0) {
                        if (nRec <= nBloc) {
                            printAvizIncarcare(crs, nPas * nBloc,  nBloc, true, true,0);
                        } else {
                            printAvizIncarcare(crs, nPas * nBloc,  nBloc, true, false,0);
                        }
                    } else if ((nPas + 1) * nBloc >= nRec) {
                        // suntem la ultimul pas
                        printAvizIncarcare(crs, nPas * nBloc,  nBloc, false, true,0);
                    } else {
                        printAvizIncarcare(crs, nPas * nBloc,  nBloc, false, false,0);
                    }
//                        try {
//                            Thread.sleep(Math.round((4 + nBloc / 4) * 1000) + 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    nPas = nPas + 1;
                }
            }
            break;
            case Biz.TipDoc.ID_TIPDOC_TRANSAM: // aviz client
                break;

            default:
                break;
        }

    }

    public int printFactura(final Cursor crs, final int nRand, final int nLinii, final boolean lAntet, final boolean lSubsol, int iTopCurent) {
        boolean lRed = false;
        crs.moveToFirst();
        while (!crs.isAfterLast()) {
            Log.d("PRO&", "Proc red=" + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_PROC_RED)));
            if (crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_PROC_RED)) != 0) {
                lRed=true;
            }
            crs.moveToNext();
        }

        if (lRed) {
            return printFactura_cuRed(crs, nRand, nLinii, lAntet, lSubsol,iTopCurent);
        } else {
            return printFactura_faraRed(crs, nRand, nLinii, lAntet, lSubsol,iTopCurent);
        }

    }

    @SuppressLint("Range")
    public int printFactura_cuRed(final Cursor crs, final int nRand, final int nLinii, final boolean lAntet, final boolean lSubsol, int iTopCurent) {

                // Log.d("PRO FCURED","Verificare stare dupa set stare "+getStareLaPrint());
                try {
                    setStampila(context);
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    crs.moveToFirst();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    antePrint();
                    int iCurrLeft = iCurrLeftAbsolut;
                    int iCurrTop = iInalt;
                    int iTopPageRegion = iTopCurent;
                    int iHeightPageRegion = 90;
                    int iLinieDen = 5 * 8;
                    int iLinieUM = iLinieDen + 34 * 8;
                    int iLiniePRED = iLinieUM + 5 * 8;
                    int iLinieCant = iLiniePRED + 10 * 8;
                    int iLiniePU = iLinieCant + 12 * 8;
                    int iLinieFara = iLiniePU + 12 * 8;
                    int iLinieTva = iLinieFara + 14 * 8;
                    int k = 0;
                    // deplasamentele pentru liniile ce delimiteaza coloanele din factura
                    // mm*8
                    int iLiniiCaseta = 0;
                    // descriere antet factura
                    if (lAntet) {
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
                    iTopPageRegion = iTopPageRegion + iHeightPageRegion;
                    int nRows = 0;
                    crs.moveToFirst();
                    while (!crs.isAfterLast() && nRows < nRand) {
                        crs.moveToNext();
                        nRows = nRows + 1;
                    }
                    nRows = 0;
                    k = 0;
                    while (!crs.isAfterLast() && k < nLinii) {
                        nRows = nRows + 1;
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length() > 24) {
                            nRows = nRows + 1;
                        }
                        // se ia in considerare si codul suplimentar
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_SUPLIM1)).trim().isEmpty()) {
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_SUPLIM2)).trim().isEmpty()) {
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_SUPLIM3)).trim().isEmpty()) {
                            nRows = nRows + 1;
                        }
                        if (!crs.getString(crs.getColumnIndex(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_SUPLIM4)).trim().isEmpty()) {
                            nRows = nRows + 1;
                        }
                        k = k + 1;
                        crs.moveToNext();
                    }
                    // se tine seama ca la urma vin 2 randuri pentru val marfa si val reducere
                    if (lSubsol) {
                        nRows = nRows + 2;
                        if (nRows < 4) nRows = 4;
                    }
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
                    k = 0;
                    double nCotaTva = 0;
                    double nValFara = 0, nValRed = 0, nValTva = 0, nTvaRed = 0;
                    String sDen = "";
                    while (!crs.isAfterLast() && k < nRand) {
                        crs.moveToNext();
                        k = k + 1;
                    }
                    k = 0;
                    while (!crs.isAfterLast() && k < nLinii) {
                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        sDen = crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim();

                        mPrinter.setPageXY(iCurrLeft, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k + (nRand) + 1) + "", 3) + " {br}");
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
                        mPrinter.setPageXY(iLinieTva, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}" +
                                Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA)), 6, Biz.ConstCalcul.ZEC_VAL_TVA)
                                + "{br}");

                        if (sDen.length() > 29) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " + Siruri.padR(sDen.substring(29), 29) + "{br}");
                        }
                        // se ia in considerare si codul suplimentar
                        if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim().length() > 0) {
                            iCurrTop = iCurrTop + iRowHeight;
                            mPrinter.setPageXY(iLinieDen, iCurrTop);
                            mPrinter.printTaggedText("{reset}{left}" + "   " +
                                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Cod_Bare.TABLE_NAME + "_" + ColectieAgentHelper.Table_Cod_Bare.COL_COD)).trim() + "{br}");
                        }

                        iCurrTop = iCurrTop + iRowHeight;
                        crs.moveToNext();
                        k = k + 1;
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
                        Log.d("PRO", "Control 1");
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
                        if (argb!=null) {
                            mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                        }
                        iCurrTop = iCurrTop + iRowHeight;
                        mPrinter.setPageXY(210, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + "BI/CI:{/i}{/b} " +
                                settings.getString(context.getString(R.string.key_ecran1_biagent), "") +
                                "{br}");
                        iCurrLeft = 428;
                        Log.d("PRO", "Control 2");

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
                        Log.d("PRO", "Control 3");

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
                        Log.d("PRO", "Control 4");

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
                        Log.d("PRO", "Control 5");

                    }
                    postPrint(lSubsol);
                    return iTopPageRegion + iHeightPageRegion;

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&", "Eroare imprimare list factura cu red: ");
                    postPrint(lSubsol);
                    return 0;
                }

    }


    @SuppressLint("Range")
    public int printFactura_faraRed(final Cursor crs, final int nRand, final int nLinii, final boolean lAntet, final boolean lSubsol, int iTopCurent) {
        int nNextIndex = 0;
        try {

                    setStampila(context);
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    crs.moveToFirst();
                    Log.d("PRO&","Inainte de compunere reset listare fact fara red : "+mPrinter.getStatus());
                    antePrint();
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
                                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_FISc)).trim().replace(" ","")
                                + "{br}");
                        mPrinter.setPageXY(iWidthPageregion / 2, iCurrTop);
                        mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR("Nr.reg.Com:", 16) + "{/i}" +
                                crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC)).trim().replace(" ","")
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
                    crs.moveToFirst();
                    if (lSubsol) {
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
                        if (argb!=null) {
                            mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                        }
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
                    // Log.d("PRO&", "Inainte de printpage factura: " + mPrinter.getStatus());
                    postPrint(lSubsol);
                    return iTopPageRegion + iHeightPageRegion;

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
            postPrint(lSubsol);
            return 0;

        } finally {

        }

    }

    //comanda
    public int printComanda(Cursor crs,int iTopCurent) {
        int nNextIndex = 0;
        try {
            crs.moveToFirst();
            // 80-pixeli= 1 cm pe latime
            // 100-pixeli = 12 mm pe inaltime
            setStampila(context);
            final int width = imagewidth;
            final int height = imageheight;
            final int[] argb = imageargb;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

            antePrint();
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
            mPrinter.printTaggedText("{reset}{center}{b}{w}{u}COMANDA PRODUSE {br}");
            iCurrTop = iCurrTop + iRowHeight + 3;
            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}Data: {/i}" +
                    Siruri.dtoc(Siruri.cTod(crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_DATA))))
                    + "{br}");
            // caseta antet articole
            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
            iHeightPageRegion = iInalt + 2 * iRowHeight;
            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                    iWidthPageregion, iHeightPageRegion,
                    Printer.PAGE_LEFT);
            iCurrLeft = iCurrLeftAbsolut;
            iCurrTop = iInalt;
            int iLinieDen = 5 * 8;
            int iLinieCant = iLinieDen + 64 * 8;
            int iLiniePU = iLinieCant + 12 * 8;

            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}Nr {br}");
            mPrinter.setPageXY(iLinieDen, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}" + Siruri.padR(" Denumire articol", 32) + "{br}");
            mPrinter.setPageXY(iLinieCant, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}" + "  Cant" + "{br}");
            mPrinter.setPageXY(iLiniePU, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Pret" + "{br}");

            iCurrTop = iCurrTop + iRowHeight;
            mPrinter.setPageXY(iCurrLeft, iCurrTop);

            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}cr {br}");

            mPrinter.drawPageRectangle(iLinieDen - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageFrame(0, 0,
                    iWidthPageregion - 1, iHeightPageRegion - 1
                    , Printer.FILL_BLACK, 3);
            // caseta articole
            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
            int nRows = 0;
            crs.moveToFirst();
            while (!crs.isAfterLast()) {
                nRows = nRows + 1;
                if (crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Produse.TABLE_NAME + "_" + ColectieAgentHelper.Table_Produse.COL_DENUMIRE)).trim().length() > 40) {
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
            mPrinter.drawPageRectangle(iLinieCant - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
            mPrinter.drawPageRectangle(iLiniePU - 2, 0, 1, iHeightPageRegion, Printer.FILL_BLACK);
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
                mPrinter.printTaggedText("{reset}{left}" + Siruri.padR(sDen, 40) + "{br}");
                mPrinter.setPageXY(iLinieCant, iCurrTop);
                mPrinter.printTaggedText("{reset}{left}" +
                        Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_CANTITATE)), 7, 2)
                        + "{br}");
                mPrinter.setPageXY(iLiniePU, iCurrTop);
                mPrinter.printTaggedText("{reset}{left}" +
                        Siruri.str(crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_PRET_CU)), 8, Biz.ConstCalcul.ZEC_PRET_CU)
                        + "{br}");
                if (sDen.length() > 40) {
                    iCurrTop = iCurrTop + iRowHeight;
                    mPrinter.setPageXY(iLinieDen, iCurrTop);
                    mPrinter.printTaggedText("{reset}{left}" + "   " + Siruri.padR(sDen.substring(40), 40) + "{br}");
                }
                nValFara = nValFara + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_FARA));
                nValTva = nValTva + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_TVA));
                nValRed = nValRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_VAL_RED));
                nTvaRed = nTvaRed + crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Pozitii.TABLE_NAME + "_" + ColectieAgentHelper.Table_Pozitii.COL_TVA_RED));
                iCurrTop = iCurrTop + iRowHeight;
                crs.moveToNext();
                k = k + 1;

            }
            // descriere subsol ( 2 randuri)
            crs.moveToFirst();
            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
            iHeightPageRegion = iInalt + 2 * iRowHeight;
            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                    iWidthPageregion, iHeightPageRegion,
                    Printer.PAGE_LEFT);
            iCurrLeft = iCurrLeftAbsolut;
            iCurrTop = iInalt;
            mPrinter.drawPageFrame(0, 0,
                    iWidthPageregion - 1, iHeightPageRegion - 1
                    , Printer.FILL_BLACK, 3);

            // totaluri fara si tva
            iCurrLeft = iCurrLeftAbsolut;
            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Total " + "{br}");
            mPrinter.setPageXY(iLiniePU, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{w}" +
                    Siruri.str(
                            crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_FARA)) +
                                    crs.getDouble(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_VAL_TVA))
                            , 7, Biz.ConstCalcul.ZEC_VAL_CU)
                    + "{br}");
            postPrint(true);
            return iTopPageRegion + iHeightPageRegion;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
            postPrint(true);
            return 0;
        }

    }


    // aviz incarcare
    public int printAvizIncarcare(final Cursor crs, final int nRand, final int nLinii, final boolean lAntet, final boolean lSubsol, int iTopCurent) {
        int nNextIndex = 0;

        try {
            // 80-pixeli= 1 cm pe latime
            // 100-pixeli = 12 mm pe inaltime
            setStampila(context);
            final int width = imagewidth;
            final int height = imageheight;
            final int[] argb = imageargb;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            int nRowTot = 0;
            int iCurrLeft = iCurrLeftAbsolut;
            int iCurrTop = this.iInalt;
            int iTopPageRegion = iTopCurent;
            int iHeightPageRegion = 0;
            int iLiniicaseta = 0;
            antePrint();
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
                mPrinter.printTaggedText("{reset}{left}" + Siruri.padL((k + (nRand) + 1) + "", 3) + " {br}");
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
                if (argb!=null) {
                    mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                }
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
            postPrint(lSubsol);
            return iTopPageRegion + iHeightPageRegion;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
            postPrint(lSubsol);
            return 0;
        }
    }

    // aviz descarcare
    public int printAvizDescarcare(Cursor crs,int iTopCurent) {
        int nNextIndex = 0;
        try {
            // 80-pixeli= 1 cm pe latime
            // 100-pixeli = 12 mm pe inaltime
            crs.moveToFirst();
            setStampila(context);
            final int width = imagewidth;
            final int height = imageheight;
            final int[] argb = imageargb;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            antePrint();
            int iCurrLeft = iCurrLeftAbsolut;
            int iCurrTop = iInalt;
            int iTopPageRegion = iTopCurent;
            int iHeightPageRegion = 90;
            int iLiniicaseta = 0;
            // descriere antet factura
            iHeightPageRegion = iInalt + 3 * iRowHeight;
            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                    iWidthPageregion, iHeightPageRegion,
                    Printer.PAGE_LEFT);
            mPrinter.drawPageFrame(0, 0,
                    iWidthPageregion - 1, iHeightPageRegion - 1
                    , Printer.FILL_BLACK, 3);
            mPrinter.setPageXY(0, iCurrTop);
            mPrinter.printTaggedText("{reset}{center}{b}{w}{u}AVIZ DE INSOTIRE AL MARFII * {br}");
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
            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
            iHeightPageRegion = iInalt + 2 * iRowHeight;
            mPrinter.setPageRegion(iLeftPageRegion, iTopPageRegion,
                    iWidthPageregion, iHeightPageRegion,
                    Printer.PAGE_LEFT);
            iCurrLeft = iCurrLeftAbsolut;
            iCurrTop = iInalt;
            int iLinieDen = 5 * 8;
            int iLinieUM = iLinieDen + 49 * 8;
            int iLinieCant = iLinieUM + 6 * 8;
            int iLiniePU = iLinieCant + 12 * 8;
            int iLinieFara = iLiniePU + 14 * 8;


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
            // caseta articole
            iTopPageRegion = iTopPageRegion + iHeightPageRegion;
            int nRows = 0;
            crs.moveToFirst();
            while (!crs.isAfterLast()) {
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
            if (argb!=null) {
                mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
            }
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
            postPrint();
            return iTopPageRegion + iHeightPageRegion;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
            postPrint();
            return 0;
        } finally {
        }
    }
    public int printAviz_client ( final Cursor crs,final int nRand,final int nLinii,final boolean lAntet,final boolean lSubsol, int iTopCurent) {
//aici voi trece pt decl de conformitate pt betty
                int nNextIndex=0;
                try {
                    crs.moveToFirst();
                    // 80-pixeli= 1 cm pe latime
                    // 100-pixeli = 12 mm pe inaltime
                    setStampila(context);
                    final int width = imagewidth;
                    final int height = imageheight;
                    final int[] argb = imageargb;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    antePrint();
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
                    double nPretFara=0 ;
                    boolean lFaraPret = false ;
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
                        if (argb!=null) {
                            mPrinter.printImage(argb, width, height, Printer.ALIGN_LEFT, true);
                        }
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
                    postPrint(lSubsol);
                    return iTopPageRegion + iHeightPageRegion;
//                    iCurrTop=2;
//                    mPrinter.setPageXY(iCurrLeft, iCurrTop);
//                    mPrinter.printTaggedText("{reset}{left}{i}{s}"+"Software pentru agenti de vanzari realizat de PROSOFT SRL . Tel. 0722 236256"+"{br}");
                    // se verifica starea impr relativ la celelalte sarcini
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PRO&", "Eroare imprimare: " + e.getMessage());
                    postPrint(lSubsol);
                    return 0;
                }

    }
    // chitanta
    public int printChitanta (  Cursor crs , int iTopCurent) {
        try {
            setStampila(context);
            antePrint();
            Log.d("PRO&", "dupa set stampila chitanta");
            final int width = imagewidth;
            final int height = imageheight;
            final int[] argb = imageargb;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            Log.d("PRO&", "Inainte de compunere listare chitanta: ");
            // 80-pixeli= 1 cm pe latime
            // 100-pixeli = 12 mm pe inaltime
            int iCurrLeft = iCurrLeftAbsolut;
            int iCurrTop = this.iInalt ;
            int iTopPageRegion = iTopCurent ;
            int iHeightPageRegion = 90;
            int iLiniiCaseta = 0;
            // descriere antet chitanta
            crs.moveToFirst();
            Log.d("PRO&", "chitanta 1 ");
            iHeightPageRegion = this.iInalt + 3 * iRowHeight;
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
                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_CHITANTA)) + "{br}");
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
            iHeightPageRegion = this.iInalt + iLiniiCaseta * iRowHeight;
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
            iHeightPageRegion = this.iInalt + 10 * iRowHeight;
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
                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_FISc)).trim().replace(" ","") +
                    "{b}{i} Nr. ORC/an: " +
                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_NR_RC)).trim().replace(" ","")
                    + "{br}");
            iCurrTop = iCurrTop + iRowHeight;
            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Adresa: " + "{/i}" +
                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Clienti.TABLE_NAME + "_" + ColectieAgentHelper.Table_Clienti.COL_JUDET))
                    + "{br}");

            //pozitionare stampila
            // se mai lasa un rand suplimentar pt a compensa un rand in plus de la textul in litere
            // mPrinter.setPageXY(iWidthPageregion - width - 20, iCurrTop);
            mPrinter.setPageXY(iWidthPageregion -  30, iCurrTop+2*iRowHeight);
            if (argb!=null) {
                mPrinter.printImage(argb, width, height, Printer.ALIGN_RIGHT, true);
            }



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
            iCurrTop = iCurrTop +3* iRowHeight;
            mPrinter.setPageXY(iCurrLeft, iCurrTop);
            mPrinter.printTaggedText("{reset}{left}{b}{i}" + "Reprezentand : " + "{/i}{/b}" +
                    " CV. Fact. " +
                    crs.getString(crs.getColumnIndexOrThrow(ColectieAgentHelper.Table_Antet.TABLE_NAME + "_" + ColectieAgentHelper.Table_Antet.COL_NR_DOC)) + "{br}");
            mPrinter.drawPageFrame(0, 0,
                    iWidthPageregion - 1, iHeightPageRegion - 1
                    , Printer.FILL_BLACK, 3);
            Log.d("PRO&", "chitanta 8 " );
            postPrint();

            return iTopPageRegion + iHeightPageRegion;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("PRO&", "Eroare imprimare chitanta: " + e.getMessage());
            postPrint();

            return 0;
        }

    }
    public void antePrint () {
        try {
            Thread.sleep(500);
            mPrinter.reset();
            mPrinter.selectPageMode();
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }
    public void postPrint() {
        postPrint(true);
    }

    public void postPrint(Boolean lSubsol) {
       try {
            mPrinter.printPage();
           mPrinter.flush();
           Thread.sleep(500);
           mPrinter.selectStandardMode();
            if (lSubsol) {
                mPrinter.feedPaper(150);
            }
            mPrinter.flush();
            Thread.sleep(500);

           // mPrinter.reset();
        } catch (Exception e ) {
            e.printStackTrace();
           Log.d("PRO&", "Eroare postprint: " + e.getMessage());
        }

    }
}