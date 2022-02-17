package cs455.overlay.node;

import java.util.HashSet;
import java.util.Random;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.DataTraffic;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.Deregister;


public class Registry implements Node {

    public static HashMap<Integer, Socket> nodes = new HashMap<Integer, Socket>();
    TCPServerThread server = null;
    TCPSender sender = null;
    int completed = 0;


    Registry(int port) throws IOException {
        server = new TCPServerThread(port, this);
        Thread sthread = new Thread(server);
        sthread.start();

        while (true) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line = input.readLine();
            if (line.equals("list-messaging-nodes")) {
                listNodes();
            } else if (line.equals("setup-overlay")) {
                setupOverlay();
            } else {
                String pattern = "^start \\d*$";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(line);
                if (m.find()) { // User input was "start #"
                    int toSend = Integer.parseInt(line.replaceAll("[\\D]", ""));
                    this.taskInitiate(toSend);
                }
            }
        }
    }

    public static void setupOverlay() throws IOException {
        ArrayList<Integer> keys = new ArrayList<Integer>(new TreeSet<Integer>(nodes.keySet()));
        for (int i = 0; i < keys.size() - 1; i++) {
            Socket next = nodes.get(keys.get(i+1));
            ConnectionsDirective connect = new ConnectionsDirective(next.getInetAddress().getHostAddress(), next.getPort());
            byte[] data = connect.getBytes();
            TCPSender sender = new TCPSender(nodes.get(keys.get(i)));
            sender.sendData(data);
        }
        Socket next = nodes.get(keys.get(0));
        ConnectionsDirective connect = new ConnectionsDirective(next.getInetAddress().getHostAddress(), next.getPort());
        byte[] data = connect.getBytes();
        TCPSender sender = new TCPSender(nodes.get(keys.get(keys.size() - 1)));
        sender.sendData(data);
    }

    public static int register(Register register) throws UnknownHostException, IOException {
        Random r = new Random();
        int max = 1024;
        Integer rand = r.nextInt(max);
        while (nodes.keySet().contains(rand)) rand = r.nextInt(max);
        Socket socket = new Socket(register.getIp(), register.getPort());
        nodes.put(rand, socket);
        for (Integer i : nodes.keySet()) {
            Socket current = nodes.get(i);
            System.out.println("Num: " + Integer.toString(i) + ", IP: " + current.getInetAddress().getHostAddress() + ", Port: " + Integer.toString(current.getPort()));
        }
        return rand;
    }

    public void taskInitiate(int num) throws IOException {
        TaskInitiate init = new TaskInitiate(num);
        byte[] data = init.getBytes();
        for (Socket s : nodes.values()) {
            this.sender = new TCPSender(s);
            this.sender.sendData(data);
        }
    }

    public void handleTaskComplete(int id) {
        ++completed;
        if (completed == nodes.size()) {
            System.out.println("All nodes completed");
        }
    }

    public static boolean deregister(Deregister register, int id) throws UnknownHostException, IOException {
        if(nodes.keySet().contains(id)){
            nodes.keySet().remove(id);
            return true;
        }
        System.out.println("In registry: Failed to derigster node due to invalid id");
        return false;
    }
    //derister check if id is valid
    //tells node it can stop
    //othwrwise message node to try again

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
        for (Socket socket : nodes.values()) {
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

    @Override
    public void handleTaskInitiate(int num) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleDataTraffic(DataTraffic traffic) {

    }
}
