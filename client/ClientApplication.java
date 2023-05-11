package com.example.chat.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ClientApplication extends Application {
	private ArrayList<Thread> threads;
	public static void main(String[] args){
		launch();
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		for (Thread thread: threads){
			thread.interrupt();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		threads = new ArrayList<>();
		primaryStage.setTitle("JavaFX Chat Client");
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
		Label nameLabel = new Label("Name ");
		Label hostNameLabel = new Label("Host Name");
		Label portNumberLabel = new Label("Port Number");
		Label errorLabel = new Label();
		Button submitClientInfoButton = new Button("Done");
		submitClientInfoButton.setOnAction((event) -> {
				Client client;
				try {
					client = new Client(hostNameField.getText(),
							Integer.parseInt(portNumberField.getText()),
							nameField.getText());
					Thread clientThread = new Thread(client);
					clientThread.setDaemon(true);
					clientThread.start();
					threads.add(clientThread);
					primaryStage.close();
					primaryStage.setScene(makeChatUI(client));
					primaryStage.show();
				}
				catch(ConnectException e){
					errorLabel.setTextFill(Color.RED);
					errorLabel.setText("Invalid host name, try again");
				}
				catch (NumberFormatException | IOException e) {
					errorLabel.setTextFill(Color.RED);
					errorLabel.setText("Invalid port number, try again");
				}
		});
		rootPane.add(nameField, 0, 0);
		rootPane.add(nameLabel, 1, 0);
		rootPane.add(hostNameField, 0, 1);
		rootPane.add(hostNameLabel, 1, 1);
		rootPane.add(portNumberField, 0, 2);
		rootPane.add(portNumberLabel, 1, 2);
		rootPane.add(submitClientInfoButton, 0, 3, 2, 1);
		rootPane.add(errorLabel, 0, 4);
		return new Scene(rootPane, 400, 400);
	}

	public void makeFileUi(Client client) {
		Stage fileStage = new Stage();
		fileStage.setTitle("Attached file");
		GridPane rootPane = new GridPane();
		rootPane.setPadding(new Insets(20));
		rootPane.setAlignment(Pos.CENTER);
		rootPane.setHgap(10);
		rootPane.setVgap(10);
		TextField chatTextField = new TextField();
		Button button = new Button("파일등록");
		button.setOnAction((event) -> {
			String fileName = chatTextField.getText();
			chatTextField.clear();
			try {
				FileInputStream file = new FileInputStream(fileName);
				byte[] readData = new byte[1024];
				while (true) {
					int ret = file.read(readData);
					if (ret == -1)
						break;
					client.fileBuffer.put(readData);
				}
			} catch (Exception e) {
				chatTextField.setText("Wrong file path");
				e.printStackTrace();
			}
		});
		rootPane.add(chatTextField, 0, 0);
		rootPane.add(button,1, 0);
		fileStage.setScene(new Scene(rootPane, 300, 100));
		fileStage.show();
	}

	public Scene makeChatUI(Client client) {
		GridPane rootPane = new GridPane();
		rootPane.setPadding(new Insets(20));
		rootPane.setAlignment(Pos.CENTER);
		rootPane.setHgap(10);
		rootPane.setVgap(10);
		ListView<String> chatListView = new ListView<>();
		chatListView.setItems(client.chatLog);
		TextField chatTextField = new TextField();
		chatTextField.setOnAction((event) -> {
				client.writeToServer(chatTextField.getText());
				chatTextField.clear();
		});
		Button button = new Button("파일첨부");
		button.setOnAction((event) -> makeFileUi(client));
		rootPane.add(chatListView, 0, 0);
		rootPane.add(chatTextField, 0, 1);
		rootPane.add(button,1, 1);
		return new Scene(rootPane, 400, 400);
	}
}
