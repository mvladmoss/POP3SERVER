package pop3.command;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerResponse {

    private boolean isPositiveResponse;
    private List<String> messages;

    public ServerResponse() {
        isPositiveResponse = false;
        messages = new ArrayList<>();
    }

    public ServerResponse(boolean isPositiveResponse) {
        this.isPositiveResponse = isPositiveResponse;
        messages = new ArrayList<>();
    }

    public ServerResponse(boolean isPositiveResponse, String arg) {
        this.isPositiveResponse = isPositiveResponse;
        messages = new ArrayList<>();
        messages.add(arg);
    }

    public static String getLineEnd() {
        return "\r\n";
    }

    void setPositiveResponse() {
        this.isPositiveResponse = true;
    }

    void addMessage(String arg) {
        messages.add(arg);
    }

    void clearMessages() {
        messages.clear();
    }

    void setResponse(String arg) {
        this.isPositiveResponse = true;
        clearMessages();
        addMessage(arg);
    }

    public String buildResponse() {
        StringBuilder str = new StringBuilder();
        str.append(isPositiveResponse ? "+OK" : "-ERR");
        str.append(getLineEnd());
        for (String arg : messages) {
            str.append(arg).append(getLineEnd());
        }
        return str.toString();
    }
}
