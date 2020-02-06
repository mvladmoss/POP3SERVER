package pop3.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import pop3.SessionState;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    private String lastCommand;
    private String user;
    private SessionState sessionState;
    private boolean closeConnection;
}
