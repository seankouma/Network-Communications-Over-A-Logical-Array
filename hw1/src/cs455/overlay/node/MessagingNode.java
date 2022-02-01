package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.Register;

public class MessagingNode implements Node {
    TCPServerThread server = null;
    public TCPSender sender = null;
    public int identifier = 0;
    Socket peerSocket = null;

    MessagingNode(int selfPort, int otherPort) throws IOException, InterruptedException {
        server = new TCPServerThread(selfPort, this);
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
        Register register = new Register("127.0.0.1", selfPort);
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
        int selfPort = Integer.parseInt(args[0]);
        int otherPort = Integer.parseInt(args[1]);
        MessagingNode node = new MessagingNode(selfPort, otherPort);
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
        
    }
    
}
