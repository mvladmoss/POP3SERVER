package pop3.command;

public enum POP3Command {
    DELE("DELE", new DELECommandExecutor()),
    LIST("LIST", new LISTCommandExecutor()),
    NOOP("NOOP", new NOOPCommandExecutor()),
    PASS("PASS", new PASSCommandExecutor()),
    QUIT("QUIT", new QUITCommandExecutor()),
    RETR("RETR", new RETRCommandExecutor()),
    RSET("RSET", new RSETCommandExecutor()),
    STAT("STAT", new STATCommandExecutor()),
    TOP("TOP", new TOPCommandExecutor()),
    USER("USER", new USERCommandExecutor());

    private String command;
    private CommandExecutor commandExecutor;

    POP3Command(String command, CommandExecutor commandExecutor) {
        this.command = command;
        this.commandExecutor = commandExecutor;
    }

    public String getCommand() {
        return command;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }
}
