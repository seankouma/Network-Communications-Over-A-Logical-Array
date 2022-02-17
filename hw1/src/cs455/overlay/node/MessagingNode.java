package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.DataTraffic;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.TaskComplete;

public class MessagingNode implements Node {
    TCPServerThread server = null;
    public TCPSender sender = null;
    public int identifier = 0;
    Socket peerSocket = null;
    TCPSender peerSender = null;
    private int received = 0;

    MessagingNode(int otherPort) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(0);
        server = new TCPServerThread(serverSocket, this);
        Thread sthread = new Thread(server);
        sthread.start();
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket(addr, otherPort);
            } catch (IOException e) {
                socket = null;
            }
        }
        sender = new TCPSender(socket);
        // while (true) {
        //     DataInputStream input  = new DataInputStream(System.in);
        //     String line = input.readLine();
        //     byte[] bytes = getBytes(line);
        //     sender.sendData(bytes);
        // }
        Register register = new Register("127.0.0.1", serverSocket.getLocalPort());
        byte[] bytes = register.getBytes();
        sender.sendData(bytes);
    }

    public byte[] getBytes(String line) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout =
        new DataOutputStream(new BufferedOutputStream(baOutputStream));
        byte[] lineBytes = line.getBytes();
        int elementLength = lineBytes.length;
        dout.writeInt(elementLength);
        dout.write(lineBytes);
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    // To run this, just pass in the port you want this node to use and the port it should communicate with as CLI parameters.
    public static void main(String[] args) throws IOException, InterruptedException {
        int otherPort = Integer.parseInt(args[0]);
        MessagingNode node = new MessagingNode(otherPort);
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
        System.out.println("Connected to: " + Integer.toString(this.peerSocket.getPort()));
        this.peerSender = new TCPSender(peerSocket);
        
    }

    @Override
    public void handleTaskInitiate(int num) {
        System.out.println("Messages to send from node: " + Integer.toString(num));
        Random rand = new Random();
        for (int i = 0; i < num; i++) {
            DataTraffic traffic = new DataTraffic(rand.nextInt(), this.identifier);
            try {
                this.peerSender.sendData(traffic.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.handleTaskComplete(this.identifier);
    }

    @Override
    public void handleTaskComplete(int id){
        TaskComplete tc = new TaskComplete(id);
        try {
            this.sender.sendData(tc.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleDataTraffic(DataTraffic traffic) {
        if (traffic.id == this.identifier) return;
        ++received;
        System.out.println("Num received: " + Integer.toString(received));
        try {
            byte[] data = traffic.getBytes();
            peerSender.sendData(traffic.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
