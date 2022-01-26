package cs455.overlay.node;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;

public class Registry {
    TCPServerThread server = null;
    TCPSender sender = null;
    Registry(int port) throws IOException {
        server = new TCPServerThread(port);
        Thread sthread = new Thread(server);
        sthread.start();

        while (true) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line = input.readLine();
            // byte[] bytes = getBytes(line);
            // sender.sendData(bytes);
        }
    }



    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        try {
            Registry node = new Registry(port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
