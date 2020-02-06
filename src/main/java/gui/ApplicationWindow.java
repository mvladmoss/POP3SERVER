package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import pop3.Server;

public class ApplicationWindow extends Application {

    private Server server = Server.getInstance();
    private TextArea textArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("POP3 Server");
        Pane anchorPane = new AnchorPane();
        server.setWindow(this);
        textArea = new TextArea();
        textArea.setMinHeight(300);
        textArea.setMinWidth(400);
        AnchorPane.setLeftAnchor(textArea, 10d);
        AnchorPane.setTopAnchor(textArea, 10d);

        Button startServerButton = createButton("Start Server", 130d, 510d);
        startServerButton.setOnAction(event -> {
            if (server.isRunning()) {
                server.stop();
                startServerButton.setText("Start server");
            } else {
                server.start();
                startServerButton.setText("Stop server");
            }
        });

        Button clearLogButton = createButton("Clear log", 350d, 220d);
        clearLogButton.setOnAction(event -> textArea.clear());

        anchorPane.getChildren().addAll(startServerButton, clearLogButton,textArea);
        Scene scene = new Scene(anchorPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createButton(String buttonName, double topAnchor, double leftAnchor) {
        Button button = new Button(buttonName);
        AnchorPane.setTopAnchor(button, topAnchor);
        AnchorPane.setLeftAnchor(button, leftAnchor);
        return button;
    }

    public void logMessage(String message) {
        textArea.appendText(message + '\n');
    }

    @Override
    public void stop() throws Exception {
        this.server.setRunning(false);
    }
}
