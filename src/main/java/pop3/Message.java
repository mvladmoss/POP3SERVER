package pop3;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import pop3.command.ServerResponse;

@AllArgsConstructor
@Data
public class Message implements Serializable {

    private String subject;
    private String sender;
    private String replyTo;
    private String recipient;
    private String body;

    @Override
    public String toString() {
        return  "Subject=" + subject + ServerResponse.getLineEnd() +
                "Sender=" + sender + ServerResponse.getLineEnd() +
                "ReplyTo=" + replyTo + ServerResponse.getLineEnd() +
                "Recipient=" + recipient + ServerResponse.getLineEnd() +
                "Body=[" + body + ']';
    }
}
