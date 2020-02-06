package pop3.command;

import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import static pop3.command.POP3Command.RETR;

public class RETRCommandExecutor implements CommandExecutor {

    @Override
    public ServerResponse execute(Session session, Server server) {

        CommandValidatorUtils.validateState(RETR.getCommand(), session, SessionState.TRANSACTION);

        String[] commandArgs = CommandParser.getCommandArgs(session.getLastCommand());
        CommandValidatorUtils.validateArgumentsNumber(1, commandArgs);

        Mailbox mail = server.getUserMailbox(session.getUser());
        Integer messageIndex = CommandValidatorUtils.validateAndGetMessageIndex(commandArgs, mail);
        String msg = mail.getMessage(messageIndex).toString();
        return new ServerResponse(true, msg);
    }

}
