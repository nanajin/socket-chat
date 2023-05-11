package com.example.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.chat.ServerApplication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Server implements Runnable {
    private int portNumber;
    private ServerSocket socket;
    private ArrayList<Socket> clients;
    private ArrayList<Socket> clientsFile;
    public ArrayList<ClientThread> clientThreads;

    public ArrayList<ClientFileThread> clientFileThreads;
    public ObservableList<String> serverLog;
    public ObservableList<String> muteClient;
    public ObservableList<String> clientNames;
    public ObservableList<String> chats;
    public ObservableList<String> clientRealNames;

    // 추가
    public static ObservableList<String> list;
    public static class MyListener implements ListChangeListener<String> {
        @Override
        public void onChanged(Change<? extends String> change) {
            System.out.println("server change.getlist = " + change.getList());
            list = (ObservableList<String>) change.getList();
        }
    }
    //
    public Server(int portNumber) throws IOException {
        this.portNumber = portNumber;
        serverLog = FXCollections.observableArrayList();
        muteClient = FXCollections.observableArrayList();
        clientNames = FXCollections.observableArrayList();
        chats = FXCollections.observableArrayList();
        clientRealNames = FXCollections.observableArrayList();
        clients = new ArrayList<>();
        clientThreads = new ArrayList<>();
        socket = new ServerSocket(portNumber);
        clientsFile = new ArrayList<>();
        clientFileThreads = new ArrayList<>();
    }



    public void run() {
        try {
            while (true) {
                Platform.runLater(() -> serverLog.add("Listening for client"));
                final Socket clientSocket = socket.accept();
                String[] firstMsg = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())).
                        readLine().split(":");
                synchronized (this) {
                    if (firstMsg[0].equals("[MSG]")) {
                        clients.add(clientSocket);
                        Platform.runLater(() ->
                                serverLog.add("Client "
                                        + clientSocket.getRemoteSocketAddress()
                                        + " connected"));
                        ClientThread clientThreadHolderClass = new ClientThread(clientSocket, this, firstMsg[1]);
                        Thread clientThread = new Thread(clientThreadHolderClass);
                        clientThreads.add(clientThreadHolderClass);
                        clientThread.setDaemon(true);
                        clientThread.start();
                        ServerApplication.threads.add(clientThread);
                        synchronized (this) {
                            clientRealNames.add(firstMsg[1]);
                            firstToAllSockets();
                        }
                    } else {
                        clientsFile.add(clientSocket);
                        ClientFileThread clientFileThreadHolderClass = new ClientFileThread(clientSocket, this, firstMsg[1]);
                        Thread clientFileThread = new Thread(clientFileThreadHolderClass);
                        clientFileThreads.add(clientFileThreadHolderClass);
                        clientFileThread.setDaemon(true);
                        clientFileThread.start();
                        ServerApplication.threads.add(clientFileThread);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void clientDisconnected(ClientThread client,boolean b) {
        System.out.println("Server.java에서 clientThread: "+clientThreads);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverLog.add("Client "
                        + client.getClientSocket().getRemoteSocketAddress()
                        + " disconnected");
                clients.remove(clientThreads.indexOf(client));
                clientNames.remove(clientThreads.indexOf(client));
                clientThreads.remove(clientThreads.indexOf(client));
                clientRealNames.remove(clientThreads.indexOf(client));
                if(b){
                    writeToAllSockets(client.getClientName()+"님께서 나가셨습니다", false);
                }
                else{
                    System.out.println("Server.java outperson: "+client.getClientName());
                    client.writeToServer("강퇴당하셨습니다", client);
                    writeToAllSockets(client.getClientName()+"님께서 강퇴당하셨습니다", false);
                }
                ServerApplication.threads.get(ServerApplication.threads.indexOf(client)).interrupt();
            }
        });
        try {
            client.clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized void clientFileDisconnected(ClientFileThread clientFile) {
        if (clientsFile.contains(clientFile)) {
            clientsFile.remove(clientFileThreads.indexOf(clientFile));
            clientFileThreads.remove(clientFileThreads.indexOf(clientFile));
        }
        ServerApplication.threads.get(ServerApplication.threads.indexOf(clientFile)).interrupt();
    }

    public void writeToAllSockets(String input, Boolean time) {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat s = new SimpleDateFormat("[a hh:mm] ");

        for (ClientThread clientThread : clientThreads) {
            if(list != null && list.contains(clientThread.getClientName())) {
                System.out.println("뮤트");
            }
            else{
                if(time){
                    clientThread.writeToServer(s.format(now) + input, clientThread);
                }
                else{
                    clientThread.writeToServer(input, clientThread);
                }
            }
        }
    }

    public void firstToAllSockets() {
        for (ClientThread clientThread : clientThreads) {
            if (list != null && list.contains(clientThread.getClientName())) {
                System.out.println("뮤트");
            } else {
                StringBuilder stringBuilder = new StringBuilder("[INDEX]:");
                for (int i = 0; i < clientRealNames.size() - 1; i++) {
                    stringBuilder.append(clientRealNames.get(i) + "&");
                }
                stringBuilder.append(clientRealNames.get(clientRealNames.size() - 1));
                clientThread.writeToServer(stringBuilder.toString(), clientThread);
            }
        }
    }
}
