package pop3.command;

import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import static pop3.command.POP3Command.DELE;

public class DELECommandExecutor implements CommandExecutor {

	private static final String MESSAGE_ALREADY_MARKED_FOR_DELETION_MESSAGE = "Message has already marked for deletion";
	private static final String MESSAGE_MARKED_FOR_DELETION_MESSAGE = "Message was marked for deletion";

	@Override
	public ServerResponse execute(Session session, Server server) {
		CommandValidatorUtils.validateState(DELE.getCommand(), session, SessionState.TRANSACTION);
		String[] commandArgs = CommandParser.getCommandArgs(session.getLastCommand());
		CommandValidatorUtils.validateArgumentsNumber(1, commandArgs);

		Mailbox mail = server.getUserMailbox(session.getUser());
		Integer messageIndex = CommandValidatorUtils.validateAndGetMessageIndex(commandArgs, mail);

		mail.markMessageToDelete(messageIndex);
		return new ServerResponse(true, MESSAGE_MARKED_FOR_DELETION_MESSAGE);
	}

}
