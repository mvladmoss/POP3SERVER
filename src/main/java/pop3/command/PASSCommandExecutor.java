package pop3.command;

import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import static pop3.command.POP3Command.PASS;

public class PASSCommandExecutor implements CommandExecutor {

    private static final String MAILBOX_NOT_FOUND_MESSAGE = "Mailbox for user %s doesn't found";
    private static final String MAILBOX_ALREADY_LOCKED_MESSAGE = "Mailbox has already locked";
    private static final String PASSWORD_ACCEPTED_MESSAGE = "Password accepted";

    @Override
    public ServerResponse execute(Session session, Server server) {

        CommandValidatorUtils.validateState(PASS.getCommand(), session, SessionState.AUTHORIZATION);
        CommandValidatorUtils.validateUserSession(session);
        CommandValidatorUtils.validatePassword(session, server);

        Mailbox userMailbox = server.getUserMailbox(session.getUser());
        if (userMailbox == null) {
            return new ServerResponse(false, String.format(MAILBOX_NOT_FOUND_MESSAGE, session.getUser()));
        } else if (userMailbox.isLocked()) {
            return new ServerResponse(false, MAILBOX_ALREADY_LOCKED_MESSAGE);
        }
        userMailbox.lock();
        session.setSessionState(SessionState.TRANSACTION);
        return new ServerResponse(true, PASSWORD_ACCEPTED_MESSAGE);
    }
}
