package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Protocol, Event {
    int messageType = Protocol.TRAFFIC_SUMMARY;
    String ip;
    int port;
    public int numOfMSent = 0;
    public int sumOfSent = 0;
    public int numOfMReceived = 0;
    public int sumOfReceived = 0;
    
    

    public TrafficSummary(int numOfMSent, int sumOfSent, int numOfMReceived, int sumOfReceived) {
        this.numOfMSent = numOfMSent;
        this.sumOfSent = sumOfSent;
        this.numOfMReceived = numOfMReceived;
        this.sumOfReceived = sumOfReceived;
    }
    
    public TrafficSummary(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        numOfMSent = din.readInt();
        sumOfSent = din.readInt();
        numOfMReceived = din.readInt();
        sumOfReceived = din.readInt();
        baInputStream.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeInt(this.messageType);
        dout.writeInt(this.numOfMSent);
        dout.writeInt(this.sumOfSent);
        dout.writeInt(this.numOfMReceived);
        dout.writeInt(this.sumOfReceived);
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public int getType() {
        return messageType;
    }
}