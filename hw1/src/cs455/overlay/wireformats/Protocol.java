package cs455.overlay.wireformats;

public interface Protocol {
    public static final int REGISTER_REQUEST = 0;
    public static final int REGISTER_RESPONSE = 1;
    public static final int CONNECT = 2;
    public static final int TASK_INITIATE = 3;
    public static final int DATA_TRAFFIC = 4;
    
}
