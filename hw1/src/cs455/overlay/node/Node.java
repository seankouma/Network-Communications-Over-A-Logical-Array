package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;

import cs455.overlay.wireformats.ConnectionsDirective;
import cs455.overlay.wireformats.DataTraffic;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.*;

public interface Node {
    void handleEvent(int id, int dataLength, byte[] data) throws IOException;
}
