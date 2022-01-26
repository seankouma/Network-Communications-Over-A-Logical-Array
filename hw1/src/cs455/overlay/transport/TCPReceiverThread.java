package cs455.overlay.transport;

import java.net.*;
import java.io.*;

public class TCPReceiverThread implements Runnable {
    private Socket socket = null;
    private DataInputStream input = null;

    public TCPReceiverThread(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public void run() {
        int dataLength;
        while (socket != null) {
            try {
                dataLength = input.readInt();
                byte[] data = new byte[dataLength];
                input.readFully(data, 0, dataLength);
                String string = new String(data);
                System.out.println("Client says " + string);
            } catch (SocketException se) {
                System.out.println(se.getMessage());
                break;
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                break;
            }
        }
    }
}