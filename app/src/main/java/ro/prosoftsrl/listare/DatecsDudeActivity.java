package ro.prosoftsrl.listare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import ro.prosoftsrl.agenti.R;
import com.datecs.fiscalprinter.SDK.BuildInfo;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayer.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayer.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayer.cmdReport;

import java.io.IOException;

public class DatecsDudeActivity extends FragmentActivity {
    public static DatecsFiscalDevice myFiscalDevice;
    public static final int DATECS_USB_VID = 65520;
    public static final int FTDI_USB_VID = 1027;
    private int CONNECT_DEVICE = 999;
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private TextView mTitle;
    private TextView mLibraryVersion;
    private cmdReceipt myReceipt=new cmdReceipt();

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
 //                   if (device.getVendorId() == DATECS_USB_VID) connect();
                }
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datecs_dude);
    }

//    protected void connect() {
//        final ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setMessage("Conectare la casa de marcat");
//        dialog.setCancelable(false);
//        dialog.show();
//        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//        if (adapter != null && adapter.isDiscovering()) {
//            adapter.cancelDiscovery();
//        }
//        final AbstractConnector item
//        final Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    try {
//                        item.connect();
//                    } catch (Exception e) {
//                        fail("Connection error: " + e.getMessage());
//                        return;
//                    }
//
//                    try {
//                        PrinterManager.instance.init(item);
//                    } catch (Exception e) {
//                        try {
//                            item.close();
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                        fail("Error: " + e.getMessage());
//                        return;
//                    }
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent resultData = new Intent();
////                            resultData.putExtra(EXTRA_CONNECTED_DEVICE, PrinterManager.instance.getModelVendorName() + ":"
////                                    + PrinterManager.getsConnectorType());
//                            setResult(RESULT_OK, resultData);
//                            finish();
//
//                        }
//                    });
//                } finally {
//                    dialog.dismiss();
//                }
//            }
//        });
//        thread.start();
//
//
//    }
}