package pop3.command;

import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import static pop3.command.POP3Command.RSET;

public class RSETCommandExecutor implements CommandExecutor {

	private static final String ALL_MESSAGE_RESTORED = "All message was restored";

	@Override
	public ServerResponse execute(Session session, Server server) {
		CommandValidatorUtils.validateState(RSET.getCommand(), session, SessionState.TRANSACTION);
		Mailbox mail = server.getUserMailbox(session.getUser());
		for (Integer msgIndex : mail.getMarkedMessages()) {
			mail.unmarkMessageToDelete(msgIndex);
		}
		return new ServerResponse(true, ALL_MESSAGE_RESTORED);
	}
}
