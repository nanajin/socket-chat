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
	private Socket clientSocket;
	private BufferedReader serverToClientReader;
	private PrintWriter clientToServerWriter;
	private String name;
	public ByteBuffer fileBuffer;
	public ObservableList<String> chatLog;

	public Client(String hostName, int portNumber, String name) throws UnknownHostException, IOException {
			clientSocket = new Socket(hostName, portNumber);
			serverToClientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			clientToServerWriter = new PrintWriter(clientSocket.getOutputStream(), true);
			chatLog = FXCollections.observableArrayList();
			this.name = name;
			clientToServerWriter.println(name);
			fileBuffer = ByteBuffer.allocate(1024 * 1024 * 1024);
	}

	public void writeToServer(String input) {
		clientToServerWriter.println(name + " : " + input);
	}

	public void run() {
		while (true) {
			try {
				final String inputFromServer = serverToClientReader.readLine();
				Platform.runLater(() -> chatLog.add(inputFromServer));
			} catch (SocketException e) {
				Platform.runLater(() -> chatLog.add("Error in server"));
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
