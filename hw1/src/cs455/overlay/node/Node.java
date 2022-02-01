package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;

import cs455.overlay.wireformats.ConnectionsDirective;

public interface Node {
    void setIdentifier(int id);
    int getIdentifier();
    void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException;
}
