package cs455.overlay.wireformats;

public interface Protocol {
    public static final int REGISTER_REQUEST = 0;
    public static final int REGISTER_RESPONSE = 1;
    public static final int CONNECT = 2;
    public static final int TASK_INITIATE = 3;
    public static final int DATA_TRAFFIC = 4;
    public static final int DEREGISTER_REQUEST = 5;
    public static final int TASK_COMPLETE = 6;
    public static final int PULL_TRAFFIC_SUMMARY = 7;
    public static final int TRAFFIC_SUMMARY = 8;
    public static final int DEREGISTER_RESPONSE = 9;
}
