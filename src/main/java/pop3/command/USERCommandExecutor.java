package pop3.command;

import pop3.Server;
import pop3.SessionState;

import static pop3.command.POP3Command.USER;

public class USERCommandExecutor implements CommandExecutor {

	private static final String NOT_REGISTERED_MESSAGE = "User %s is not registered";
	private static final String ALREADY_SIGNED_IN_MESSAGE = "User %s has already signed in";
	private static final String USER_FOUND_MESSAGE = "User accepted";

	@Override
	public ServerResponse execute(Session session, Server server) {

		CommandValidatorUtils.validateState(USER.getCommand(), session, SessionState.AUTHORIZATION);

		String[] commandArgs = CommandParser.getCommandArgs(session.getLastCommand());
		String user = String.join("", commandArgs);
		if (!server.hasUser(user)) {
			return new ServerResponse(false, String.format(NOT_REGISTERED_MESSAGE, user));
		}
		if (server.getUserMailbox(user).isLocked()) {
			return new ServerResponse(false, String.format(ALREADY_SIGNED_IN_MESSAGE, user));
		}
		session.setUser(user);
		return new ServerResponse(true, String.format(USER_FOUND_MESSAGE, user));
	}
}
