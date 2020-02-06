package pop3;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import io.vavr.control.Try;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import pop3.command.CommandExecutor;
import pop3.command.CommandParser;
import pop3.command.POP3Command;
import pop3.command.ServerResponse;
import pop3.command.Session;

@Data
@Slf4j
public class ClientHandler implements Runnable {

    private static final String WELCOME_MESSAGE = "POP3 server is start up";

    private Socket socket;
    private DataOutputStream socketOutput;
    private Scanner socketInput;
    private InputStream socketInputStream;
    private String user;
    private SessionState sessionState;
    private boolean closeConnection = false;
    private Server server;
    private List<POP3Command> availableCommands;

    ClientHandler(Socket clientSocket, Server server) {
        this.socket = clientSocket;
        this.server = server;
        this.availableCommands = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            socketInputStream = socket.getInputStream();
            socketInput = new Scanner(socketInputStream);
            socketInput.useDelimiter(CommandParser.getLineEnd());
            socketOutput = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
            return;
        }
        sessionState = SessionState.AUTHORIZATION;
        sendGreeting();
        while (!socket.isClosed() && !closeConnection && !Thread.interrupted()) {
            receiveCommand();
        }
        disconnect();
    }

    private void sendGreeting() {
        sendResponse(new ServerResponse(true, WELCOME_MESSAGE));
    }

    private void sendResponse(ServerResponse response) {
        Try
                .run(() -> {
                    socketOutput.write(response.buildResponse().getBytes(StandardCharsets.US_ASCII));
                    socketOutput.flush();
                    ServerEvent event = ServerEvent.of(EventType.SEND_RESPONSE, server.getCurrentTime());
                    event.addArgument(getClientAddress());
                    event.addArgument(response.buildResponse());
                    server.logEvent(event);
                })
                .onFailure(ex -> disconnect());
    }

    private void receiveCommand() {
        String command = Try
                .of(() -> socketInput.nextLine()).getOrElse("");

        ServerEvent event = ServerEvent.of(EventType.RECEIVE_COMMAND, server.getCurrentTime());
        event.addArgument(getClientAddress());
        event.addArgument(command);
        server.logEvent(event);

        if (CommandParser.validate(command)) {
            CommandExecutor processor = getCommandProcessor(CommandParser.getCommandKeyword(command));
            Session state = getClientSessionState();
            state.setLastCommand(command);
            Consumer<ServerResponse> responseHandler = response -> {
                applyCommandChanges(state);
                sendResponse(response);
            };
            Try
                    .of(() -> processor.execute(state, server))
                    .onSuccess(responseHandler)
                    .onFailure(ex -> {
                        ServerResponse response = new ServerResponse(false, ex.getMessage());
                        responseHandler.accept(response);
                    });

        } else {
            sendResponse(CommandParser.getInvalidResponse());
        }
    }

    private void disconnect() {
        server.logEvent(ServerEvent.of(EventType.DISCONNECT_CLIENT, getClientAddress(), server.getCurrentTime()));
        if (sessionState == SessionState.TRANSACTION) {
            server.getUserMailbox(user).unlock();
        }
        Try
                .run(() -> {
                    socketOutput.close();
                    socketInput.close();

                    if (!socket.isClosed()) {
                        socket.close();
                    }
                })
                .onFailure(ex -> log.error(ex.getMessage()));
    }

    public String getClientAddress() {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    private CommandExecutor getCommandProcessor(String commandKeyword) {
        return POP3Command.valueOf(commandKeyword).getCommandExecutor();
    }

    private Session getClientSessionState() {
        return Session.builder()
                .user(user)
                .sessionState(sessionState)
                .closeConnection(closeConnection)
                .build();
    }

    private void applyCommandChanges(Session state) {
        sessionState = state.getSessionState();
        user = state.getUser();
        closeConnection = state.isCloseConnection();
    }
}
