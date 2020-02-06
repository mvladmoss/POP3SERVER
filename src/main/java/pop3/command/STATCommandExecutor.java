package pop3.command;

import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import static pop3.command.POP3Command.STAT;

public class STATCommandExecutor implements CommandExecutor {

	@Override
	public ServerResponse execute(Session session, Server server) {
		CommandValidatorUtils.validateState(STAT.getCommand(), session, SessionState.TRANSACTION);
		Mailbox mail = server.getUserMailbox(session.getUser());
		return new ServerResponse(true, mail.getMessageCount() + " " + mail.getMailSize());
	}

}
