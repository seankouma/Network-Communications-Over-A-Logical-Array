package cs455.overlay.node;

import java.util.HashSet;
import java.util.Random;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
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
import cs455.overlay.wireformats.*;


public class Registry implements Node {

    public static HashMap<Integer, Socket> nodes = new HashMap<Integer, Socket>();
    TCPServerThread server = null;
    TCPSender sender = null;
    int completed = 0;

    BigInteger totalSent = new BigInteger("0");
    BigInteger totalReceived = new BigInteger("0");
    BigInteger sumSent = new BigInteger("0");
    BigInteger sumReceived = new BigInteger("0");



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

    public static int register(Register register) throws UnknownHostException, IOException {
        Random r = new Random();
        int max = 1024;
        Integer rand = r.nextInt(max);
        while (nodes.keySet().contains(rand)) rand = r.nextInt(max);
        Socket socket = new Socket(register.getIp(), register.getPort());
        nodes.put(rand, socket);
        System.out.println("Registration request successful. The number of messaging nodes currently constituting the overlay is (" + Integer.toString(nodes.size()) + ")");
        return rand;
    }

    public boolean deregister(Deregister register, int id) throws UnknownHostException, IOException {
        for (Socket s : nodes.values()) {
            System.out.println(s.getInetAddress().getHostName() + " " + register.ip + " " + Integer.toString(s.getPort()) + " " + Integer.toString(register.port));
            if (s.getPort() == register.port) {
                sender = new TCPSender(s);
                DeregisterResponse response = new DeregisterResponse("Successfully deregistered", register.port);
                sender.sendData(response.getBytes());
                nodes.values().remove(s);
                return true;
            }
        }
        System.out.println("In registry: Failed to deregister node due to invalid ip/port");
        return false;
    }

    public static void setupOverlay() throws IOException {
        ArrayList<Integer> keys = new ArrayList<Integer>(new TreeSet<Integer>(nodes.keySet()));
        for (int i = 0; i < keys.size() - 1; i++) {
            Socket next = nodes.get(keys.get(i+1));
            ConnectionsDirective connect = new ConnectionsDirective(next.getInetAddress().getHostAddress(), next.getPort(), keys.get(i+1));
            byte[] data = connect.getBytes();
            TCPSender sender = new TCPSender(nodes.get(keys.get(i)));
            sender.sendData(data);
        }
        Socket next = nodes.get(keys.get(0));
        ConnectionsDirective connect = new ConnectionsDirective(next.getInetAddress().getHostAddress(), next.getPort(), keys.get(0));
        byte[] data = connect.getBytes();
        TCPSender sender = new TCPSender(nodes.get(keys.get(keys.size() - 1)));
        sender.sendData(data);
    }

    public void taskInitiate(int num) throws IOException {
        TaskInitiate init = new TaskInitiate(num);
        byte[] data = init.getBytes();
        for (Socket s : nodes.values()) {
            this.sender = new TCPSender(s);
            this.sender.sendData(data);
        }
    }

    public synchronized void handleTaskComplete(int id) {
        ++completed;
        System.out.println("Node completed");
        if (completed == nodes.size()) {
            System.out.println("All nodes completed");
            try {   
                gatherTrafficSummaries();
                completed = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void gatherTrafficSummaries() throws IOException {
        try {
            this.wait(15000); // Waits 15 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PullTrafficSummary trafficSummary = new PullTrafficSummary();
        byte[] data = trafficSummary.getBytes();
        System.out.println("getting summaries");
        for( Socket s: nodes.values()) {
            this.sender = new TCPSender(s);
            this.sender.sendData(data);
        }
        System.out.println("      | Num Sent Messages | Num Messages Recieved | Sum of Sent | Sum of Recieved |");
    }

    private void listNodes() {
        for (Socket socket : nodes.values()) {
            System.out.println("Hostname: " + socket.getInetAddress().getHostName() + ", Port: " + Integer.toString(socket.getPort()));
        }
    }

    @Override
    public synchronized void handleTrafficSummary(TrafficSummary summary) {
        ++completed;
        this.sumSent = sumSent.add(new BigInteger(Integer.toString(summary.sumOfSent)));
        this.sumReceived = sumReceived.add(new BigInteger(Integer.toString(summary.sumOfReceived)));
        this.totalReceived = totalReceived.add(new BigInteger(Integer.toString(summary.numOfMReceived)));
        this.totalSent = totalSent.add(new BigInteger(Integer.toString(summary.numOfMSent)));

        System.out.format("Node  |%19d|%23d|%16d|%17d|\n", summary.numOfMSent, summary.numOfMReceived, summary.sumOfSent, summary.sumOfReceived);
        if (completed == nodes.size()) {
            System.out.format(" Sum  |%19d|%23d|%16d|%17d|\n", this.totalSent, this.totalReceived, this.sumSent, this.sumReceived);
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        try {
            Registry node = new Registry(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setIdentifier(int id) {
        // Auto-generated method stub
        
    }

    @Override
    public int getIdentifier() {
        // Auto-generated method stub
        return 0;
    }

    @Override
    public void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException {
        // Auto-generated method stub
        
    }

    @Override
    public void handleTaskInitiate(int num) {
        // Auto-generated method stub
        
    }

    @Override
    public void handleDataTraffic(byte[] traffic) {}

    @Override
    public void handlePullTrafficSummary() {
        // Auto-generated method stub
    }

    @Override
    public void handleDeregister(String status) {
        // TODO Auto-generated method stub
        
    }
}
