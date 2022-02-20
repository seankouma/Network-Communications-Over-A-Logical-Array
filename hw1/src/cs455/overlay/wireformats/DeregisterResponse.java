package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeregisterResponse implements Protocol, Event {
    int messageType = DEREGISTER_RESPONSE;
    String status;
    int port;

    // TODO remove these from being public and manage them from the factory instead
    public DeregisterResponse(String status, int port) {
        this.status = status;
        this.port = port;
    }

    public DeregisterResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream =
        new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
        new DataInputStream(new BufferedInputStream(baInputStream));
        messageType = din.readInt();
        int ipLength = din.readInt();
        byte[] ipBytes = new byte[ipLength];
        din.readFully(ipBytes);
        status = new String(ipBytes);
        port = din.readInt();
        baInputStream.close();
        din.close();
    }

    public DeregisterResponse(byte[] marshalledBytes, int dataLength) throws IOException {
        ByteArrayInputStream baInputStream =
        new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
        new DataInputStream(new BufferedInputStream(baInputStream));
        int ipLength = din.readInt();
        byte[] ipBytes = new byte[ipLength];
        din.readFully(ipBytes);
        status = new String(ipBytes);
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
        byte[] ipBytes = status.getBytes();
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

    public String getStatus() {
        return this.status;
    }

    public int getPort() {
        return this.port;
    }
}
