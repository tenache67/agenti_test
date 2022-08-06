package ro.prosoftsrl.bluet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import com.datecs.fiscalprinter.FiscalPrinterException;

import ro.prosoftsrl.agenti.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class Bluet {
    Context context;
    public String sAdapter=""; // numele asociat al dispozitivului bluet
    public int stare=0 ; // starea in care se gaseste 0 - inchs 1 - deschis
	// android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket mmSocket;
    public BluetoothDevice mmDevice;
    
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    Thread workerThread;

    private interface MethodInvoker {
        public void invoke() throws IOException;
    }

    
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    public Bluet(Context context, String sAdapter) {
		// TODO Auto-generated constructor stub
    	this.context=context;
    	if (sAdapter.equals("")) {
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    		this.sAdapter=settings.getString(context.getString(R.string.key_ecran3_bluetadapter),"");
    	} else 
    		this.sAdapter=sAdapter;
    	Log.d("BLUET","Adaptor:"+this.sAdapter);
	}
    
    /*
     * This will find a bluetooth printer device
     */
    public String  findBT() {
    	String err = "";
    	if (!sAdapter.equals("")) { 
	        try {
	            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	            if (mBluetoothAdapter == null) {
	                err=context.getResources().getString(R.string.err_bluet_nuexistaadaptor);
	            } else {
	            	if (!mBluetoothAdapter.isEnabled()) {
	            		Log.d("BLUET","Inactiv");
	            		Intent enableBluetooth = new Intent(
	                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            		((Activity) context).startActivityForResult(enableBluetooth, 0);
	            	}
	            	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
	            	if (pairedDevices.size() > 0) {
	            		for (BluetoothDevice device : pairedDevices) {
	                    // MP300 is the name of the bluetooth printer device
	                    if (device.getName().equals(sAdapter)) {
	                        mmDevice = device;
	                        break;
	                    }
	                }
	            }
	         }
	            //myLabel.setText("Bluetooth Device Found");
	        } catch (NullPointerException e) {
	            e.printStackTrace();
	            err=context.getResources().getString(R.string.err_bluet_nuexistaadaptor)+e.getMessage();
	        } catch (Exception e) {
	            e.printStackTrace();
	            err=context.getResources().getString(R.string.err_bluet_nuexistaadaptor)+e.getMessage();
	        }
    	} else {
    		
    	}
        return err;
    }

    /*
     * Tries to open a connection to the bluetooth printer device
     */
    public String openBT() throws IOException {
    	String err ="";
    	this.stare=0;
        // Standard SerialPortService ID
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        if (mmDevice!=null) {
            final BluetoothSocket sock= mmDevice.createRfcommSocketToServiceRecord(uuid);
            Log.d("BLUET","inainte de connect");
            mBluetoothAdapter.cancelDiscovery();
            // se incearca 3 pasi de conectare
            boolean lTermin=false;
            int steps = 0 ;
            while (! lTermin) {
    	        try {
    	            sock.connect();            
    	            Log.d("BLUET","dupa connect");         
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
    	            Log.d("BLUET","dupa socket");
    	            mmSocket=sock;
    	            setMmOutputStream(mmSocket.getOutputStream());
    	            setMmInputStream(mmSocket.getInputStream());
    	            stare=1;
    	            lTermin=true;
    	        } else {
    	        	if (steps==5) lTermin=true; else err="";
    	        }
    	        Log.d("BLUET","Pas conect "+steps);
    	        steps=steps+1;
            }        	
        } else {
        	err="Eroare adaptor bluetooth";
        	stare=0;
        }
        
        return err;
    }
    
    /*
     * After opening a connection to bluetooth printer device, 
     * we have to listen and check if a data were sent to be printed.
     */
    void beginListenForData() {
        try {
            final Handler handler = new Handler();
            // This is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            
            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted()
                            && !stopWorker) {
                        
                        try {
                            
                            int bytesAvailable = getMmInputStream().available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                getMmInputStream().read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length);
                                        final String data = new String(
                                                encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;
                                        Log.d("BLUET","Citit:"+data);
                                        handler.post(new Runnable() {
                                            public void run() {
//                                                myLabel.setText(data);
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                            
                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                        
                    }
                }
            });

            workerThread.start();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * This will send data to be printed by the bluetooth printer
     */
    public String sendData( String msg) throws IOException {
    	String err="";
        try {            
            getMmOutputStream().write(msg.getBytes());
            Thread.sleep(200);
            //mmOutputStream.wait(200);
        } catch (NullPointerException e) {
            e.printStackTrace();
            err=context.getResources().getString(R.string.err_bluet_nusatransmis)+" "+sAdapter+" "+e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            err=context.getResources().getString(R.string.err_bluet_nusatransmis)+" "+sAdapter+" "+e.getMessage();
        }
        return err;
    }

    /*
     * Close the connection to bluetooth printer.
     */
    public void closeBT() throws IOException {
        try {
        	Log.d("PRO","Close BT 1");
            stopWorker = true;
            getMmOutputStream().close();
        	Log.d("PRO","Close BT 2");
            getMmInputStream().close();
        	Log.d("PRO","Close BT 3");
            mmSocket.close();
        	Log.d("PRO","Close BT 4");
            this.stare=0;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void invokeHelper(final MethodInvoker invoker) {
    	final ProgressDialog dialog = new ProgressDialog(context);
    	dialog.setCancelable(false);
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.setMessage(context.getString(R.string.bluet_asteapta));
    	dialog.setOnKeyListener(new OnKeyListener() {					
    		@Override
    		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
    			return true;
    		}
    	});
    	dialog.show();
    	
    	final Thread t = new Thread(new Runnable() {			
    		@Override
    		public void run() {				
    			try {
    				invoker.invoke();
    			} catch (FiscalPrinterException e) { // Fiscal printer error
    			    e.printStackTrace();
    			    Log.d("BLUET_CASA","FiscalPrinterException: " + e.getMessage());				    
    	    	} catch (IOException e) { //Communication error
    	    	    e.printStackTrace();
    	    	    Log.d("BLUET_CASA","IOException: " + e.getMessage());
//    	    		disconnect();   		
    	    	} catch (Exception e) { // Critical exception
                    e.printStackTrace();
                    Log.d("BLUET_CASA","Exception: " + e.getMessage());
//                    disconnect();           
                } finally {
    	    		dialog.dismiss();
    	    	}
    		}
    	});    	
    	t.start();
    }

    
	public InputStream getMmInputStream() {
		return mmInputStream;
	}

	public void setMmInputStream(InputStream mmInputStream) {
		this.mmInputStream = mmInputStream;
	}

	public OutputStream getMmOutputStream() {
		return mmOutputStream;
	}

	public void setMmOutputStream(OutputStream mmOutputStream) {
		this.mmOutputStream = mmOutputStream;
	}
    
}
