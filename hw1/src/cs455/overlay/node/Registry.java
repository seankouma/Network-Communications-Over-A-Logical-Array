package cs455.overlay.node;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.Register;

public class Registry implements Node {

    public static HashMap<Integer, Socket> map = new HashMap<Integer, Socket>();

    TCPServerThread server = null;
    TCPSender sender = null;
    Registry(int port) throws IOException {
        server = new TCPServerThread(port, this);
        Thread sthread = new Thread(server);
        sthread.start();

        while (map.size() < 5) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line = input.readLine();
            if (line.equals("list-messaging-nodes")) {
                listNodes();
            } else if (line.equals("setup-overlay")) {
                start();
            }
        }
    }

    public static void start() throws IOException {
        ArrayList<Integer> keys = new ArrayList<Integer>(new TreeSet<Integer>(map.keySet()));
        for (int i = 0; i < keys.size() - 1; i++) {
            Socket next = map.get(keys.get(i+1));
            ConnectionsDirective connect = new ConnectionsDirective(next.getInetAddress().getHostAddress(), next.getPort());
            byte[] data = connect.getBytes();
            TCPSender sender = new TCPSender(map.get(keys.get(i)));
            sender.sendData(data);
        }
        Socket next = map.get(keys.get(0));
        ConnectionsDirective connect = new ConnectionsDirective(next.getInetAddress().getHostAddress(), next.getPort());
        byte[] data = connect.getBytes();
        TCPSender sender = new TCPSender(map.get(keys.get(keys.size() - 1)));
        sender.sendData(data);
    }

    public static int register(Register register) throws UnknownHostException, IOException {
        Random r = new Random();
        Integer rand = r.nextInt();
        Socket socket = new Socket(register.getIp(), register.getPort());
        map.put(rand, socket);
        for (Integer i : map.keySet()) {
            Socket current = map.get(i);
            System.out.println("Num: " + Integer.toString(i) + ", IP: " + current.getInetAddress().getHostAddress() + ", Port: " + Integer.toString(current.getPort()));
            System.out.println();
        }
        if (map.keySet().size() >= 4) start(); 
        return rand;
    }

    //derister check if id is valid

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        try {
            Registry node = new Registry(port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void listNodes() {
        for (Socket socket : map.values()) {
            System.out.println("Hostname: " + socket.getInetAddress().getHostName() + ", Port: " + Integer.toString(socket.getPort()));
        }
    }

    @Override
    public void setIdentifier(int id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getIdentifier() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        
    }
}
