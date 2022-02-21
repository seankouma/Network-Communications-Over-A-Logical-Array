package cs455.overlay.node;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import java.io.IOException;
import cs455.overlay.wireformats.*;
import java.util.Random;


public class MessagingNodeHelper implements Runnable {
    TCPSender sender = null;
    public int identifier = 0;
    public int numOfMSent = 0;
    public int sumOfSent = 0;
    int num = 0;
    MessagingNode parent = null;

    MessagingNodeHelper(TCPSender sender, MessagingNode parent, int identifier, int num) {
        this.sender = sender;
        this.parent = parent;
        this.identifier = identifier;
        this.num = num;
    }

    @Override
    public void run() {
        System.out.println("Messages to send from node: " + Integer.toString(num));
        Random rand = new Random();
        for (int i = 0; i < num; i++) {
            DataTraffic traffic = new DataTraffic(rand.nextInt(), this.identifier);
            try {
                this.numOfMSent += 1;
                this.sumOfSent += traffic.random;
                this.sender.sendData(traffic.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("NODE FINISHED");
        this.updateTotals();
        parent.handleTaskComplete(parent.identifier);
    }

    void updateTotals() {
        parent.numOfMSent += this.numOfMSent;
        parent.sumOfSent += this.sumOfSent;
    }
}
