package cs455.overlay.transport;

import java.net.*;

import cs455.overlay.node.Node;
import java.io.*;

public class TCPReceiverThread implements Runnable {
    private Socket socket = null;
    private DataInputStream input = null;
    Node caller = null;

    public TCPReceiverThread(Socket socket, Node caller) throws IOException {
        this.socket = socket;
        this.caller = caller;
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public void run() {
        int dataLength;
        while (socket != null) {
            try {
                dataLength = input.readInt();
                int id = input.readInt();
                byte[] data = new byte[dataLength-4];
                input.readFully(data, 0, dataLength-4);
                caller.handleEvent(id, dataLength-4, data);
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