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

    public acknowledge(byte[] marshalledbytes) throws IOException{
        
    }

    @Override
    public byte[] getBytes() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getType() {
        // TODO Auto-generated method stub
        return 0;
    }
    // registry responds to messaging nodes with this ack
    //tells them to wait until all nodes are 
    //initialized and sorted before assigning neighbors to each node


}
