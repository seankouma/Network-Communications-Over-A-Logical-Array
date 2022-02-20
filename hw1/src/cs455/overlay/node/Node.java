package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;

import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.DataTraffic;
import cs455.overlay.wireformats.*;

public interface Node {
    void setIdentifier(int id);
    int getIdentifier();
    void handleConnect(ConnectionsDirective connect) throws UnknownHostException, IOException;
    void handleTaskInitiate(int num);
    void handleDataTraffic(byte[] data);
    void handleTaskComplete(int id);
    void handlePullTrafficSummary();
    void handleTrafficSummary(TrafficSummary summary);
    void handleDeregister(String status);
    boolean deregister(Deregister dr, int st) throws UnknownHostException, IOException;
}
