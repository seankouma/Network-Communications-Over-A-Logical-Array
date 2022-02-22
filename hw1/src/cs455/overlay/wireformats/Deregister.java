package cs455.overlay.wireformats;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class Deregister implements Protocol, Event{
    public int messageType = DEREGISTER_REQUEST;
    public String ip;
    public int port;

    public Deregister(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    //check if ID valid

    public Deregister(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        port = din.readInt();
        int ipLength = din.readInt();
        byte[] ipBytes = new byte[ipLength];
        ip = new String(ipBytes);
        baInputStream.close();
        din.close();
    }

    public Deregister(byte[] marshalledBytes, int dataLength) throws IOException {
        ByteArrayInputStream baInputStream =
        new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
        new DataInputStream(new BufferedInputStream(baInputStream));
        int ipLength = din.readInt();
        byte[] ipBytes = new byte[ipLength];
        din.readFully(ipBytes);
        ip = new String(ipBytes);
        port = din.readInt();
        baInputStream.close();
        din.close();
    }

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
