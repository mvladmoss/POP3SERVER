package pop3;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.ApplicationWindow;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import pop3.command.POP3Command;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static pop3.EventType.ACCEPT_CLIENT;
import static pop3.EventType.DISCONNECT_CLIENT;
import static pop3.EventType.RECEIVE_COMMAND;
import static pop3.EventType.SEND_RESPONSE;
import static pop3.EventType.START_SERVER;
import static pop3.EventType.STOP_SERVER;
import static pop3.command.POP3Command.*;

@Slf4j
public class Server {

    private static final int POP3_IP_PORT = 110;
    private static final int TOTAL_CLIENTS = 5;
    private static final int TIMEOUT = 1_000;
    private static final String USERS_FILE_PATH = "D:\\6sem\\POP3SERVER\\src\\main\\resources\\users.txt";

    private static Server instance;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Map<String, Mailbox> userMaildrop = new HashMap<>();
    private Map<String, String> userPassword = new HashMap<>();
    private ApplicationWindow view;
    private ExecutorService executor;

    private Server() {

    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public void start() {
        if (isRunning) {
            log.error("Server has already been started");
            return;
        }
        Thread serverThread = new Thread(() -> Try
                .run(() -> {
                    loadUsers();
                    loadMail();
                    logEvent(ServerEvent.of(START_SERVER, getCurrentTime()));
                    isRunning = true;
                    executor = Executors.newFixedThreadPool(TOTAL_CLIENTS);
                    Try
                            .run(() -> {
                                serverSocket = new ServerSocket(POP3_IP_PORT);
                                serverSocket.setSoTimeout(TIMEOUT);
                            })
                            .onFailure(exc -> log.error("Failed to set up server socket"));
                    acceptClients();
                    shutdown();
                })
                .onFailure(exc -> view.logMessage(exc.getMessage())));
        serverThread.start();
    }

    private void shutdown() {
        if (!serverSocket.isClosed()) {
            Try
                    .run(() -> serverSocket.close())
                    .onFailure(exc -> log.error(exc.getMessage()));
        }
        executor.shutdownNow();
        logEvent(ServerEvent.of(EventType.STOP_SERVER, getCurrentTime()));
    }

    public void logEvent(ServerEvent serverEvent) {
        view.logMessage(logServerAction(serverEvent));
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        isRunning = false;
    }

    private void acceptClients() {
        while (!serverSocket.isClosed() && isRunning) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            executor.execute(createClientHandler(clientSocket));
        }
    }

    private ClientHandler createClientHandler(Socket clientSocket) {
        ClientHandler client = new ClientHandler(clientSocket, this);
        logEvent(ServerEvent.of(EventType.ACCEPT_CLIENT, client.getClientAddress(), getCurrentTime()));
        List<POP3Command> standardClientCommands = Arrays.asList(USER, PASS, QUIT, STAT, LIST, RETR, DELE, NOOP, RSET, TOP);
        client.setAvailableCommands(standardClientCommands);
        return client;
    }

    private String logServerAction(ServerEvent event) {

        String time = event.getTime().toString();

        StringBuilder message = new StringBuilder(time + ": ");
        String additionalInfo = Match(event.getType()).of(
                Case($(START_SERVER), () -> "Server was started"),
                Case($(STOP_SERVER), () -> "Server was stopped"),
                Case($(ACCEPT_CLIENT), () -> "Accept client " + event.getArguments().get(0)),
                Case($(DISCONNECT_CLIENT), () -> "Disconnect client " + event.getArguments().get(0)),
                Case($(RECEIVE_COMMAND), () -> "Received lastCommand \"" + event.getArguments().get(1) + "\" from " + event.getArguments().get(0)),
                Case($(SEND_RESPONSE), () -> "Send response to " + event.getArguments().get(0) + "\n---\n" + event.getArguments().get(1) + "---"));
        message.append(additionalInfo);

        return message.toString();
    }

    public void setWindow(ApplicationWindow window) {
        this.view = window;
    }

    private void loadUsers() {
        Try
                .run(() -> {
                    List<String> lines = Files.readAllLines(Paths.get(USERS_FILE_PATH));
                    for (String line : lines) {
                        String[] userAndPass = line.split(" ", 2);
                        userPassword.put(userAndPass[0], userAndPass[1]);
                    }
                })
                .onFailure(ex -> log.error("Failed to load users and passwords from file."));
    }

    private void loadMail() {
        for (String user : userPassword.keySet()) {
            Mailbox mail = new Mailbox();

            try {
                mail.loadFromFile(getUserMailFileName(user));
            } catch (IOException e) {
                e.printStackTrace();
            }

            userMaildrop.put(user, mail);
        }
    }

    public boolean hasUser(String user) {
        return userMaildrop.get(user) != null;
    }

    public String getUserPassword(String user) {
        return userPassword.get(user);
    }

    public Mailbox getUserMailbox(String user) {
        return userMaildrop.get(user);
    }

    public String getUserMailFileName(String user) {
        String MAIL_FOLDER = "emails";
        return File.separator + MAIL_FOLDER + File.separator + user + "_email.json";
    }

    public Instant getCurrentTime() {
        return Instant.now();
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
