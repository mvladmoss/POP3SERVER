package pop3.command;

import pop3.Server;

public interface CommandExecutor {
	
	ServerResponse execute(Session session, Server server);
}