package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RegisterResponse implements Protocol, Event {
    int messageType = Protocol.REGISTER_RESPONSE;
    public int identifier = 0;

    public RegisterResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        identifier = din.readInt();
        baInputStream.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getType() {
        return messageType;
    }
    
}
