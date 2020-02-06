package pop3.command;

import io.vavr.control.Try;
import pop3.Mailbox;
import pop3.Server;
import pop3.SessionState;

import java.util.List;

import static pop3.command.POP3Command.PASS;
import static pop3.command.POP3Command.USER;

public class CommandValidatorUtils {

    private static final String INVALID_SESSION_STATUS_EXCEPTION = "%s lastCommand can only be used in %s state";
    private static final String INVALID_ARGUMENTS_NUMBER_EXCEPTION = "Expected %d arguments, but was received %d";
    private static final String INVALID_ARGUMENTS_NUMBER_RANGE_EXCEPTION = "Expected %s arguments, but was received %d";
    private static final String INVALID_MESSAGE_INDEX_FORMAT_EXCEPTION = "Invalid message index format was received";
    private static final String INVALID_MESSAGE_INDEX_EXCEPTION = "Invalid message index %d was received. There is no messages with such index";
    private static final String MESSAGE_ALREADY_MARKED_FOR_DELETION_EXCEPTION = "Message has already marked for deletion";
    private static final String ILLEGAL_COMMAND_ORDER_EXCEPTION = "Command %s was received before %s";
    private static final String INCORRECT_PASSWORD_FORMAT_EXCEPTION = "Was received empty password";
    private static final String INCORRECT_PASSWORD_FOR_USER_EXCEPTION = "Was received incorrect password for user %s";

    public static void validateState(String command, Session session, SessionState requiredState) {
        if (session.getSessionState() != requiredState) {
            throw new IllegalArgumentException(String.format(INVALID_SESSION_STATUS_EXCEPTION, command,
                    requiredState.name()));
        }
    }

    public static void validateArgumentsNumber(Integer requiredArgumentNumber, String[] commandArgs) {
        if (requiredArgumentNumber != commandArgs.length) {
            throw new IllegalArgumentException(String.format(INVALID_ARGUMENTS_NUMBER_EXCEPTION, requiredArgumentNumber,
                    commandArgs.length));
        }
    }

    public static void validateArgumentsNumber(List<Integer> requiredArgumentNumbers, String[] commandArgs) {
        if (!requiredArgumentNumbers.contains(commandArgs.length)) {
            throw new IllegalArgumentException(String.format(INVALID_ARGUMENTS_NUMBER_RANGE_EXCEPTION,
                    requiredArgumentNumbers.toString(), commandArgs.length));
        }
    }

    public static Integer validateAndGetMessageIndex(String[] commandArgs, Mailbox mail) {
        int messageIndex =
                Try
                        .of(() -> Integer.parseInt( commandArgs[0]))
                        .getOrElseThrow(() -> new IllegalArgumentException(INVALID_MESSAGE_INDEX_FORMAT_EXCEPTION));
        if (!mail.isValidIndex(messageIndex)) {
            throw new IllegalArgumentException(String.format(INVALID_MESSAGE_INDEX_EXCEPTION, messageIndex));
        }
        if (mail.isMessageMarked(messageIndex)) {
            throw new IllegalStateException(MESSAGE_ALREADY_MARKED_FOR_DELETION_EXCEPTION);
        }
        return messageIndex;
    }

    public static void validateUserSession(Session session) {
        if (session.getUser().isEmpty()) {
            throw new IllegalArgumentException(String.format(ILLEGAL_COMMAND_ORDER_EXCEPTION, PASS.getCommand(), USER.getCommand()));
        }
    }

    public static void validatePassword(Session session, Server server) {
        String password = String.join("", CommandParser.getCommandArgs(session.getLastCommand()));
        if (password.isEmpty()) {
            throw new IllegalArgumentException(INCORRECT_PASSWORD_FORMAT_EXCEPTION);
        }
        String actualPassword = server.getUserPassword(session.getUser());
        if (!actualPassword.equals(password)) {
            throw new IllegalArgumentException(String.format(INCORRECT_PASSWORD_FOR_USER_EXCEPTION, session.getUser()));
        }
    }
}
