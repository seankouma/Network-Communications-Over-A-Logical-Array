package cs455.overlay.transport;

import java.io.*;
import java.net.*;

import cs455.overlay.node.Node;

public class TCPServerThread implements Runnable {
    private ServerSocket server = null;
    public Socket socket = null;
    Node caller = null;

    public TCPServerThread(int port, Node caller) throws IOException {
        server = new ServerSocket(port);
        this.caller = caller;
    }

    public TCPServerThread(ServerSocket socket, Node caller) throws IOException {
        server = socket;
        this.caller = caller;
    }

    @Override
    public void run() {
        System.out.println("Server started");

        try {
            while (true) {
                socket = server.accept();
                // Spawn another thread to accept incoming packets to the port
                TCPReceiverThread receiver = new TCPReceiverThread(socket, this.caller);
                Thread rthread = new Thread(receiver);
                rthread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  
    }
}
