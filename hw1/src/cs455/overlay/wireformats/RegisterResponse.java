package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RegisterResponse implements Protocol, Event {
    int messageType = Protocol.REGISTER_RESPONSE;
    public int identifier = 0;
    public byte status;

    public RegisterResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        identifier = din.readInt();
        baInputStream.close();
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
    
}
