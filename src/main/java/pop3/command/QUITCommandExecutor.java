package pop3.command;


import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

@Slf4j
public class QUITCommandExecutor implements CommandExecutor {

    private static final String SERVER_STOPPED_MESSAGE = "Server was stopped";
    private static final String SERVER_LEFT_MESSAGE_FOR_USER_MESSAGE = "Server was stopped. Server persist %d messages for user %s";

    @Override
    public ServerResponse execute(Session session, Server server) {
        ServerResponse response = new ServerResponse();

        if (session.getSessionState() == SessionState.AUTHORIZATION) {
            response.setResponse(SERVER_STOPPED_MESSAGE);
        } else if (session.getSessionState() == SessionState.TRANSACTION) {
            session.setSessionState(SessionState.UPDATE);
            Mailbox mail = server.getUserMailbox(session.getUser());
            mail.deleteMarkedMessages();
            response.setResponse(String.format(SERVER_LEFT_MESSAGE_FOR_USER_MESSAGE, mail.getMessageCount(), session.getUser()));
            Try
                    .run(() -> mail.saveToFile(server.getUserMailFileName(session.getUser())))
                    .onSuccess(ex -> {
                        mail.unlock();
                        session.setUser("");
                    })
                    .onFailure(ex -> log.error(ex.getMessage()));
        }
        session.setCloseConnection(true);
        return response;
    }

}
