package cs455.overlay.node;

import java.io.IOException;

public interface Node {
    void handleEvent(int id, int dataLength, byte[] data) throws IOException;
}
