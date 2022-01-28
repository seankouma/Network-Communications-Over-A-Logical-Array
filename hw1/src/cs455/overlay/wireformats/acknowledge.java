package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class acknowledge implements Protocol, Event {

    int messageType = REGISTER_REPONSE;
    byte status;
    int identifier;
    String info;
    
    public acknowledge(byte status, int identifier, String info){
        this.status = status;
        this.identifier = identifier;
        this.info = info;
    }  

    public acknowledge(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream byteArrInputStream =
        new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
        new DataInputStream(new BufferedInputStream(byteArrInputStream));
        messageType = din.readInt();
        int statusLength = din.readInt();
        int identifierLength = din.readInt();
        byte[] statusBytes = new byte[statusLength];
        byte[] identifierBytes = new byte[identifierLength];
        din.readFully(statusBytes);
        din.readFully(identifierBytes);
        status = din.readByte();
        identifier = din.readInt();
        byteArrInputStream.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout =
        new DataOutputStream(new BufferedOutputStream(byteArrOutputStream));
        dout.writeInt(messageType);
        dout.writeInt(identifier);
        dout.writeByte(status);
        dout.flush();
        marshalledBytes = byteArrOutputStream.toByteArray();
        byteArrOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public int getType() {
        return messageType;
    }

    public byte getStatus(){
        return status;
    }

    public int getIdentifier(){
        return identifier;
    }
    // registry responds to messaging nodes with this ack
    //tells them to wait until all nodes are 
    //initialized and sorted before assigning neighbors to each node


}
