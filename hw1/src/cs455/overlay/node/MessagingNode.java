package cs455.overlay.node;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

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
    private MessagingNodeHelper helper = null;

    MessagingNode(String hostname, int otherPort) throws IOException, InterruptedException {
        // Initiate variables and start threads
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

        // Listen for CLI input
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
        // This method handles all the events. So as soon as the TCPReceiverThread gets the message, it calls this function
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

    public void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException {
        this.peerSocket = new Socket(connect.getIp(), connect.getPort());
        System.out.println("Connected to: " + Integer.toString(connect.getID()));
        this.peerSender = new TCPSender(peerSocket);
    }

    public void handleDataTraffic(byte[] data) {
        try {
            DataTraffic traffic = new DataTraffic(data);
            ++numOfMReceived;
            sumOfReceived += traffic.random;
            if (this.numOfMReceived % 10000 == 0) {
                System.out.println("Total Received: " + this.numOfMReceived + " | ID: " + traffic.id + " My ID: " + this.identifier);
            }
            if (traffic.id != this.identifier) peerSender.sendData(traffic.getBytes());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            TrafficSummary summary = new TrafficSummary(this.numOfMSent, this.sumOfSent, this.numOfMReceived, this.sumOfReceived, InetAddress.getLocalHost().getHostName());
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

    public void setIdentifier(int id) {
        this.identifier = id;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int otherPort = Integer.parseInt(args[1]);
        MessagingNode node = new MessagingNode(args[0], otherPort);
    }
}
