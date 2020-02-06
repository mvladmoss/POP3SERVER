package pop3;

public enum EventType {
    START_SERVER,
    STOP_SERVER,
    RECEIVE_COMMAND,
    SEND_RESPONSE,
    ACCEPT_CLIENT,
    DISCONNECT_CLIENT
}
