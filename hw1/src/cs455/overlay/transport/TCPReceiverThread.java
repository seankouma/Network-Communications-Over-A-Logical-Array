package cs455.overlay.transport;

import java.net.*;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.wireformats.*;
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
                // System.out.println("Message Type: " + Integer.toString(id) + " message length: " + Integer.toString(dataLength));
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

    synchronized void handleEvent(int id, int dataLength, byte[] data) throws IOException {
        switch (id) {
            case Protocol.REGISTER_REQUEST:
                Register register = new Register(data, dataLength);
                int identifier = Registry.register(register);
                sendRegisterResponse(identifier);
                break;
        
            case Protocol.REGISTER_RESPONSE:
                RegisterResponse response = new RegisterResponse(data);
                caller.setIdentifier(response.identifier);
                System.out.println("Successful registration. ID is: " + Integer.toString(response.identifier));
                break;
            case Protocol.CONNECT:
                ConnectionsDirective connect = new ConnectionsDirective(data, dataLength);
                caller.handleConnect(connect);
                break;
            case Protocol.TASK_INITIATE:
                TaskInitiate task = new TaskInitiate(data);
                caller.handleTaskInitiate(task.sendMessages);
                break;
            case Protocol.DATA_TRAFFIC:
                caller.handleDataTraffic(data);
                break;
            case Protocol.DEREGISTER_REQUEST:
                Deregister dereg = new Deregister(data, dataLength);
                caller.deregister(dereg, dataLength);
                break;
            case Protocol.TASK_COMPLETE:
                TaskComplete completed = new TaskComplete(data);
                caller.handleTaskComplete(completed.getIdentifier());
                break;
            case Protocol.PULL_TRAFFIC_SUMMARY:
                caller.handlePullTrafficSummary();
                break;
            case Protocol.TRAFFIC_SUMMARY:
                TrafficSummary summary = new TrafficSummary(data);
                caller.handleTrafficSummary(summary);
                break;
            case Protocol.DEREGISTER_RESPONSE:
                DeregisterResponse resp = new DeregisterResponse(data, dataLength);
                this.caller.handleDeregister(resp.getStatus());
                break;
            default:
                break;
        }
    }

    void sendRegisterResponse(int identifier) throws IOException {
        byte[] bytes = getResponseBytes(identifier);
        Socket socket = Registry.nodes.get(identifier);
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