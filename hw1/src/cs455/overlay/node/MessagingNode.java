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
import java.util.Queue;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Random;
import java.util.ArrayDeque;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;
import cs455.overlay.node.*;

public class MessagingNode implements Node {
    TCPServerThread server = null;
    public TCPSender sender = null;
    public volatile int identifier = 0;
    public Socket peerSocket = null;
    public TCPSender peerSender = null;
    public int numOfMSent = 0;
    public int numOfMReceived = 0;
    public int sumOfSent = 0;
    public int sumOfReceived = 0;
    private boolean complete = false;
    private MessagingNodeHelper helper = null;
    public ArrayDeque<DataTraffic> queue = new ArrayDeque<DataTraffic>(1250000);

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
        Register register = new Register(InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort());
        byte[] bytes = register.getBytes();
        sender.sendData(bytes);

        while (true) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line = input.readLine();
            if (line != null && line.equals("exit-overlay")) {
                Deregister deregister = new Deregister(InetAddress.getLocalHost().getHostName(), serverSocket.getLocalPort());
                bytes = deregister.getBytes();
                sender.sendData(bytes);
            }
        }
    }


    @Override
    public void handleEvent(int id, int dataLength, byte[] data) throws IOException {
        switch (id) {
            case Protocol.REGISTER_RESPONSE:
                RegisterResponse response = new RegisterResponse(data);
                setIdentifier(response.identifier);
                System.out.println("ID is: " + Integer.toString(response.identifier) + " Host: " + InetAddress.getLocalHost().getHostName());
                break;
            case Protocol.CONNECT:
                ConnectionsDirective connect = new ConnectionsDirective(data, dataLength);
                handleConnect(connect);
                break;
            case Protocol.TASK_INITIATE:
                TaskInitiate task = new TaskInitiate(data);
                this.helper = new MessagingNodeHelper(this.peerSender, this, this.identifier, task.sendMessages);
                Thread td = new Thread(helper);
                td.start();
                // MessageForwarder helper2 = new MessageForwarder(this.peerSender, this);
                // Thread td2 = new Thread(helper2);
                // td2.start();
                break;
            case Protocol.DATA_TRAFFIC:
                handleDataTraffic(data);
                break;
            case Protocol.PULL_TRAFFIC_SUMMARY:
                handlePullTrafficSummary();
                break;
            case Protocol.DEREGISTER_RESPONSE:
                DeregisterResponse resp = new DeregisterResponse(data, dataLength);
                handleDeregister(resp.getStatus());
                break;
            default:
                System.out.println("You missed something: " + Integer.toString(id));
                break;
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        int otherPort = Integer.parseInt(args[1]);
        MessagingNode node = new MessagingNode(args[0], otherPort);
    }

    public void setIdentifier(int id) {
        this.identifier = id;
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException {
        this.peerSocket = new Socket(connect.getIp(), connect.getPort());
        System.out.println("Connected to: " + Integer.toString(connect.getID()));
        this.peerSender = new TCPSender(peerSocket);
    }

    // public void handleTaskInitiate(int num) {
    //     System.out.println("Messages to send from node: " + Integer.toString(num));
    //     Random rand = new Random();
    //     for (int i = 0; i < num; i++) {
    //         DataTraffic traffic = new DataTraffic(rand.nextInt(), this.identifier);
    //         try {
    //             this.numOfMSent += 1;
    //             this.sumOfSent += traffic.random;
    //             this.sendDataTraffic(traffic);
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         }
    //     }
    //     System.out.println("NODE FINISHED");
    //     this.handleTaskComplete(this.identifier);
    //     this.complete = true;
    // }

    public void handleDataTraffic(byte[] data) {
        try {
            DataTraffic traffic = new DataTraffic(data);
            ++numOfMReceived;
            sumOfReceived += traffic.random;
            if (this.numOfMReceived % 10000 == 0) {
                System.out.println("Total Received: " + this.numOfMReceived + " | ID: " + traffic.id + " My ID: " + this.identifier);
            }
            // if (traffic.id != this.identifier) {
            //     if (queue.size() > 2000000) Thread.sleep(50);
            //     queue.push(traffic);
            // }
            if (traffic.id != this.identifier) peerSender.sendData(traffic.getBytes());
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void sendDataTraffic(DataTraffic traffic) throws IOException {
        byte[] data = traffic.getBytes();
        this.peerSender.sendData(data);
    }

    public void handleTaskComplete(int id){
        TaskComplete tc = new TaskComplete(id);
        try {
            this.sender.sendData(tc.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlePullTrafficSummary() {
        TrafficSummary summary = new TrafficSummary(this.numOfMSent, this.sumOfSent, this.numOfMReceived, this.sumOfReceived);
        try {
            this.sender.sendData(summary.getBytes());
            this.numOfMSent = 0;
            this.sumOfSent = 0;
            this.numOfMReceived = 0;
            this.sumOfReceived = 0;
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void handleDeregister(String status) {
        System.out.println(status);
        System.exit(0);
    }
}
