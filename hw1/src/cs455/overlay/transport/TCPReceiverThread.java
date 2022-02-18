package cs455.overlay.transport;

import java.net.*;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.DataTraffic;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.Deregister;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.PullTrafficSummary;
import cs455.overlay.wireformats.TrafficSummary;

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

    void handleEvent(int id, int dataLength, byte[] data) throws IOException {
        switch (id) {
            case Protocol.REGISTER_REQUEST:
                Register register = new Register(data, dataLength);
                int identifier = Registry.register(register);
                sendRegisterResponse(identifier);
                break;
        
            case Protocol.REGISTER_RESPONSE:
                RegisterResponse response = new RegisterResponse(data);
                caller.setIdentifier(response.identifier);
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
                DataTraffic traffic = new DataTraffic(data);
                caller.handleDataTraffic(traffic);
                break;
            case Protocol.DEREGISTER_REQUEST:
                int boolNum = 0;
                Deregister dereg = new Deregister(data, dataLength);
                boolean isRegistered = Registry.deregister(dereg, dataLength);
                if(isRegistered) boolNum = 1;
                sendRegisterResponse(boolNum);
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