package com.example.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javafx.application.Platform;

import static com.example.chat.ServerApplication.muteList;

public class ClientThread implements Runnable {

	public Socket clientSocket;
	private Server baseServer;
	private BufferedReader incomingMessageReader;
	private PrintWriter outgoingMessageWriter;
	private String clientName;

	public ClientThread(Socket clientSocket, Server baseServer, String clientName) {
		this.clientSocket = clientSocket;
		this.clientName = clientName;
		this.baseServer = baseServer;
		try {
			incomingMessageReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outgoingMessageWriter = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			Platform.runLater(() -> {
					baseServer.clientNames.add(clientName + " - "
							+ clientSocket.getRemoteSocketAddress());
					baseServer.writeToAllSockets(clientName+"님이 참가하셨습니다.", false);
			});
			String inputToServer;
			while (true) {
				inputToServer = incomingMessageReader.readLine();
				if (inputToServer == null)
					throw new SocketException();
				if(muteList.contains(inputToServer.split(" : ")[0])){
					inputToServer ="[뮤트된 사용자입니다] "+inputToServer;
				}
				String finalInputToServer1 = inputToServer;
				Platform.runLater(() -> baseServer.chats.add(finalInputToServer1));
				baseServer.writeToAllSockets(inputToServer,true);
			}
		} catch (Exception e) {
			baseServer.clientDisconnected(this,true);
		}
	}

	public void writeToServer(String input, ClientThread thread) {
		System.out.println("Client Trhead에서 baseServer의 쓰레드: "+ baseServer.clientThreads);
		if(baseServer.clientThreads.contains(thread)) {
			outgoingMessageWriter.println(input);
		}
		else {
			outgoingMessageWriter.println("강퇴당하셨습니다");
		}
	}

	public String getClientName() {
		return this.clientName;
	}
	public Socket getClientSocket() {
		return clientSocket;
	}

}
