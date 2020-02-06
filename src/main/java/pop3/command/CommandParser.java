package pop3.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

    private static final  Pattern COMMAND_PATTERN = Pattern.compile("^[\\w]{3,4}[ \\w]{0,40}$");
    private static final String INVALID_COMMAND_MESSAGE = "invalid command";

    public static boolean validate(String command) {
        if (command == null) {
            return false;
        }

        Optional<POP3Command> maybeCommand = Arrays.stream(POP3Command.values())
                .filter(com -> com.getCommand().equalsIgnoreCase(getCommandKeyword(command)))
                .findAny();
        if (!maybeCommand.isPresent()) {
            return false;
        }

        Matcher commandMatcher = COMMAND_PATTERN.matcher(command);
        return commandMatcher.matches();
    }

    public static String getCommandKeyword(String command) {
        String[] argsAndKeyword = command.split(" ");
        return argsAndKeyword[0].toUpperCase();
    }

    public static String[] getCommandArgs(String command) {
        String[] argsAndKeyword = command.split(" ");
        if (argsAndKeyword.length == 1) {
            return new String[0];
        } else {
            return Arrays.copyOfRange(argsAndKeyword, 1, argsAndKeyword.length);
        }
    }

    public static ServerResponse getInvalidResponse() {
        return new ServerResponse(false, Collections.singletonList(INVALID_COMMAND_MESSAGE));
    }

    public static int getLineMaxLength() {
        return 512;
    }

    public static String getLineEnd() {
        return "\\r\\n";
    }
}
