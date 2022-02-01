package cs455.overlay.wireformats;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.ConnectionsDirective;


public class deregister implements Protocol, Event{
    public int messageType = DEREGISTER_REQUEST;
    public String ip;
    public int port;

    public deregister(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    //check if ID valid

    public deregister(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        port = din.readInt();
        int ipLength = din.readInt();
        byte[] ipBytes = new byte[ipLength];
        ip = new String(ipBytes);
        baInputStream.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout =
        new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeInt(messageType);
        byte[] ipBytes = ip.getBytes();
        int elementLength = ipBytes.length;
        dout.writeInt(elementLength);
        dout.write(ipBytes);
        dout.writeInt(port);
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public int getType() {
        // TODO Auto-generated method stub
        return messageType;
    }



}
