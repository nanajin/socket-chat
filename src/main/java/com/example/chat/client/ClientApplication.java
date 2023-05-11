package com.example.chat.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ClientApplication extends Application {
    private ArrayList<Thread> threads;

    public void showClientProfile(String clientName, ClientImage clientImage) {
        Stage profileStage = new Stage();
        profileStage.setTitle("Profile");
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(0);
        TextField name = new TextField();
        TextField nameField = new TextField();
        name.setText("NickName");
        name.setDisable(true);
        name.setAlignment(Pos.CENTER);
        nameField.setText(clientName);
        nameField.setDisable(true);
        nameField.setAlignment(Pos.CENTER);
        ImageView imageView = new ImageView();
        clientImage.setImage(imageView, profileStage);
        try {
            StringBuilder stringBuilder = new StringBuilder(clientName);
            stringBuilder.append(":");
            while (stringBuilder.length() < 21) {
                stringBuilder.append(0);
            }
            byte[] msg = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
            clientImage.fileToServer(msg, 21);
        } catch (Exception e) {
            nameField.setText("SomeThing Wrong");
        }
        imageView.setFitWidth(160);
        imageView.setFitHeight(150);
        rootPane.add(imageView, 0, 0);
        rootPane.add(name, 0, 1);
        rootPane.add(nameField, 0, 2);
        profileStage.setScene(new Scene(rootPane, 300, 300));
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        threads = new ArrayList<>();
        primaryStage.setTitle("Chat");
        primaryStage.setScene(makeInitScene(primaryStage));
        primaryStage.show();
    }

    public Scene makeInitScene(Stage primaryStage) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);
        rootPane.setAlignment(Pos.CENTER);
        TextField nameField = new TextField();
        TextField hostNameField = new TextField();
        TextField portNumberField = new TextField();
        Label nameLabel = new Label("사용자 닉네임 ");
        Label hostNameLabel = new Label("서버 아이피 번호");
        Label portNumberLabel = new Label("서버 포트 번호");
        Label errorLabel = new Label();
        Button submitClientInfoButton = new Button("접속");
        submitClientInfoButton.setOnAction(event -> {
            Client client;
            ClientImage clientImage;
            TimeChecker timeChecker;
            try {
                final String ip = hostNameField.getText();
                final int port = Integer.parseInt(portNumberField.getText());
                final String name = nameField.getText();
                File directory = new File("clientimage");
                directory.mkdir();
                client = new Client(ip, port, name);
                clientImage = new ClientImage(ip, port, name);
                timeChecker = new TimeChecker(client.chatLog);
                Thread clientThread = new Thread(client);
                Thread timeCheckThread = new Thread(timeChecker);
                Thread clientImageThread = new Thread(clientImage);
                clientThread.setDaemon(true);
                clientThread.start();
                clientImageThread.setDaemon(true);
                clientImageThread.start();
                timeCheckThread.setDaemon(true);
                timeCheckThread.start();
                threads.add(clientThread);
                threads.add(clientImageThread);
                threads.add(timeCheckThread);
                primaryStage.close();
                primaryStage.setScene(makeFileUi(client, timeChecker, clientImage, primaryStage));
                primaryStage.show();
            } catch (ConnectException e) {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Invalid host name, try again");
            } catch (NumberFormatException | IOException e) {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Invalid port number, try again");
            }
        });
        Button exit = new Button("종료");
        exit.setOnAction((e) -> {
            System.exit(0);
        });
        exit.setAlignment(Pos.CENTER);
        submitClientInfoButton.setAlignment(Pos.CENTER);
        submitClientInfoButton.setMinSize(160, 0);
        exit.setMinSize(160, 0);
        rootPane.add(nameField, 1, 0);
        rootPane.add(nameLabel, 0, 0);
        rootPane.add(hostNameField, 1, 1);
        rootPane.add(hostNameLabel, 0, 1);
        rootPane.add(portNumberField, 1, 2);
        rootPane.add(portNumberLabel, 0, 2);
        rootPane.add(submitClientInfoButton, 0, 3);
        rootPane.add(exit, 1, 3);
        rootPane.add(errorLabel, 0, 4);

        return new Scene(rootPane, 400, 200);
    }
    public Scene makeFileUi(Client client, TimeChecker timeChecker, ClientImage clientImage, Stage stage) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);
        TextField chatTextField = new TextField();
        ImageView imageView = new ImageView();
        Button button = new Button("파일등록");
        Button button1 = new Button("확인");
        button1.setDisable(true);
        button.setOnAction(event -> {
            String fileName = chatTextField.getText();
            chatTextField.clear();
            try {
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex == -1)
                    throw new IllegalArgumentException();
                String extension = fileName.substring(dotIndex);
                if (!extension.equals(".png") && !extension.equals(".jpg"))
                    throw new IllegalArgumentException();
                File file = new File(fileName);
                if (!file.exists())
                    throw new IllegalArgumentException();
                long fileSize = file.length();
                if (fileSize > 1048576)
                    throw new IllegalArgumentException();
                StringBuffer fileSizeString = new StringBuffer(Long.toString(fileSize));
                while (fileSizeString.length() < 10) {
                    fileSizeString.insert(0, 0);
                }
                byte[] msg = (UUID.randomUUID().toString().substring(0, 6) + extension +
                        ":" + fileSizeString.toString()).getBytes(StandardCharsets.UTF_8);
                clientImage.fileToServer(msg, 21);
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] readData = new byte[1024];
                int readSize = 0;
                while ((readSize = fileInputStream.read(readData)) != -1) {
                    clientImage.fileToServer(readData, readSize);
                }
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                button1.setDisable(false);
            } catch (Exception e) {
                chatTextField.setText("Wrong path or file or extension or size");
            }
        });
        button1.setOnAction(event -> {
            stage.close();
            stage.setScene(makeChatUI(client, timeChecker, clientImage));
            stage.show();
        });
        rootPane.add(chatTextField, 0, 1);
        imageView.setFitHeight(150);
        imageView.setFitWidth(160);
        rootPane.add(imageView, 0, 0);
        rootPane.add(button, 1, 1);
        rootPane.add(button1, 1, 0);
        return new Scene(rootPane, 400, 300);
    }

    //채팅방 대화 입출력
    public boolean containsProfanity(String message) {
        String[] profanityList = {"바보", "멍청이", "병신"};

        for (String profanity : profanityList) {
            if (message.contains(profanity)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsMannerWord(String message) {
        String[] mannerWordList = {"세요", "습니다", "감사합니다", "겠습니다"};
        for (String mannerWord : mannerWordList) {
            if (message.contains(mannerWord)) {
                return true;
            }
        }
        return false;
    }


    public Scene makeChatUI(Client client, TimeChecker timeChecker, ClientImage clientImage) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);
        ListView<TimeChatLog> chatListView = new ListView<>(client.chatLog);
        chatListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<>() {
            @Override
            public String toString(TimeChatLog object) {
                return object.getElement();
            }

            @Override
            public TimeChatLog fromString(String string) {
                return new TimeChatLog(string);
            }
        }));

        ListView<String> clientListView = new ListView<>(client.chatList);
        clientListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    Label label = new Label(item);
                    setGraphic(label);
                }
            }
        });

        Button button1 = new Button("프로필 가져오기");
        button1.setDisable(true);
        button1.setOnAction(event -> {
            button1.setDisable(true);
            int index = clientListView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                String clientName = clientListView.getItems().get(index);
                showClientProfile(clientName, clientImage);
            }
        });

        clientListView.setOnMouseClicked(event -> button1.setDisable(false));

        TextField chatTextField = new TextField();
        chatTextField.setOnAction((event) -> {
            String message = chatTextField.getText();
            if (containsProfanity(message)) {
                // 비매너
                client.writeToServer("비속어 사용할 수 없습니다^^");
                client.pointOfManner--;
            } else if (containsMannerWord(message)) {
                // 매너
                client.writeToServer(message);
                client.pointOfManner++;
            } else {
                client.writeToServer(message);
            }
            chatTextField.clear();
        });
        Button button2 = new Button("삭제 멈춤");
        button2.setOnAction(event -> {
            if (timeChecker.isStop()) {
                button2.setText("삭제 멈춤");
                timeChecker.reStart();
            } else {
                button2.setText("삭제 시작");
                timeChecker.stopTime();
            }
        });

        Button button3 = new Button("배경 등록");
        button3.setOnAction(event -> {
            Stage stage = new Stage();
            GridPane pane = new GridPane();
            pane.setPadding(new Insets(20));
            pane.setAlignment(Pos.CENTER);
            pane.setHgap(10);
            pane.setVgap(10);
            TextField questionField = new TextField();
            questionField.setPromptText("배경 이미지 파일을 등록하세요.");
            Button button = new Button("등록");
            button.setOnAction(event2 -> {
                String fileName = questionField.getText();
                questionField.clear();
                try {
                    int dotIndex = fileName.lastIndexOf('.');
                    if (dotIndex == -1)
                        throw new IllegalArgumentException();
                    String extension = fileName.substring(dotIndex);
                    if (!extension.equals(".png") && !extension.equals(".jpg"))
                        throw new IllegalArgumentException();
                    File file = new File(fileName);
                    if (!file.exists())
                        throw new IllegalArgumentException();
                    long fileSize = file.length();
                    if (fileSize > 1048576)
                        throw new IllegalArgumentException();
                    Image image = new Image(file.toURI().toString());
                    BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
                    BackgroundImage backgroundImage = new BackgroundImage(image,
                            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.DEFAULT, backgroundSize);
                    Background background = new Background(backgroundImage);
                    rootPane.setBackground(background);
                } catch (Exception e) {
                    questionField.setText("Wrong path or file or extension or size");
                }
            });
            pane.add(questionField, 0, 0);
            pane.add(button, 1, 0);
            stage.setScene(new Scene(pane, 300, 300));
            stage.show();
        });

        Button startGameButton = new Button("끝말잇기");
        startGameButton.setOnAction(event -> client.writeToServer("나부터 할게 \n 바나나"));


        Button askQuestionButton = new Button("문제 내기");
        askQuestionButton.setOnAction(event -> {
            TextField questionField = new TextField();
            questionField.setPromptText("문제를 입력하세요");
            TextField answerField = new TextField();
            answerField.setPromptText("정답을 입력하세요");

            Button confirmButton = new Button("확인");
            confirmButton.setOnAction(eventa -> {
                String question = questionField.getText();
                String answer = answerField.getText();
                questionField.setText("");
                answerField.setText("");
            });
            VBox vbox = new VBox();
            vbox.getChildren().addAll(new Label("문제:"), questionField, new Label("정답:"), answerField, confirmButton);
            Scene scene = new Scene(vbox, 300, 200);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        });
        rootPane.add(chatListView, 0, 0, 1, 11);
        chatListView.setPrefSize(600, 550);

        rootPane.add(chatTextField, 0, 11, 1, 1);
        chatTextField.setPrefSize(600, 50);

        rootPane.add(clientListView, 1, 0, 1, 4);
        clientListView.setPrefSize(200, 200);

        rootPane.add(startGameButton, 1, 4, 1, 4);
        startGameButton.setPrefSize(200, 200);

        rootPane.add(askQuestionButton, 1, 8, 1, 4);
        askQuestionButton.setPrefSize(200, 200);

        rootPane.add(button1, 2, 0);
        rootPane.add(button2, 2, 1);
        rootPane.add(button3, 2, 2);

        return new Scene(rootPane, 1000, 450);
    }
}
