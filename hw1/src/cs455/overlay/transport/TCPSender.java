package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender {
    private DataOutputStream dout;

    public TCPSender(Socket socket) throws IOException {
        dout = new DataOutputStream(socket.getOutputStream());
    }

    public void sendData(byte[] dataToSend) throws IOException {
        /* This method doesn't need its own thread because it doesn't need to always be polling, unlike the TCPServerThread and TCPReceiverThread */
        int dataLength = dataToSend.length;
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, dataLength);
        dout.flush();
    }
}