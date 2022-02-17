package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;

import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.DataTraffic;

public interface Node {
    void setIdentifier(int id);
    int getIdentifier();
    void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException;
    void handleTaskInitiate(int num);
    void handleDataTraffic(DataTraffic traffic);
    void handleTaskComplete(int id);
}
