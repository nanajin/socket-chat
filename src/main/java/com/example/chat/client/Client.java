package com.example.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client implements Runnable {
	public int pointOfManner=10;
	private Socket clientSocket;
	private BufferedReader serverToClientReader;
	private PrintWriter clientToServerWriter;
	public String name;
	public ObservableList<TimeChatLog> chatLog;
	public ObservableList<String> chatList;


	public String newName;
	public Client(String hostName, int portNumber, String name) throws UnknownHostException, IOException {
			clientSocket = new Socket(hostName, portNumber);
			serverToClientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			clientToServerWriter = new PrintWriter(clientSocket.getOutputStream(), true);
			chatList = FXCollections.observableArrayList();
			chatLog = FXCollections.observableArrayList();
			this.name = name;
			clientToServerWriter.println("[MSG]:" + name);
	}

	public void writeToServer(String input) {newName=name;
		newName =name;
		if (pointOfManner > 20) {
			newName = "(친절한)" + name;
			clientToServerWriter.println(newName + " : " + input);
		} else if (pointOfManner < 0) {
			newName="(과격한)"+name;
			clientToServerWriter.println(newName + " : " + input);

		} else {
			clientToServerWriter.println(newName + " : " + input);
		}
	}

	public void run() {
		while (true) {
			try {
				final String inputFromServer = serverToClientReader.readLine();
				if(inputFromServer == null)
					throw new SocketException();
				if(inputFromServer.contains(":")) {
					String[] msg = inputFromServer.split(":");
					if (msg[0].equals("[INDEX]")) {
						Platform.runLater(() -> chatList.clear());
						String[] nickNames = msg[1].split("&");
						for (String nickName : nickNames) {
							Platform.runLater(() -> chatList.add(nickName));
						}
						continue;
					}
				}
				Platform.runLater(() -> {
					chatLog.add(new TimeChatLog(inputFromServer));
				});
			} catch (SocketException e) {
				Platform.runLater(() -> {
					chatLog.add(new TimeChatLog("Error in server"));
				});
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}