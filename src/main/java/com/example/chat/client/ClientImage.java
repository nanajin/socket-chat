package com.example.chat.client;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class ClientImage implements Runnable {
    private Socket clientSocket;
    private InputStream serverToClinetStream;
    private OutputStream clientToServerStream;
    private String name;
    private ImageView imageView;
    private Stage profileStage;

    public ClientImage(String hostName, int portNumber, String name) throws UnknownHostException, IOException {
        clientSocket = new Socket(hostName, portNumber);
        serverToClinetStream = clientSocket.getInputStream();
        clientToServerStream = clientSocket.getOutputStream();
        this.name = name;
        new PrintWriter(clientSocket.getOutputStream(), true).println("[File]:" + name);
    }

    public void fileToServer(byte[] input, int size) throws Exception {
        clientToServerStream.write(input, 0, size);
    }

    public void setImage(ImageView imageView, Stage profileStage) {
        this.imageView = imageView;
        this.profileStage = profileStage;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] readData = new byte[1024];
                int n = serverToClinetStream.read(readData, 0, 21);
                if (n == -1)
                    throw new SocketException();
                String[] header = (new String(readData,0, 21, StandardCharsets.UTF_8)).split(":");
                File file = new File("clientimage/" + header[0]);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                int fileSize = Integer.parseInt(header[1]);
                int readSize = 0;
                while ((readSize = serverToClinetStream.read(readData, 0, fileSize < 1024 ? fileSize : 1024)) != -1) {
                    fileOutputStream.write(readData);
                    fileSize -= readSize;
                    if (fileSize == 0)
                        break ;
                }
                fileOutputStream.close();
                Platform.runLater(() -> {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    profileStage.show();
                });
            }
        } catch (Exception e) {}
    }
}
