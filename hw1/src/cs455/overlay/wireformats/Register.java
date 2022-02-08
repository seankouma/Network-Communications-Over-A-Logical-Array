package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Register implements Protocol, Event {
    int messageType = REGISTER_REQUEST;
    String ip;
    int port;

    // TODO remove these from being public and manage them from the factory instead
    public Register(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Register(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream =
        new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
        new DataInputStream(new BufferedInputStream(baInputStream));
        messageType = din.readInt();
        int ipLength = din.readInt();
        byte[] ipBytes = new byte[ipLength];
        din.readFully(ipBytes);
        ip = new String(ipBytes);
        port = din.readInt();
        baInputStream.close();
        din.close();
    }

    public Register(byte[] marshalledBytes, int dataLength) throws IOException {
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

    public int getType() {
        return messageType;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }
}
