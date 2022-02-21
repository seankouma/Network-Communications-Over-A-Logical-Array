package cs455.overlay.node;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import java.io.IOException;
import cs455.overlay.wireformats.*;

import java.util.EmptyStackException;
import java.util.Random;


public class MessageForwarder implements Runnable {
    TCPSender sender = null;
    public int numOfMSent = 0;
    public int sumOfSent = 0;
    public int numOfMReceived;
    public int sumOfReceived;
    int num = 0;
    byte[] data;
    MessagingNode parent = null;

    MessageForwarder(TCPSender sender, MessagingNode parent) {
        this.sender = sender;
        this.parent = parent;
    }

    @Override
    public void run() {
        while (true) {
            try {
                    DataTraffic traffic = parent.queue.pollLast();
                    if (traffic != null) {
                        this.sender.sendData(traffic.getBytes());
                    } else {
                        
                    }
            } catch (IOException e) {
                
            }
           
        }
    }
}
