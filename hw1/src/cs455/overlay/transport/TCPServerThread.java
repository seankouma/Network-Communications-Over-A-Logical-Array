package cs455.overlay.transport;

import java.io.*;
import java.net.*;

public class TCPServerThread implements Runnable {
    private ServerSocket server = null;
    public Socket socket = null;

    public TCPServerThread(int port) throws IOException {
        server = new ServerSocket(port);
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
                TCPReceiverThread receiver = new TCPReceiverThread(socket);
                Thread rthread = new Thread(receiver);
                rthread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  
    }
}