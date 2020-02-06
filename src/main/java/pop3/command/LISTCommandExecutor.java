package pop3.command;

import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import java.util.Arrays;

import static pop3.command.POP3Command.LIST;

public class LISTCommandExecutor implements CommandExecutor {

    @Override
    public ServerResponse execute(Session session, Server server) {
        ServerResponse response = new ServerResponse();

        CommandValidatorUtils.validateState(LIST.getCommand(), session, SessionState.TRANSACTION);
        Mailbox mail = server.getUserMailbox(session.getUser());

        String[] commandArgs = CommandParser.getCommandArgs(session.getLastCommand());
        CommandValidatorUtils.validateArgumentsNumber(Arrays.asList(0, 1), commandArgs);

        if (commandArgs.length == 1) {
            Integer messageIndex = CommandValidatorUtils.validateAndGetMessageIndex(commandArgs, mail);
            response.setResponse(messageIndex + " " + mail.getMessageSize(messageIndex));
        } else {
            response.clearMessages();
            response.setPositiveResponse();
            for (int msgIndex = 0; msgIndex < mail.getMessageCount(); msgIndex++) {
                response.addMessage((msgIndex + 1) + " " + mail.getMessageSize(msgIndex + 1));
            }
        }
        return response;
    }
}
