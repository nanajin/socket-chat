package com.example.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.example.chat.server.Server;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ServerApplication extends Application {
    public static ArrayList<Thread> threads;

    public static void main(String[] args) {
        launch();
    }

    public String nickName;
    public int idx;
    public String muteNN;
    public static ObservableList<String> muteList = FXCollections.observableArrayList(); // mute리스트
    public int portNumber;
    public ObservableList<String> clientList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception {
        threads = new ArrayList<>();
        primaryStage.setTitle("Our Chat Server");
        primaryStage.setScene(makePortUI(primaryStage));
        primaryStage.show();

        //추가
        muteList.add("시작");
        muteList.addListener(new Server.MyListener());
        //
    }

    public Scene makePortUI(Stage primaryStage) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(0);
        rootPane.setAlignment(Pos.CENTER);
        Text portText = new Text("Port Number");
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        TextField portTextField = new TextField();
        portText.setFont(Font.font("Tahoma"));
        Button portApprovalButton = new Button("Done");
        portApprovalButton.setOnAction(event -> {
            try {
                Server server = new Server(Integer.parseInt(portTextField.getText()));
                Thread serverThread = (new Thread(server));
                serverThread.setName("Server Thread");
                serverThread.setDaemon(true);
                serverThread.start();
                File directory = new File("image");
                directory.mkdir();
                threads.add(serverThread);
                primaryStage.hide();
                primaryStage.setScene(makeServerUI(server));
                primaryStage.show();
            } catch (IllegalArgumentException e) {
                errorLabel.setText("Invalid port number");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        rootPane.add(portText, 0, 0);
        rootPane.add(portTextField, 0, 1);
        rootPane.add(portApprovalButton, 0, 2);
        rootPane.add(errorLabel, 0, 3);
        return new Scene(rootPane, 400, 300);
    }

    public Scene makeServerUI(Server server) {
        GridPane rootPane = new GridPane();
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(20));
        rootPane.setHgap(30);
        rootPane.setVgap(10);
        Label logLabel = new Label("Server Log");
        ListView<String> logView = new ListView<>(server.serverLog);
        Label clientLabel = new Label("Clients Connected");
        ListView<String> clientView = new ListView<>(server.clientNames);
        Label chatLabel = new Label("Chat Log");
        ListView<String> chatView = new ListView<>(server.chats);
        Label muteLabel = new Label("mute Clients");
        ListView<String> muteView = new ListView<>(server.muteClient);

        // 추가
        class ListViewHandler implements EventHandler<MouseEvent> {
            @Override
            public void handle(MouseEvent event) {
            }
        }

        clientView.setOnMouseClicked(new ListViewHandler() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                nickName = clientView.getSelectionModel().getSelectedItem().split(" ")[0];
                idx = clientView.getSelectionModel().getSelectedIndex();
            }
        });

        Button kick = new Button("kick");
        kick.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { // 버튼 클릭 이벤트
                System.out.println("kick " + idx);
                server.clientDisconnected(server.clientThreads.get(idx), false);
            }
        });

        Button mute = new Button("mute");
        mute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("mute: " + nickName);
                muteList.add(nickName);
                muteList.addListener(new Server.MyListener());
                server.muteClient.add(nickName);

                // 뮤트 버튼을 클릭했을 때 해당 닉네임가진 사람 채팅 X
            }
        });

        //추가
        Button unMute = new Button("unMute");
        unMute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("unMute : " + nickName);
                muteList.remove(nickName);
                server.muteClient.remove(nickName);
                // 뮤트 버튼을 클릭했을 때 해당 닉네임가진 사람 채팅 X
            }
        });
//여기까지

        rootPane.add(logLabel, 0, 0);
        rootPane.add(logView, 0, 1);
        rootPane.add(clientLabel, 1, 0);
        rootPane.add(muteLabel, 2, 0);
        rootPane.add(muteView, 2, 1);
        rootPane.add(clientView, 1, 1);
        rootPane.add(chatLabel, 3, 0);
        rootPane.add(chatView, 3, 1);
        rootPane.add(kick, 0, 2);
        rootPane.add(mute, 1, 2);
        //추가
        rootPane.add(unMute, 2, 2);
//여기까지
        return new Scene(rootPane, 1000, 600);
    }
}
