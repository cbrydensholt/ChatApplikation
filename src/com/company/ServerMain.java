package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        int port = 8811; //port number vi vil connecte til
        Server server = new Server(port);
        server.start(); //starter vores thread
    }


}
