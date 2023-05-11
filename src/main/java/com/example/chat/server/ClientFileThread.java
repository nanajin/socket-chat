package com.example.chat.server;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class ClientFileThread implements Runnable {
    private Socket clientSocket;
    private Server baseServer;
    private InputStream clientToServerStream;
    private OutputStream serverToClientStream;
    public String clientName;
    private File imageFile;

    public ClientFileThread(Socket clientSocket, Server baseServer, String clientName) {
        this.clientSocket = clientSocket;
        this.clientName = clientName;
        this.baseServer = baseServer;
        try {
            clientToServerStream = clientSocket.getInputStream();
            serverToClientStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            byte[] readData = new byte[1024];
            int n = clientToServerStream.read(readData, 0, 21);
            if (n == -1)
                throw new SocketException();
            String[] header = (new String(readData, 0, 21, StandardCharsets.UTF_8)).split(":");
            imageFile = new File("image/" + header[0]);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            int fileSize = Integer.parseInt(header[1]);
            int readSize = 0;
            while ((readSize = clientToServerStream.read(readData, 0, fileSize < 1024 ? fileSize : 1024)) != -1) {
                fileOutputStream.write(readData);
                fileSize -= readSize;
                if (fileSize == 0)
                    break;
            }
            fileOutputStream.close();
            while (true) {
                n = clientToServerStream.read(readData, 0, 21);
                if (n == -1)
                    throw new SocketException();
                header = (new String(readData, 0, 21, StandardCharsets.UTF_8)).split(":");
                String otherClientName = header[0];
                Optional<ClientFileThread> optionalClient = baseServer.clientFileThreads.stream().filter(c -> {
                    return c.clientName.equals(otherClientName);
                }).findFirst();
                if (optionalClient.isEmpty())
                    continue;
                ClientFileThread client = optionalClient.get();
                FileInputStream fileInputStream = new FileInputStream(client.imageFile);
                StringBuffer fileSizeString = new StringBuffer(Long.toString(client.imageFile.length()));
                while (fileSizeString.length() < 10) {
                    fileSizeString.insert(0, 0);
                }
                byte[] msg = (client.imageFile.getName() +
                        ":" + fileSizeString.toString()).
                        getBytes(StandardCharsets.UTF_8);
                serverToClientStream.write(msg, 0, 21);
                readSize = 0;
                while ((readSize = fileInputStream.read(readData)) != -1) {
                    serverToClientStream.write(readData, 0, readSize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            baseServer.clientFileDisconnected(this);
        }
    }
}
