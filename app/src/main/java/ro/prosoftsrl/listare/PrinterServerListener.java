package ro.prosoftsrl.listare;

import java.net.Socket;

public interface PrinterServerListener {
    public void onConnect(Socket socket);
}
