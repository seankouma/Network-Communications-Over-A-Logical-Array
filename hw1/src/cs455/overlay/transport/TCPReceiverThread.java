package cs455.overlay.transport;

import java.net.*;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.deregister;

import java.io.*;

public class TCPReceiverThread implements Runnable {
    private Socket socket = null;
    private DataInputStream input = null;
    Node caller = null;

    public TCPReceiverThread(Socket socket, Node caller) throws IOException {
        this.socket = socket;
        this.caller = caller;
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public void run() {
        int dataLength;
        while (socket != null) {
            try {
                dataLength = input.readInt();
                int id = input.readInt();
                System.out.println("Message Type: " + Integer.toString(id) + " message length: " + Integer.toString(dataLength));
                byte[] data = new byte[dataLength-4];
                input.readFully(data, 0, dataLength-4);
                handleEvent(id, dataLength-4, data);
            } catch (SocketException se) {
                System.out.println(se.getMessage());
                break;
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                break;
            }
        }
    }

    void handleEvent(int id, int dataLength, byte[] data) throws IOException {
        switch (id) {
            case Protocol.REGISTER_REQUEST:
                Register register = new Register(data, dataLength);
                int identifier = Registry.register(register);
                sendRegisterResponse(identifier);
                System.out.println("Type: " + Integer.toString(register.getType()) + ", IP: " + register.getIp() + ", Port: " + Integer.toString(register.getPort()));
                break;
        
            case Protocol.REGISTER_RESPONSE:
                System.out.println("Response!");
                RegisterResponse response = new RegisterResponse(data);
                caller.setIdentifier(response.identifier);
                System.out.println("Caller ID: " + Integer.toString(caller.getIdentifier()));
                break;
            case Protocol.CONNECT:
                System.out.println("Connections Directive!");
                ConnectionsDirective connect = new ConnectionsDirective(data, dataLength);
                caller.handleConnect(connect);
            case Protocol.DEREGISTER_REQUEST:
                System.out.println("Deregister");
                deregister dereg = new deregister(data, dataLength);
                int identifier = Registry.deregister(register);
                sendRegisterResponse(identifier);
            default:
                break;
        }
    }

    void sendRegisterResponse(int identifier) throws IOException {
        byte[] bytes = getResponseBytes(identifier);
        Socket socket = Registry.map.get(identifier);
        TCPSender sender = new TCPSender(socket);
        sender.sendData(bytes);
    }

    public byte[] getResponseBytes(int identifier) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeInt(Protocol.REGISTER_RESPONSE);
        dout.writeInt(identifier);
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    
}