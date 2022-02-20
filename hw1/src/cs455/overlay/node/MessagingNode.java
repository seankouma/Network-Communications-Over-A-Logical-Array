package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

public class MessagingNode implements Node {
    TCPServerThread server = null;
    public TCPSender sender = null;
    public int identifier = 0;
    Socket peerSocket = null;
    TCPSender peerSender = null;
    public int numOfMSent = 0;
    public int numOfMReceived = 0;
    public int sumOfSent = 0;
    public int sumOfReceived = 0;

    MessagingNode(String hostname, int otherPort) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(0);
        server = new TCPServerThread(serverSocket, this);
        Thread sthread = new Thread(server);
        sthread.start();
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket(hostname, otherPort);
            } catch (IOException e) {
                socket = null;
            }
        }
        sender = new TCPSender(socket);
        Register register = new Register(hostname, serverSocket.getLocalPort());
        byte[] bytes = register.getBytes();
        sender.sendData(bytes);

        while (true) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line = input.readLine();
            if (line.equals("exit-overlay")) {
                Deregister deregister = new Deregister(InetAddress.getLocalHost().getHostName(), serverSocket.getLocalPort());
                bytes = deregister.getBytes();
                sender.sendData(bytes);
            }
        }
    }

    // To run this, just pass in the port you want this node to use and the port it should communicate with as CLI parameters.
    public static void main(String[] args) throws IOException, InterruptedException {
        int otherPort = Integer.parseInt(args[1]);
        MessagingNode node = new MessagingNode(args[0], otherPort);
    }

    @Override
    public void setIdentifier(int id) {
        this.identifier = id;
    }

    @Override
    public int getIdentifier() {
        return this.identifier;
    }

    @Override
    public void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException {
        this.peerSocket = new Socket(connect.getIp(), connect.getPort());
        System.out.println("Connected to: " + Integer.toString(connect.getID()));
        this.peerSender = new TCPSender(peerSocket);
        
    }

    @Override
    public void handleTaskInitiate(int num) {
        System.out.println("Messages to send from node: " + Integer.toString(num));
        Random rand = new Random();
        for (int i = 0; i < num; i++) {
            DataTraffic traffic = new DataTraffic(rand.nextInt(), this.identifier);
            try {
                this.numOfMSent += 1;
                this.sumOfSent += traffic.random;
                this.peerSender.sendData(traffic.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("NODE FINISHED");
        this.handleTaskComplete(this.identifier);
    }

    @Override
    public synchronized void handleTaskComplete(int id){
        TaskComplete tc = new TaskComplete(id);
        try {
            this.sender.sendData(tc.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleDataTraffic(byte[] data) {
        try {
            DataTraffic traffic = new DataTraffic(data);
            ++numOfMReceived;
            sumOfReceived += traffic.random;
            if (this.numOfMReceived % 10000 == 0) {
                System.out.println("Total Received: " + this.numOfMReceived + " | Sum of Received: " + this.sumOfReceived);
            }
            if (traffic.id != this.identifier) peerSender.sendData(traffic.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void handlePullTrafficSummary() {
        TrafficSummary summary = new TrafficSummary(this.numOfMSent, this.sumOfSent, this.numOfMReceived, this.sumOfReceived);
        try {
            this.sender.sendData(summary.getBytes());
            this.numOfMSent = 0;
            this.sumOfSent = 0;
            this.numOfMReceived = 0;
            this.sumOfReceived = 0;

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public void handleTrafficSummary(TrafficSummary summary) {

    }

    @Override
    public void handleDeregister(String status) {
        System.out.println(status);
        System.exit(0);
    }

    @Override
    public boolean deregister(Deregister dr, int st) throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        return false;
    }
}
