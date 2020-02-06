package pop3.command;

import pop3.Server;
import pop3.SessionState;

import static pop3.command.POP3Command.NOOP;

public class NOOPCommandExecutor implements CommandExecutor {
	
	@Override
	public ServerResponse execute(Session session, Server server) {
		CommandValidatorUtils.validateState(NOOP.getCommand(), session, SessionState.TRANSACTION);
		return new ServerResponse(true);
	}

}
