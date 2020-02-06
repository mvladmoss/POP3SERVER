package pop3.command;


import io.vavr.control.Try;
import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pop3.command.POP3Command.TOP;

public class TOPCommandExecutor implements CommandExecutor {

    private static final String INVALID_LINES_NUMBER_PARAMETER_EXCEPTION = "Incorrect number of lines parameter was received";
    private static final String INVALID_NUMBER_OF_LINES_EXCEPTION = "Required number of lines more than there is in body";


    @Override
    public ServerResponse execute(Session session, Server server) {

        CommandValidatorUtils.validateState(TOP.getCommand(), session, SessionState.TRANSACTION);

        String[] commandArgs = CommandParser.getCommandArgs(session.getLastCommand());
        CommandValidatorUtils.validateArgumentsNumber(2, commandArgs);

        Mailbox mail = server.getUserMailbox(session.getUser());


        Integer messageIndex = CommandValidatorUtils.validateAndGetMessageIndex(commandArgs, mail);
        Integer requiredLinesNumber =
                Try
                        .of((() -> Integer.parseInt(commandArgs[1])))
                        .getOrElseThrow(() -> new IllegalArgumentException(INVALID_LINES_NUMBER_PARAMETER_EXCEPTION));
        String msg = mail.getMessage(messageIndex).getBody();
        List<String> lines = Arrays.stream(msg.split(ServerResponse.getLineEnd()))
                .collect(Collectors.toList());
        if (lines.size() < requiredLinesNumber) {
            throw new IllegalArgumentException(INVALID_NUMBER_OF_LINES_EXCEPTION);
        }

        return new ServerResponse(true, lines.subList(0, requiredLinesNumber).stream()
                .collect(Collectors.joining(ServerResponse.getLineEnd())));
    }


}
