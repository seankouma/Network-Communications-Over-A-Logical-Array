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

    @Override
    public void run() {
        System.out.println("Server started");

        System.out.println("Waiting for client");

        try {
            while (true) {
                socket = server.accept();
                System.out.println("Client accepted");
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