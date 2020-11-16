package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;

    private ArrayList<ServerWorker> workerList = new ArrayList<>();
        //arraylist af brugere der kan forbinde sig til serveren (med hver deres socket)

    public Server(int serverPort) {

        this.serverPort = serverPort;

    }

    public List<ServerWorker> getWorkerList(){

        return workerList;

    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort); //socket vi bruger til at skaffe forbindelse til serveren.
            while (true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept(); //creates the connection between server and client
                System.out.println("Accepted client connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
                //vi gør dette fordi vi vil have en collection af workers, så vi kan have flere workers der kan kommunikere med hinanden.
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker){

        workerList.remove(serverWorker);

    }


}
